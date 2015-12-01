package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.utils.JsonHelper;
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
    private int nextFileIndex;
    private String interfaces;

    private final String passwordKey = "password";

    private enum CloudInitFileMode {
        FILE,
        NETWORK;
    }

    public CloudInitHandler(VmInit vmInit) {
        this.vmInit = vmInit;
        metaData = new HashMap<>();
        userData = new HashMap<>();
        files = new HashMap<>();
        nextFileIndex = 0;
    }


    public Map<String, byte[]> getFileData()
            throws UnsupportedEncodingException, IOException, JsonGenerationException, JsonMappingException {
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
        String userDataStr = mapToYaml(userData);

        if (vmInit != null && vmInit.getCustomScript() != null) {
            userDataStr += vmInit.getCustomScript();
        }

        // add #cloud-config for user data file head
        if (StringUtils.isNotBlank(userDataStr)) {
            userDataStr = "#cloud-config\n" + userDataStr;
        }

        files.put("openstack/latest/meta_data.json", metaDataStr.getBytes("UTF-8"));
        files.put("openstack/latest/user_data", userDataStr.getBytes("UTF-8"));

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
        if (vmInit.getRegenerateKeys() != null && vmInit.getRegenerateKeys()) {
            // Create new system ssh keys
            userData.put("ssh_deletekeys", "True");
        }
    }

    private void storeNetwork() throws UnsupportedEncodingException {
        StringBuilder output = new StringBuilder();

        if (vmInit.getNetworks() != null) {
            List<VmInitNetwork> networks = vmInit.getNetworks();

            for (VmInitNetwork iface: networks) {
                if (Boolean.TRUE.equals(iface.getStartOnBoot())) {
                    output.append("auto ").append(iface.getName()).append("\n");
                }

                output.append("iface " + iface.getName() + " inet "
                        + iface.getBootProtocol().getDisplayName() + "\n");
                if (StringUtils.isNotEmpty(iface.getIp())) {
                    output.append("  address " + iface.getIp() + "\n");
                }
                if (StringUtils.isNotEmpty(iface.getNetmask())) {
                    output.append("  netmask " + iface.getNetmask() + "\n");
                }
                if (StringUtils.isNotEmpty(iface.getGateway())) {
                    output.append("  gateway " + iface.getGateway() + "\n");
                }

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
        }
        else {
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
