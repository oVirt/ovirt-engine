package org.ovirt.engine.core.vdsbroker.architecture;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.utils.archstrategy.ArchCommand;

public class GetControllerIndices implements ArchCommand {

    Map<DiskInterface, Integer> controllerIndexMap;

    @Override
    public void runForX86_64() {
        controllerIndexMap = new HashMap<>();

        controllerIndexMap.put(DiskInterface.VirtIO_SCSI, 0);

        // There isn't a sPAPR VSCSI controller in the x86-64 architecture
        controllerIndexMap.put(DiskInterface.SPAPR_VSCSI, -1);
    }

    @Override
    public void runForPPC64() {
        controllerIndexMap = new HashMap<>();

        // The sPAPR VSCSI controller is the first one in ppc64 VMs
        controllerIndexMap.put(DiskInterface.SPAPR_VSCSI, 0);
        controllerIndexMap.put(DiskInterface.VirtIO_SCSI, 1);
    }

    @Override
    public void runForS390X() {
        // For now the same as x86
        runForX86_64();
    }

    public Map<DiskInterface, Integer> returnValue() {
        return controllerIndexMap;
    }
}
