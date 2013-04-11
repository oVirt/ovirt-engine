package org.ovirt.engine.core.vdsbroker.vdsbroker;


import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.action.CloudInitParameters;
import org.ovirt.engine.core.common.action.CloudInitParameters.Attachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class CloudInitHandler {
    private static Log log = LogFactory.getLog(CloudInitHandler.class);

    private final CloudInitParameters params;
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

    public CloudInitHandler(CloudInitParameters cloudInitParams) {
        this.params = cloudInitParams;
        metaData = new HashMap<String, Object>();
        userData = new HashMap<String, Object>();
        files = new HashMap<String, byte[]>();
        nextFileIndex = 0;
    }


    public Map<String, byte[]> getFileData()
            throws UnsupportedEncodingException, IOException, JsonGenerationException, JsonMappingException {
        if (params != null) {
            try {
                storeHostname();
                storeAuthorizedKeys();
                storeRegenerateKeys();
                storeNetwork();
                storeTimeZone();
                storeRootPassword();
                storeUserFiles();
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Malformed input", ex);
            }
        }

        // Add other required/supplemental data
        storeExecutionParameters();

        String metaDataStr = mapToJson(metaData);
        String userDataStr = mapToYaml(userData);

        files.put("openstack/latest/meta_data.json", metaDataStr.getBytes("UTF-8"));
        files.put("openstack/latest/user_data", userDataStr.getBytes("UTF-8"));

        // mask password for log if exists
        if (metaDataStr.contains(passwordKey)) {
            String oldStr = String.format("\"%s\" : \"%s\"", passwordKey, params.getRootPassword());
            String newStr = String.format("\"%s\" : ***", passwordKey);
            metaDataStr = metaDataStr.replace(oldStr, newStr);
        }
        log.debugFormat("cloud-init meta-data:\n{0}", metaDataStr);
        log.debugFormat("cloud-init user-data:\n{0}", userDataStr);

        return files;
    }


    private void storeHostname() {
        if (!StringUtils.isEmpty(params.getHostname())) {
            metaData.put("hostname", params.getHostname());
            metaData.put("name", params.getHostname());
        }
    }

    private void storeAuthorizedKeys() {
        if (!StringUtils.isEmpty(params.getAuthorizedKeys())) {
            metaData.put("public_keys", normalizeAuthorizedKeys(params.getAuthorizedKeys()));
        }
    }

    private List<String> normalizeAuthorizedKeys(String authorizedKeys) {
        List<String> keys = new ArrayList<String>();
        for (String key : params.getAuthorizedKeys().split("(\\r?\\n|\\r)+")) {
            if (!StringUtils.isEmpty(key)) {
                keys.add(key);
            }
        }
        return keys;
    }

    private void storeRegenerateKeys() {
        if (params.getRegenerateKeys() != null && (boolean) params.getRegenerateKeys()) {
            // Create new system ssh keys
            userData.put("ssh_deletekeys", "True");
        }
    }

    private void storeNetwork() throws UnsupportedEncodingException {
        StringBuilder output = new StringBuilder();

        // As of cloud-init 0.7.1, you can't set DNS servers without also setting NICs
        if (!CollectionUtils.isEmpty(params.getDnsServers())) {
            output.append("dns-nameservers");
            for (String server : params.getDnsServers()) {
                output.append(" " + server);
            }
            output.append("\n");
        }

        if (!CollectionUtils.isEmpty(params.getDnsSearch())) {
            output.append("dns-search");
            for (String domain : params.getDnsSearch()) {
                output.append(" " + domain);
            }
            output.append("\n");
        }

        if (!CollectionUtils.isEmpty(params.getInterfaces())) {
            Map<String, VdsNetworkInterface> interfaces = params.getInterfaces();
            List<String> names = new ArrayList<String>(interfaces.keySet());
            Collections.sort(names);

            for (String name : names) {
                VdsNetworkInterface iface = interfaces.get(name);
                output.append("iface " + name + " inet "
                        + networkBootProtocolToString(iface.getBootProtocol()) + "\n");
                output.append("  address " + iface.getAddress() + "\n");
                output.append("  netmask " + iface.getSubnet() + "\n");
                if (!StringUtils.isEmpty(iface.getGateway())) {
                    output.append("  gateway " + iface.getGateway() + "\n");
                }
            }
        }

        if (!CollectionUtils.isEmpty(params.getStartOnBoot())) {
            output.append("auto");
            for (String name : params.getStartOnBoot()) {
                output.append(" " + name);
            }
            output.append("\n");
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

    private String networkBootProtocolToString(NetworkBootProtocol proto) {
        switch (proto) {
        case DHCP:
            return "dhcp";
        case STATIC_IP:
            return "static";
        case NONE:
        default:
            return "none";
        }
    }

    private void storeTimeZone() {
        if (params.getTimeZone() != null) {
            userData.put("timezone", params.getTimeZone());
        }
    }

    private void storeRootPassword() {
        if (!StringUtils.isEmpty(params.getRootPassword())) {
            // Note that this is in plain text in the config disk
            metaData.put(passwordKey, params.getRootPassword());
        }
    }

    private void storeUserFiles() throws UnsupportedEncodingException {
        if (params.getAttachments() != null) {
            for (Map.Entry<String, Attachment> entry : params.getAttachments().entrySet()) {
                Attachment attachment = entry.getValue();
                byte[] data;
                if (attachment.getAttachmentType() == Attachment.AttachmentType.BASE64) {
                    data = Base64.decodeBase64(attachment.getContent());
                } else {
                    data = attachment.getContent().getBytes("UTF-8");
                }
                storeNextFile(CloudInitFileMode.FILE, entry.getKey(), data);
            }
        }
    }


    private void storeExecutionParameters() {
        // Store defaults in meta-data and user-data that apply regardless
        // of parameters passed in from the user.

        // New instance id required for cloud-init to process data on startup
        metaData.put("uuid", UUID.randomUUID().toString());

        Map<String, String> meta = new HashMap<String, String>();
        // Local allows us to set up networking
        meta.put("dsmode", "local");
        meta.put("essential", "false");
        meta.put("role", "server");
        metaData.put("meta", meta);

        metaData.put("launch_index", "0");
        metaData.put("availability_zone", "nova");

        // Don't create ec2-user
        userData.put("user", "root");

        // Redirect log output from cloud-init execution from terminal
        Map<String, String> output = new HashMap<String, String>();
        output.put("all", ">> /var/log/cloud-init-output.log");
        userData.put("output", output);

        // Disable metadata-server-based datasources to prevent long boot times
        List<String> runcmd = new ArrayList<String>();
        runcmd.add("sed -i '/^datasource_list: /d' /etc/cloud/cloud.cfg; echo 'datasource_list: [\"NoCloud\", \"ConfigDrive\"]' >> /etc/cloud/cloud.cfg");
        userData.put("runcmd", runcmd);
    }


    private void storeNextFile(CloudInitFileMode fileMode, String destinationPath, byte[] data) {
        String contentPath = String.format("/content/%04d", nextFileIndex++);
        Map<String, String> mdEntry = new HashMap<String, String>();

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


    private String mapToJson(Map<String, Object> input)
            throws IOException, JsonGenerationException, JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();
        StringWriter writer = new StringWriter();
        JsonGenerator generator = factory.createJsonGenerator(writer);
        generator.useDefaultPrettyPrinter();
        mapper.writeValue(generator, input);
        return writer.toString();
    }

    private String mapToYaml(Map<String, Object> input) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(input);
    }
}
