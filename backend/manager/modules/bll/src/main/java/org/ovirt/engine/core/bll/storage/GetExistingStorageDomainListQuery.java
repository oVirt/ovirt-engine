package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.SANState;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetExistingStorageDomainListParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainsListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.INFO)
public class GetExistingStorageDomainListQuery<P extends GetExistingStorageDomainListParameters>
        extends QueriesCommandBase<P> {
    public GetExistingStorageDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        java.util.ArrayList<StorageDomain> returnValue = new java.util.ArrayList<StorageDomain>();
        VDSReturnValue vdsReturnValue = Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        VDSCommandType.HSMGetStorageDomainsList,
                        new HSMGetStorageDomainsListVDSCommandParameters(getParameters()
                                .getVdsId(), Guid.Empty, getParameters()
                                .getStorageType(), getParameters()
                                .getStorageDomainType(), getParameters().getPath()));
        if (vdsReturnValue.getSucceeded()) {
            java.util.ArrayList<Guid> guidsFromIrs = (java.util.ArrayList<Guid>) vdsReturnValue.getReturnValue();
            java.util.HashSet<Guid> guidsFromDb = new java.util.HashSet<Guid>();
            if (guidsFromIrs.size() > 0) {
                List<StorageDomain> domainsInDb = DbFacade.getInstance().getStorageDomainDao().getAll();
                for (StorageDomain domain : domainsInDb) {
                    guidsFromDb.add(domain.getId());
                }
                for (Guid domainId : guidsFromIrs) {
                    if (!guidsFromDb.contains(domainId)) {
                        Pair<StorageDomainStatic, SANState> domainFromIrs =
                                (Pair<StorageDomainStatic, SANState>) Backend
                                        .getInstance()
                                        .getResourceManager()
                                        .RunVdsCommand(
                                                VDSCommandType.HSMGetStorageDomainInfo,
                                                new HSMGetStorageDomainInfoVDSCommandParameters(
                                                        getParameters().getVdsId(), domainId))
                                        .getReturnValue();
                        StorageDomain domain = new StorageDomain();
                        domain.setStorageStaticData(domainFromIrs.getFirst());
                        if (getParameters().getStorageFormatType() == null
                                || getParameters().getStorageFormatType() == domain.getStorageFormat()) {
                            returnValue.add(domain);
                        }
                    }
                }
            }
            getQueryReturnValue().setReturnValue(returnValue);
        }
    }
}
