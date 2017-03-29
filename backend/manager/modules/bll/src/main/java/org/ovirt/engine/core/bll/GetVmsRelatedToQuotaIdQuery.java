package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmsRelatedToQuotaIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmDao vmDao;

    @Inject
    private ImagesHandler imagesHandler;

    public GetVmsRelatedToQuotaIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = vmDao.getAllVmsRelatedToQuotaId(getParameters().getId());
        for (VM vm : vms) {
            vmHandler.updateDisksFromDb(vm);
            vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
            Collections.sort(vm.getDiskList(), new DiskByDiskAliasComparator());
            imagesHandler.fillImagesBySnapshots(vm);
        }
        getQueryReturnValue().setReturnValue(vms);
    }
}
