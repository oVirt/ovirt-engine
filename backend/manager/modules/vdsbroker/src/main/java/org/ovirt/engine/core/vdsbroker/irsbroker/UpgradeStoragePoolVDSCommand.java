package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.UpgradeStoragePoolVDSCommandParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeStoragePoolVDSCommand<P extends UpgradeStoragePoolVDSCommandParameters> extends IrsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(UpgradeStoragePoolVDSCommand.class);

    public UpgradeStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        final P params = getParameters();
        final String storagePoolId = params.getStoragePoolId().toString();
        final String targetVersion = params.getCompatibilityVersion();

        log.info("Upgrading storage pool '{}' to version '{}'.", storagePoolId, targetVersion);
        status = getIrsProxy().upgradeStoragePool(storagePoolId, targetVersion);
        proceedProxyReturnValue();
    }
}
