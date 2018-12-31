package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.GetDisksListReturn;

public class StartVmBackupVDSCommand<P extends VmBackupVDSParameters> extends VdsBrokerCommand<P> {

    private GetDisksListReturn disksListReturn;

    public StartVmBackupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid fromCheckpointId = getParameters().getVmBackup().getFromCheckpointId();
        Guid toCheckpointId = getParameters().getVmBackup().getToCheckpointId();

        disksListReturn = getBroker().startVmBackup(
                getParameters().getVmBackup().getVmId().toString(),
                getParameters().getVmBackup().getId().toString(),
                createDisksMap(),
                fromCheckpointId != null ? fromCheckpointId.toString() : null,
                toCheckpointId != null ? toCheckpointId.toString() : null);
        proceedProxyReturnValue();
        setReturnValue(disksListReturn.getDisks());
    }


    @Override
    protected Object getReturnValueFromBroker() {
        return disksListReturn;
    }

    @Override
    protected Status getReturnStatus() {
        return disksListReturn.getStatus();
    }

    private HashMap[] createDisksMap() {
        return getParameters().getVmBackup().getDisks().stream().map(diskImage -> {
            Map<String, String> imageParams = new HashMap<>();
            imageParams.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
            imageParams.put(VdsProperties.ImageId, diskImage.getId().toString());
            imageParams.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
            return imageParams;
        }).toArray(HashMap[]::new);
    }
}
