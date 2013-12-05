package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.ReconstructMasterVDSCommandParameters;

public class ReconstructMasterVDSCommand<P extends ReconstructMasterVDSCommandParameters> extends VdsBrokerCommand<P> {
    public ReconstructMasterVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        final Map<String, String> domains = new HashMap<String, String>();

        for (StoragePoolIsoMap domain : getParameters().getDomainsList()) {
            if (domain.getStatus() == StorageDomainStatus.Maintenance) {
                domains.put(domain.getstorage_id().toString(), "attached");
            } else {
                domains.put(domain.getstorage_id().toString(), StorageDomainStatus.Active.toString().toLowerCase());
            }
        }

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
