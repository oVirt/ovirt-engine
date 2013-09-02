package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.UpgradeStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class UpgradeStoragePoolVDSCommand<P extends UpgradeStoragePoolVDSCommandParameters> extends IrsBrokerCommand<P> {
    public UpgradeStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        final P params = getParameters();
        final String storagePoolId = params.getStoragePoolId().toString();
        final String targetVersion = params.getCompatibilityVersion();

        log.infoFormat("Upgrading storage pool {0} to version {1}", storagePoolId, targetVersion);
        status = getIrsProxy().upgradeStoragePool(storagePoolId, targetVersion);
        proceedProxyReturnValue();
    }

    final private static Log log = LogFactory.getLog(UpgradeStoragePoolVDSCommand.class);
}
