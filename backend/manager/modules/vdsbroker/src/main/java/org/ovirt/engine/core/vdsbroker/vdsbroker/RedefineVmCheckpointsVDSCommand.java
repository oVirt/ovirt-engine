package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointIds;

public class RedefineVmCheckpointsVDSCommand<P extends VmBackupVDSParameters> extends VmBackupConfigVDSCommandBase<P> {
    VmCheckpointIds vmCheckpointIds;

    public RedefineVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmCheckpointIds = getBroker().redefineVmCheckpoints(
                getParameters().getVmBackup().getVmId().toString(), createCheckpointsMap());
        proceedProxyReturnValue();

        setReturnValue(vmCheckpointIds);
    }

    @Override
    protected String getDiskBackupMode(DiskImage diskImage) {
        // Disk backup_mode isn't needed for redefining a checkpoint
        return null;
    }

    private Collection<Map<String, Object>> createCheckpointsMap() {
        Collection<Map<String, Object>> checkpoints = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("id", getParameters().getVmBackup().getToCheckpointId().toString());
        Map<String, Object> backupConfig = createBackupConfig();
        // Set the time since epoch in seconds
        backupConfig.put("creation_time", getParameters().getVmBackup().getCreationDate().getTime() / 1000L);

        params.put("config", backupConfig);
        checkpoints.add(params);
        return checkpoints;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmCheckpointIds;
    }

    @Override
    protected Status getReturnStatus() {
        return vmCheckpointIds.getStatus();
    }
}
