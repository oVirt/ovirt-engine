package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.ImagesComparerByName;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;

public class GetVmsRelatedToQuotaIdQuery<P extends GetEntitiesRelatedToQuotaIdParameters>
        extends QueriesCommandBase<P> {
    public GetVmsRelatedToQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = getDbFacade().getVmDao().getAllVmsRelatedToQuotaId(
                getParameters().getQuotaId());
        for (VM vm : vms) {
            VmHandler.updateDisksFromDb(vm);
            Collections.sort(vm.getDiskList(), new ImagesComparerByName());
            ImagesHandler.fillImagesBySnapshots(vm);
        }
        getQueryReturnValue().setReturnValue(vms);
    }
}
