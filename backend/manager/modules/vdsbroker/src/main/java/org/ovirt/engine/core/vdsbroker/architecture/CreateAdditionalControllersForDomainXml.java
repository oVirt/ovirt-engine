package org.ovirt.engine.core.vdsbroker.architecture;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.utils.StringMapUtils;
import org.ovirt.engine.core.utils.archstrategy.ArchCommand;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class CreateAdditionalControllersForDomainXml implements ArchCommand {

    private List<VmDevice> devices;

    public CreateAdditionalControllersForDomainXml(List<VmDevice> devices) {
        this.devices = devices;
    }

    @Override
    public void runForX86_64() {
        // There are not any additional controllers necessary for the x86_64 guests to work properly
    }

    @Override
    public void runForPPC64() {
        // Creates one SPAPR VSCSI controller, which is needed by the virtual
        // SCSI CD-ROM and SPAPR_VSCSI disks on POWER guests
        boolean dbSpaprVscsiControllerExist = devices.stream()
                .filter(d -> d.getType().equals(VmDeviceGeneralType.CONTROLLER))
                .filter(d -> d.getDevice().equals(VdsProperties.Scsi))
                .anyMatch(d -> {
                    Map<String, String> addressMap = StringMapUtils.string2Map(d.getAddress());
                    return VdsProperties.spapr_vio.equals(addressMap.get("type"));
                });

        if (dbSpaprVscsiControllerExist) {
            return;
        }

        VmDevice dbSpaprVscsiController = new VmDevice(
                null,
                VmDeviceGeneralType.CONTROLLER,
                VdsProperties.Scsi,
                Collections.singletonMap(VdsProperties.Type, VdsProperties.spapr_vio).toString(),
                Collections.singletonMap(VdsProperties.Index, "0"),
                false,
                true,
                false,
                "",
                null,
                null,
                null);

        devices.add(dbSpaprVscsiController);
    }

    @Override
    public void runForS390X() {
        // For now same as on x86
        runForX86_64();
    }
}
