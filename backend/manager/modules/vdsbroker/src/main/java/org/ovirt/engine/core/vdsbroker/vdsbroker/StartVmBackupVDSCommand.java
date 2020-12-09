package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmBackupInfo;

public class StartVmBackupVDSCommand<P extends VmBackupVDSParameters> extends VmBackupConfigVDSCommandBase<P> {

    private VmBackupInfo vmBackupInfo;

    public StartVmBackupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Map<String, Object> backupConfig = createBackupConfig();
        vmBackupInfo = getBroker().startVmBackup(getParameters().getVmBackup().getVmId().toString(), backupConfig);
        proceedProxyReturnValue();
        setReturnValue(vmBackupInfo);
    }

    @Override
    protected String getDiskBackupMode(DiskImage diskImage) {
        return FeatureSupported.isBackupModeAndBitmapsOperationsSupported(getVds().getClusterCompatibilityVersion()) ?
                diskImage.getBackupMode().getName() :
                null;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmBackupInfo;
    }

    @Override
    protected Status getReturnStatus() {
        return vmBackupInfo.getStatus();
    }
}
