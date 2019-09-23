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

        Map<String, Object> backupConfig = createBackupConfig(fromCheckpointId, toCheckpointId);
        disksListReturn = getBroker().startVmBackup(getParameters().getVmBackup().getVmId().toString(), backupConfig);
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

    private Map<String, Object> createBackupConfig(Guid fromCheckpointId, Guid toCheckpointId) {
        Map<String, Object> backupConfig = new HashMap<>();
        backupConfig.put("backup_id", getParameters().getVmBackup().getId().toString());
        backupConfig.put("disks", createDisksMap());
        backupConfig.put("from_checkpoint_id", fromCheckpointId != null ? fromCheckpointId.toString() : null);
        backupConfig.put("to_checkpoint_id", toCheckpointId != null ? toCheckpointId.toString() : null);

        return backupConfig;
    }
}
