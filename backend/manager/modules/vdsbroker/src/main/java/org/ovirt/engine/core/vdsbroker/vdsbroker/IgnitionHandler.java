package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.compat.Version;

/**
 * Ignition handler allows passing ignition configuration to ignition enabled
 * OSs, such as Fedora CoreOs or RHCOS or any other distribution with ignition systemd service
 * through the cloud-init config-2 disk. The ignition payload goes into the user_data as-is.
 * Ignition examples - https://coreos.com/ignition/docs/latest/examples.html
 *
 * @since 4.4
 */
public class IgnitionHandler {

    private final CloudInitHandler cloudInitHandler;
    private final VmInit vmInit;
    private final Version ignitionVersion;

    /**
     * Ignition handler is simply a pass-through of the custom script as payload. The custom script
     * passed to vmInit should be a valid ignition config in json format
     */
    public IgnitionHandler(VmInit vmInit, Version ignitionVersion) {
        this.vmInit = vmInit;
        this.ignitionVersion = ignitionVersion;
        this.cloudInitHandler = new CloudInitHandler(vmInit, this::handle);
    }

    public Map<String, byte[]> getFileData() throws IOException {
        return cloudInitHandler.getFileData();
    }

    /**
     * Enrich the ignition file with more data coming from vmInit
     * @return the ignition script after handling, could  be remain untouched if nothing
     * is there to be added (no other fields declared)
     */
    private String handle() {
        String customScript = this.vmInit.getCustomScript() == null || this.vmInit.getCustomScript().isEmpty() ? "{}" : vmInit.getCustomScript();
        customScript = handleIgnitionVersion(customScript);
        customScript = handleHostname(customScript);
        customScript = handleUser(customScript);
        return customScript;
    }

    /**
     * Handle the ignition version, if the version doesn't exist in the custom script
     * then add the version as available in osinfo.
     * @param customScript String of the custom script
     * @return String of the existing custom script with the ignition version depending on the osinfo
     */
     private String handleIgnitionVersion(String customScript) {
        if (!ignitionVersionExists(customScript)) {
            JsonReader reader = Json.createReader(new StringReader(customScript));
            JsonObject ignitionJson = reader.readObject();
            JsonObject versionIgnition = ignitionSnippet(ignitionJson);
            JsonObjectBuilder builder = Json.createObjectBuilder();
            versionIgnition.forEach(builder::add);
            keyAdderWithSkip(builder, ignitionJson, "ignition");
            customScript = builder.build().toString();
        }
        return customScript;
     }

    /**
     * Creates a json snippet to ignition version in ignition format
     * @return JsonObject of the ignition version in ignition script format
     */
    private JsonObject ignitionSnippet(JsonObject ignitionJson) {
        JsonObject ignition = ignitionJson.getJsonObject("ignition");
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (ignition != null) {
            ignition.forEach(builder::add);
        }
        builder.add("version", ignitionVersion == null ? "" : ignitionVersion.toString());
        return Json.createObjectBuilder()
                .add("ignition", builder).build();
    }

    /**
     * Check whether we have the ignition version configured in the custom script.
     * @param customScript String of the custom script
     * @return true if the ignition version is configured in custom script, false otherwise
     */
     private boolean ignitionVersionExists(String customScript) {
        JsonObject reader = Json.createReader(new StringReader(customScript)).readObject();
        if (reader.containsKey("ignition")) {
            return reader.getJsonObject("ignition").containsKey("version");
        }
        return false;
     }

    /**
     * Handle the hostname if given in the UI, if the hostname exists in the custom script
     * then the UI will be disregarded.
     * @param customScript String of the custom script
     * @return String of the existing custom script with the UI hostname configuration
     */
    private String handleHostname(String customScript) {
        if (StringUtils.isNotEmpty(vmInit.getHostname()) && !hostnameExists(customScript)) {
            JsonReader reader = Json.createReader(new StringReader(customScript));
            JsonObject ignitionJson = reader.readObject();
            JsonObject hostnameIgnition = hostnameIgnitionSnippet();
            JsonObjectBuilder builder;
            if (ignitionJson.containsKey("storage") && ignitionJson.getJsonObject("storage").containsKey("files")) { // need to append the users
                builder = merger("storage", "files", ignitionJson, hostnameIgnition);
                keyAdderWithSkip(builder, ignitionJson, "storage");
                keyAdderWithSkip(builder, hostnameIgnition, "storage");
            } else {
                builder = Json.createObjectBuilder();
                ignitionJson.forEach(builder::add);
                hostnameIgnition.forEach(builder::add);
            }
            customScript = builder.build().toString();
        }
        return customScript;
    }

    /**
     * Adding each key, value of the JsonObject to JsonObjectBuilder, skipping one key, value
     * @param builder JsonObjectBuilder given to add the keys into
     * @param input JsonObject with the keys to add
     * @param keyToSkip string with the key we wish to skip
     */
    private void keyAdderWithSkip(JsonObjectBuilder builder, JsonObject input, String keyToSkip) {
        input.forEach((key, value) -> {
            if (!key.equals(keyToSkip)) {
                builder.add(key, value);
            }
        });
    }

    /**
     * Check whether we have the same hostname configured twice, both from UI and custom script.
     * @param customScript String of the custom script
     * @return true if the hostname is configured in custom script, false otherwise
     */
    private boolean hostnameExists(String customScript) {
        JsonObject reader = Json.createReader(new StringReader(customScript)).readObject();
        if (reader.containsKey("storage")) {
            if (reader.getJsonObject("storage").containsKey("files")) {
                JsonArray hostname = reader.getJsonObject("storage").getJsonArray("files");
                return hostname.toString().trim().contains("\"path\":\"/etc/hostname\"");
            }
        }
        return false;
    }

    /**
     * Creates a json snippet to add hostname file in ignition format
     * @return JsonObject of hostname in ignition script format
     */
    private JsonObject hostnameIgnitionSnippet() {
        return Json.createObjectBuilder()
                .add("storage", Json.createObjectBuilder()
                        .add("files", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add("filesystem", "root")
                                        .add("path", "/etc/hostname")
                                        .add("mode", 420)
                                        .add("contents", Json.createObjectBuilder()
                                                .add("source", "data:," + vmInit.getHostname())
                                        )
                                )
                        )
                ).build();
    }

    /**
     * Creates JsonObjectBuilder combining the inner array from the custom script
     * and from the UI.
     * @param upperProp the outer string name of the property in the JSON file
     * @param innerProp the inner string name of the property in the JSON file(key for the array)
     * @param customScript the JsonObject of the custom script given
     * @param webInput the JsonObject of the user's UI settings
     * @return JsonObjectBuilder containing both inputs files
     **/
    private JsonObjectBuilder merger(String upperProp, String innerProp, JsonObject customScript, JsonObject webInput) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonObject ignitionScript = customScript.getJsonObject(upperProp);
        JsonObject input = webInput.getJsonObject(upperProp);
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (JsonValue val: ignitionScript.getJsonArray(innerProp)) {
            array.add(val);
        }
        array.add(input.getJsonArray(innerProp).getJsonObject(0)); // Only one can be added in the UI
        keyAdderWithSkip(builder, ignitionScript, innerProp);
        keyAdderWithSkip(builder, input, innerProp);
        builder.add(innerProp, array.build());
        return Json.createObjectBuilder().add(upperProp, builder);
    }

    /**
     * Handle the user if given in the UI, if the same user exists both in UI and the custom script
     * then the UI will be disregarded.
     * @param customScript String of the custom script
     * @return String of the existing custom script with the UI user configuration
     */
    private String handleUser(String customScript) {
        if (StringUtils.isNotEmpty(vmInit.getUserName()) && !userExists(customScript)) {
            JsonReader reader = Json.createReader(new StringReader(customScript));
            JsonObject ignitionJson = reader.readObject();
            JsonObject userIgnition = userIgnitionSnippet();
            JsonObjectBuilder builder;
            if (customScript.contains("\"users\"")) { // need to append the users
                builder = merger("passwd", "users", ignitionJson, userIgnition);
                keyAdderWithSkip(builder, ignitionJson, "passwd");
                keyAdderWithSkip(builder, userIgnition, "passwd");
            } else {
                builder = Json.createObjectBuilder();
                ignitionJson.forEach(builder::add);
                userIgnition.forEach(builder::add);
            }
            customScript = builder.build().toString();
        }
        return customScript;
    }

    /**
     * Check whether we have the same user configured twice, both from UI and custom script.
     * @param customScript String of the custom script
     * @return true if the same user is configured, false otherwise
     */
    private boolean userExists(String customScript) {
        JsonObject reader = Json.createReader(new StringReader(customScript)).readObject();
        if (reader.containsKey("passwd")) {
            if (reader.getJsonObject("passwd").containsKey("users")) {
                for(JsonValue user: reader.getJsonObject("passwd").getJsonArray("users")){
                    return user.toString().replace("\"", "").contains(vmInit.getUserName());
                }
            }
        }
        return false;
    }

    /**
     * Creates a json snippet to add user file in ignition format
     * @return JsonObject of user in ignition script format
     */
    private JsonObject userIgnitionSnippet() {
        JsonObjectBuilder userSnippet = Json.createObjectBuilder()
            .add("name", vmInit.getUserName())
            .add("passwordHash", vmInit.getRootPassword());
        if (vmInit.getAuthorizedKeys() != null && !vmInit.getAuthorizedKeys().isEmpty()) {
            JsonArrayBuilder keys = Json.createArrayBuilder();
            // adding ssh keys
            for(String key: vmInit.getAuthorizedKeys().split("(\\r?\\n|\\r)+")){
                if (!StringUtils.isEmpty(key)) {
                    keys.add(key);
                }
            }
            userSnippet.add("sshAuthorizedKeys", keys);
        }

        return Json.createObjectBuilder()
                .add("passwd", Json.createObjectBuilder()
                    .add("users", Json.createArrayBuilder()
                    .add(userSnippet)
                    )
                ).build();
    }
}
