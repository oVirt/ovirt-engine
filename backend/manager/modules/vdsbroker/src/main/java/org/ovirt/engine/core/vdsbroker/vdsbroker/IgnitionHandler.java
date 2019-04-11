package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.IOException;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmInit;

/**
 * Ignition handler allows passing ignition configuration to ignition enabled
 * OSs, such as Fedora CoreOs or RHCOS or any other distribution with ignition systemd service
 * through the cloud-init config-2 disk. The ignition payload goes into the user_data as-is.
 * Ignition examples - https://coreos.com/ignition/docs/latest/examples.html
 *
 * From 4.4
 */
public class IgnitionHandler {

    private final CloudInitHandler cloudInitHandler;

    /**
     * Ignition handler is simply a pass-through of the custom script as payload. The custom script
     * passed to vmInit should be a valid ignition config in json format
     */
    public IgnitionHandler(VmInit vmInit) {
        this.cloudInitHandler = new CloudInitHandler(vmInit, () -> vmInit.getCustomScript());
    }

    public Map<String, byte[]> getFileData() throws IOException {
        return cloudInitHandler.getFileData();
    }
}
