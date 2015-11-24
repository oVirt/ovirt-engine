package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
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
public class GetExistingStorageDomainListQuery<P extends GetExistingStorageDomainListParameters> extends QueriesCommandBase<P> {

    public GetExistingStorageDomainListQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        ArrayList<StorageDomain> returnValue = new ArrayList<>();
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.HSMGetStorageDomainsList,
                new HSMGetStorageDomainsListVDSCommandParameters(getParameters().getId(),
                        Guid.Empty,
                        null,
                        getParameters().getStorageDomainType(),
                        getParameters().getPath()));
        if (vdsReturnValue.getSucceeded()) {
            ArrayList<Guid> guidsFromIrs = (ArrayList<Guid>) vdsReturnValue.getReturnValue();
            HashSet<Guid> guidsFromDb = new HashSet<>();
            if (guidsFromIrs.size() > 0) {
                List<StorageDomain> domainsInDb = DbFacade.getInstance().getStorageDomainDao().getAll();
                for (StorageDomain domain : domainsInDb) {
                    guidsFromDb.add(domain.getId());
                }
                for (Guid domainId : guidsFromIrs) {
                    if (!guidsFromDb.contains(domainId)) {
                        Pair<StorageDomainStatic, Guid> domainFromIrs =
                                (Pair<StorageDomainStatic, Guid>) runVdsCommand(
                                        VDSCommandType.HSMGetStorageDomainInfo,
                                        new HSMGetStorageDomainInfoVDSCommandParameters(
                                                getParameters().getId(), domainId))
                                        .getReturnValue();
                        StorageDomain domain = new StorageDomain();
                        domain.setStorageStaticData(domainFromIrs.getFirst());
                        domain.setStoragePoolId(domainFromIrs.getSecond());
                        if (getParameters().getStorageFormatType() == null
                                || getParameters().getStorageFormatType() == domain.getStorageFormat()) {
                            if (getParameters().getStorageType() != null
                                    && domain.getStorageType().getValue() != getParameters().getStorageType().getValue()) {
                                log.warn("The storage type of domain {} has been changed from {} to {}",
                                        domain.getStorageName(),
                                        domain.getStorageType().toString(),
                                        getParameters().getStorageType().toString());
                                domain.setStorageType(getParameters().getStorageType());
                            }
                            returnValue.add(domain);
                        }
                    }
                }
            }
            getQueryReturnValue().setReturnValue(returnValue);
        }
    }
}
