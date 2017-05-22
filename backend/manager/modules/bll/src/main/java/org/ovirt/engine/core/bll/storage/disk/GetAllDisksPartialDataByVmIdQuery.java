package org.ovirt.engine.core.bll.storage.disk;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskDao;

public class GetAllDisksPartialDataByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskDao diskDao;

    public GetAllDisksPartialDataByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<Disk> allDisks =
                diskDao.getAllForVmPartialData
                        (getParameters().getId(), false, getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(allDisks);
    }
}
