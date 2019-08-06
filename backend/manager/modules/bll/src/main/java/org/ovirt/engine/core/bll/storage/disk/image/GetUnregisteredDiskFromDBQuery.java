package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.queries.GetUnregisteredEntityQueryParameters;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;

public class GetUnregisteredDiskFromDBQuery<P extends GetUnregisteredEntityQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;

    public GetUnregisteredDiskFromDBQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<UnregisteredDisk> entityList =
                unregisteredDisksDao.getByDiskIdAndStorageDomainId(getParameters().getEntityId(),
                        getParameters().getStorageDomainId());

        UnregisteredDisk unregisteredDisk = null;
        if (!entityList.isEmpty()) {
            // We should get only one entity, since we fetched the entity with a specific Storage Domain
            unregisteredDisk = entityList.get(0);
        }

        getQueryReturnValue().setReturnValue(unregisteredDisk);
    }
}
