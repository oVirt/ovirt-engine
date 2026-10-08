package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.queries.GetManagedBlockStorageDomainsByDriversParameters;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;

/**
 * Query to retrieve the all the managed block storage domains which use the same given drivers (if none then an empty list is returned)
 */
public class GetManagedBlockStorageDomainsByDriversQuery<P extends GetManagedBlockStorageDomainsByDriversParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;

    public GetManagedBlockStorageDomainsByDriversQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<ManagedBlockStorage> allManagedBlockDomainsList =
                managedBlockStorageDao.getManagedBlockStorageByDrivers(getParameters().getDriverOption());
        List<ManagedBlockStorage> resDomainsList = allManagedBlockDomainsList.stream()
                .filter(managedBlockStorage -> managedBlockStorage.getDriverSensitiveOptions()
                        .equals(getParameters().getDriverSensitiveOption()))
                .collect(Collectors.toList());

        getQueryReturnValue().setReturnValue(resDomainsList);
    }
}
