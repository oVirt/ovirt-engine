package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;

public class RedefineVmCheckpointsVDSCommand<P extends VmCheckpointsVDSParameters> extends VdsBrokerCommand<P> {

    public RedefineVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().redefineVmCheckpoints(
                getParameters().getVmId().toString(), createCheckpointsMap());
        proceedProxyReturnValue();
    }

    private HashMap[] createCheckpointsMap() {
        return getParameters().getCheckpoints().stream().map(checkpoint -> {
            Map<String, Object> params = new HashMap<>();
            params.put("id", checkpoint.getId().toString());
            params.put("created", checkpoint.getCreationDate().toString());
            params.put(VdsProperties.vm_disks, createDisksMap(checkpoint.getDisks()));
            return params;
        }).toArray(HashMap[]::new);
    }

    private HashMap[] createDisksMap(List<DiskImage> disks) {
        return disks.stream().map(diskImage -> {
            Map<String, String> imageParams = new HashMap<>();
            imageParams.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
            imageParams.put(VdsProperties.ImageId, diskImage.getId().toString());
            imageParams.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
            return imageParams;
        }).toArray(HashMap[]::new);
    }
}
