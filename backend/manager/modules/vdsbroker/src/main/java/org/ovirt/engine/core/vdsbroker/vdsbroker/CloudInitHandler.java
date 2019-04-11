package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.common.businessentities.network.CloudInitNetworkProtocol.OPENSTACK_METADATA;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmInitToOpenStackMetadataAdapter;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.network.vm.VmInitNetworkIpInfoFetcher;
import org.ovirt.engine.core.utils.network.vm.VmInitNetworkIpv4InfoFetcher;
import org.ovirt.engine.core.utils.network.vm.VmInitNetworkIpv6InfoFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class CloudInitHandler {

    private static final Logger log = LoggerFactory.getLogger(CloudInitHandler.class);

    private final VmInit vmInit;
    private final Map<String, Object> metaData;
    private final Map<String, Object> userData;
    private final Map<String, byte[]> files;
    private Supplier<String> userDataProvider;
    private int nextFileIndex;
    private String interfaces;
    private Map<String, Object> networkData;

    private final String passwordKey = "password";

    public List<EngineMessage> validate(VmInit vmInit) {
        // validate only if 'Initial Run' parameters were specified
        // and required payload network protocol is OpenstackMetadata
        if (vmInit != null && useOpenstackMetadataProtocol(vmInit)) {
            return new VmInitToOpenStackMetadataAdapter().validate(vmInit);
        }
        return Collections.emptyList();
    }

    private enum CloudInitFileMode {
        FILE,
        NETWORK;
    }

    /**
     * c'tor required by Mock framework in tests
     * and used also by Inject framework in commands.
     */
    private CloudInitHandler() {
        vmInit = null;
        metaData = null;
        userData = null;
        files = null;
    }

    public CloudInitHandler (VmInit vmInit){
        this.vmInit = vmInit;
        metaData = new HashMap<>();
        userData = new HashMap<>();
        files = new HashMap<>();
        nextFileIndex = 0;
        userDataProvider = this::getUserData;
    }

    public CloudInitHandler (VmInit vmInit, Supplier<String> customUserDataProvider) {
        this(vmInit);
        this.userDataProvider = customUserDataProvider;
    }


    public Map<String, byte[]> getFileData()
            throws IOException {
        if (vmInit != null) {
            try {
                storeHostname();
                storeAuthorizedKeys();
                storeRegenerateKeys();
                storeNetwork();
                storeTimeZone();
                storeRootPassword();
                storeUserName();
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Malformed input", ex);
            }
        }

        // Add other required/supplemental data
        storeExecutionParameters();

        String metaDataStr = mapToJson(metaData);
        String networkDataStr = !MapUtils.isEmpty(networkData) ? mapToJson(networkData) : "";
        String userDataStr = userDataProvider.get();

        files.put("openstack/latest/meta_data.json", metaDataStr.getBytes("UTF-8"));
        files.put("openstack/latest/user_data", userDataStr.getBytes("UTF-8"));
        if (!StringUtils.isEmpty(networkDataStr) && useOpenstackMetadataProtocol()) {
            //must not pass an empty file or a file with an empty json to cloud-init-0.7.9-9 because the whole init flow fails
            files.put("openstack/latest/network_data.json", networkDataStr.getBytes("UTF-8"));
            log.debug("cloud-init network_data.json:\n{}", networkDataStr);
        }
        // mask password for log if exists
        if (metaDataStr.contains(passwordKey) && vmInit != null && vmInit.getRootPassword() != null) {
            String oldStr = String.format("\"%s\" : \"%s\"", passwordKey, vmInit.getRootPassword());
            String newStr = String.format("\"%s\" : ***", passwordKey);
            metaDataStr = metaDataStr.replace(oldStr, newStr);
        }
        log.debug("cloud-init meta-data:\n{}", metaDataStr);
        log.debug("cloud-init user-data:\n{}", userDataStr);
        return files;
    }

    private String getUserData() {
        String userDataStr = mapToYaml(userData);

        if (vmInit != null && vmInit.getCustomScript() != null) {
            userDataStr += vmInit.getCustomScript();
        }

        // add #cloud-config for user data file head
        if (StringUtils.isNotBlank(userDataStr)) {
            userDataStr = "#cloud-config\n" + userDataStr;
        }
        return userDataStr;
    }

    private void storeNetwork() throws UnsupportedEncodingException {
        if (useOpenstackMetadataProtocol()) {
            networkData = new VmInitToOpenStackMetadataAdapter().asMap(vmInit);
        } else {
            storeNetworkAsEni();
        }
    }

    private boolean useOpenstackMetadataProtocol() {
        return useOpenstackMetadataProtocol(vmInit);
    }

    /**
     * Openstack Metadata is the default protocol, so use it if it
     * is specified explicitly or if no protocol is specified at all.
     */
    private boolean useOpenstackMetadataProtocol(VmInit vmInit) {
        return vmInit != null && (vmInit.getCloudInitNetworkProtocol() == null ||
            OPENSTACK_METADATA.equals(vmInit.getCloudInitNetworkProtocol()));
    }

    private void storeHostname() {
        if (!StringUtils.isEmpty(vmInit.getHostname())) {
            metaData.put("hostname", vmInit.getHostname());
            metaData.put("name", vmInit.getHostname());
        }
    }

    private void storeAuthorizedKeys() {
        if (!StringUtils.isEmpty(vmInit.getAuthorizedKeys())) {
            metaData.put("public_keys", normalizeAuthorizedKeys(vmInit.getAuthorizedKeys()));
        }
    }

    private List<String> normalizeAuthorizedKeys(String authorizedKeys) {
        List<String> keys = new ArrayList<>();
        for (String key : vmInit.getAuthorizedKeys().split("(\\r?\\n|\\r)+")) {
            if (!StringUtils.isEmpty(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private void storeRegenerateKeys() {
        if (vmInit.getRegenerateKeys() != null) {
            // Create new system ssh keys
            userData.put("ssh_deletekeys", String.valueOf(vmInit.getRegenerateKeys()));
        }
    }

    private void storeNetworkAsEni() throws UnsupportedEncodingException {
        StringBuilder output = new StringBuilder();

        if (vmInit.getNetworks() != null) {
            List<VmInitNetwork> networks = vmInit.getNetworks();

            for (VmInitNetwork iface: networks) {
                if (Boolean.TRUE.equals(iface.getStartOnBoot())) {
                    output.append("auto ").append(iface.getName()).append("\n");
                }

                storeIpv4(iface, output);

                // As of cloud-init 0.7.1, you can't set DNS servers without also setting NICs
                if (vmInit.getDnsServers() != null) {
                    output.append("  dns-nameservers")
                        .append(" ").append(vmInit.getDnsServers());
                    output.append("\n");
                }

                if (vmInit.getDnsSearch() != null) {
                    output.append("  dns-search")
                        .append(" ").append(vmInit.getDnsSearch());
                    output.append("\n");
                }

                // bugzilla.redhat.com/1464043:
                // muting configuration of IPv6 until a proper solution is found
                //storeIpv6(iface, output);
            }
        }

        interfaces = output.toString();

        if (!interfaces.isEmpty()) {
            // Workaround for cloud-init 0.6.3, which requires the "network-interfaces"
            // meta-data entry instead of the "network_config" file reference
            metaData.put("network-interfaces", interfaces);

            // Cloud-init will translate this as needed for ifcfg-based systems
            storeNextFile(CloudInitFileMode.NETWORK, "/etc/network/interfaces", interfaces.getBytes("US-ASCII"));
        }
        log.debug("cloud-init network-interfaces:\n{}", interfaces);
    }

    private void storeIpv4(VmInitNetwork iface, StringBuilder output) {
        storeIp("inet", new VmInitNetworkIpv4InfoFetcher(iface), output);
    }

    private void storeIpv6(VmInitNetwork iface, StringBuilder output) {
        storeIp("inet6", new VmInitNetworkIpv6InfoFetcher(iface), output);
    }

    private void storeIp(String ipStack, VmInitNetworkIpInfoFetcher ipInfoFetcher, StringBuilder output) {
        output.append(String.format("iface %s %s %s%n",
                ipInfoFetcher.fetchName(),
                ipStack,
                ipInfoFetcher.fetchBootProtocol()));
        if (StringUtils.isNotEmpty(ipInfoFetcher.fetchIp())) {
            output.append(String.format("  address %s%n", ipInfoFetcher.fetchIp()));
        }
        if (StringUtils.isNotEmpty(ipInfoFetcher.fetchNetmask())) {
            output.append(String.format("  netmask %s%n", ipInfoFetcher.fetchNetmask()));
        }
        if (StringUtils.isNotEmpty(ipInfoFetcher.fetchGateway())) {
            output.append(String.format("  gateway %s%n", ipInfoFetcher.fetchGateway()));
        }
    }

    private void storeTimeZone() {
        if (vmInit.getTimeZone() != null) {
            userData.put("timezone", vmInit.getTimeZone());
        }
    }

    private void storeRootPassword() {
        if (!StringUtils.isEmpty(vmInit.getRootPassword())) {
            // Note that this is in plain text in the config disk
            userData.put(passwordKey, vmInit.getRootPassword());
        }
    }

    private void storeUserName() {
        if (!StringUtils.isEmpty(vmInit.getUserName())) {
            userData.put("user", vmInit.getUserName());
        }
    }

    private void storeExecutionParameters() {
        // Store defaults in meta-data and user-data that apply regardless
        // of parameters passed in from the user.

        // New instance id required for cloud-init to process data on startup
        metaData.put("uuid", UUID.randomUUID().toString());

        Map<String, String> meta = new HashMap<>();
        // Local allows us to set up networking
        meta.put("dsmode", "local");
        meta.put("essential", "false");
        meta.put("role", "server");
        metaData.put("meta", meta);

        metaData.put("launch_index", "0");
        metaData.put("availability_zone", "nova");

        userData.put("disable_root", 0);

        // Redirect log output from cloud-init execution from terminal
        Map<String, String> output = new HashMap<>();
        output.put("all", ">> /var/log/cloud-init-output.log");
        userData.put("output", output);

        // Disable metadata-server-based datasources to prevent long boot times
        List<String> runcmd = new ArrayList<>();
        runcmd.add("sed -i '/^datasource_list: /d' /etc/cloud/cloud.cfg; echo 'datasource_list: [\"NoCloud\", \"ConfigDrive\"]' >> /etc/cloud/cloud.cfg");
        userData.put("runcmd", runcmd);

        Map<String, Object> opts = new HashMap<>();
        opts.put("expire", false);
        userData.put("chpasswd", opts);
        userData.put("ssh_pwauth", true);
    }


    private void storeNextFile(CloudInitFileMode fileMode, String destinationPath, byte[] data) {
        String contentPath = String.format("/content/%04d", nextFileIndex++);
        Map<String, String> mdEntry = new HashMap<>();

        mdEntry.put("content_path", contentPath);
        mdEntry.put("path", destinationPath);

        if (fileMode == CloudInitFileMode.FILE) {
            if (!metaData.containsKey("files")) {
                metaData.put("files", new ArrayList<Map<String, String>>());
            }
            @SuppressWarnings("unchecked")
            List<Map<String, String>> mdFiles = (ArrayList<Map<String, String>>) metaData.get("files");
            mdFiles.add(mdEntry);
        } else {
            metaData.put("network_config", mdEntry);
        }

        files.put("openstack" + contentPath, data);
    }


    private String mapToJson(Map<String, Object> input) throws IOException {
        return JsonHelper.mapToJson(input);
    }

    private String mapToYaml(Map<String, Object> input) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(input);
    }

}
