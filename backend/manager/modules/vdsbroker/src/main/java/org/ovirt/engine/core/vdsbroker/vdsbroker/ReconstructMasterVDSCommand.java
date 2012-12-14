package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.*;
import java.util.Map;
import java.util.HashMap;

public class ReconstructMasterVDSCommand<P extends ReconstructMasterVDSCommandParameters> extends VdsBrokerCommand<P> {
    public ReconstructMasterVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        final Map<String, String> domains = new HashMap<String, String>();

        for (StoragePoolIsoMap domain : getParameters().getDomainsList()) {
            if (domain.getstatus() == StorageDomainStatus.Maintenance) {
                domains.put(domain.getstorage_id().toString(), "attached");
            } else {
                domains.put(domain.getstorage_id().toString(), StorageDomainStatus.Active.toString().toLowerCase());
            }
        }

        status = getBroker().reconstructMaster(getParameters().getStoragePoolId().toString(),
            getParameters().getStoragePoolName(),
            getParameters().getMasterDomainId().toString(), domains,
            getParameters().getMasterVersion(), Config.<String> GetValue(ConfigValues.LockPolicy),
            Config.<Integer> GetValue(ConfigValues.LockRenewalIntervalSec),
            Config.<Integer> GetValue(ConfigValues.LeaseTimeSec),
            Config.<Integer> GetValue(ConfigValues.IoOpTimeoutSec),
            Config.<Integer> GetValue(ConfigValues.LeaseRetries),
            getParameters().getVdsSpmId());

        ProceedProxyReturnValue();
    }
}
