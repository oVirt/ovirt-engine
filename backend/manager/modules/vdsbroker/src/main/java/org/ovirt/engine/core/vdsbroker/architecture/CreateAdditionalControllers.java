package org.ovirt.engine.core.vdsbroker.architecture;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.utils.archstrategy.ArchCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class CreateAdditionalControllers implements ArchCommand {

    List<Map<String, Object>> devices;

    public CreateAdditionalControllers(List<Map<String, Object>> devices) {
        this.devices = devices;
    }

    @Override
    public void runForX86_64() {
        // There are not any additional controllers necessary for the x86_64 guests to work properly
    }

    @Override
    public void runForPPC64() {
        // This creates a SPAPR VSCSI controller, which is needed by the virtual
        // SCSI CD-ROM on POWER guests
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.Type, VmDeviceType.CONTROLLER.getName());
        struct.put(VdsProperties.Device, VdsProperties.Scsi);
        struct.put(VdsProperties.Index, "0");

        Map<String, String> spaprAddress = new HashMap<>();

        spaprAddress.put(VdsProperties.Type, VdsProperties.spapr_vio);

        struct.put(VdsProperties.Address, spaprAddress);
        devices.add(struct);
    }

    @Override
    public void runForS390X() {
        // For now same as on x86
        runForX86_64();
    }
}
