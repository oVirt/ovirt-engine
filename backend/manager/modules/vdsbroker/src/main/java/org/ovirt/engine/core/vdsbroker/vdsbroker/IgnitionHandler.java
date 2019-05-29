package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmInit;

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

    /**
     * Ignition handler is simply a pass-through of the custom script as payload. The custom script
     * passed to vmInit should be a valid ignition config in json format
     */
    public IgnitionHandler(VmInit vmInit) {
        this.vmInit = vmInit;
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
        String customScript = this.vmInit.getCustomScript();
        customScript = handleHostname(customScript);
        return customScript;
    }

    private String handleHostname(String customScript) {
        if (StringUtils.isNotEmpty(vmInit.getHostname())) {
            JsonReader reader = Json.createReader(new StringReader(customScript));
            JsonObject ignitionJson = reader.readObject();
            JsonObject hostnameIgnition = hostnameIgnitionSnippet();
            JsonObjectBuilder builder = Json.createObjectBuilder();
            ignitionJson.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
            hostnameIgnition.entrySet().forEach(e -> builder.add(e.getKey(), e.getValue()));
            customScript = builder.build().toString();
        }
        return customScript;
    }

    /**
     * Creates a json snippet to add hostname file in ignition format
     * @return
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
}
