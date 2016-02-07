package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmsRelatedToQuotaIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVmsRelatedToQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = getDbFacade().getVmDao().getAllVmsRelatedToQuotaId(getParameters().getId());
        for (VM vm : vms) {
            VmHandler.updateDisksFromDb(vm);
            VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
            Collections.sort(vm.getDiskList(), new DiskByDiskAliasComparator());
            ImagesHandler.fillImagesBySnapshots(vm);
        }
        getQueryReturnValue().setReturnValue(vms);
    }
}
