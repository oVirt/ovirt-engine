package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.vdsbroker.*;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.vdscommands.*;

public class GetStorageDomainInfoVDSCommand<P extends GetStorageDomainInfoVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public GetStorageDomainInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        Pair<storage_domain_static, SANState> domainFromIrs = (Pair<storage_domain_static, SANState>) ResourceManager
                .getInstance()
                .runVdsCommand(
                        VDSCommandType.HSMGetStorageDomainInfo,
                        new HSMGetStorageDomainInfoVDSCommandParameters(getCurrentIrsProxyData().getCurrentVdsId(),
                                getParameters().getStorageDomainId())).getReturnValue();
        if (domainFromIrs != null) {
            setReturnValue(domainFromIrs.getFirst());
        }
    }
}
