package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.queries.IdAndBooleanQueryParameters;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;

public class GetUnregisteredDisksFromDBQuery<P extends IdAndBooleanQueryParameters> extends QueriesCommandBase<P> {
    public GetUnregisteredDisksFromDBQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;

    @Override
    protected void executeQueryCommand() {
        List<UnregisteredDisk> unregDisksToReturnList = new ArrayList<>();
        List<UnregisteredDisk> unregisteredDisksList =
                unregisteredDisksDao.getByDiskIdAndStorageDomainId(null, getParameters().getId());
        if (getParameters().isFilterResult()) {
            for (UnregisteredDisk unregDisk : unregisteredDisksList) {
                if (unregDisk.getVms().isEmpty()) {
                    unregDisksToReturnList.add(unregDisk);
                }
            }
        } else {
            unregDisksToReturnList.addAll(unregisteredDisksList);
        }
        getQueryReturnValue().setSucceeded(true);
        getQueryReturnValue().setReturnValue(unregDisksToReturnList);
    }
}
