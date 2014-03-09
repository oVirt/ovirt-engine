package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.ReconstructMasterVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;

public class ReconstructMasterVDSCommand<P extends ReconstructMasterVDSCommandParameters> extends VdsBrokerCommand<P> {
    public ReconstructMasterVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        final Map<String, String> domains =
                StoragePoolDomainHelper.buildStoragePoolDomainsMap(getParameters().getDomainsList());

        status = getBroker().reconstructMaster(getParameters().getStoragePoolId().toString(),
            getParameters().getStoragePoolName(),
            getParameters().getMasterDomainId().toString(), domains,
            getParameters().getMasterVersion(), Config.<String> getValue(ConfigValues.LockPolicy),
            Config.<Integer> getValue(ConfigValues.LockRenewalIntervalSec),
            Config.<Integer> getValue(ConfigValues.LeaseTimeSec),
            Config.<Integer> getValue(ConfigValues.IoOpTimeoutSec),
            Config.<Integer> getValue(ConfigValues.LeaseRetries),
            getParameters().getVdsSpmId());

        proceedProxyReturnValue();
    }
}
