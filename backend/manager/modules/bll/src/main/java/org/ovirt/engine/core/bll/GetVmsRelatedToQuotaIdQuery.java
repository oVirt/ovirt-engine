package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImagesComparerByName;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetEntitiesRelatedToQuotaIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmsRelatedToQuotaIdQuery<P extends GetEntitiesRelatedToQuotaIdParameters>
        extends QueriesCommandBase<P> {
    public GetVmsRelatedToQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vms = DbFacade.getInstance().getVmDAO().getAllVmsRelatedToQuotaId(
                getParameters().getQuotaId());
        for (VM vm : vms) {
            VmHandler.updateDisksFromDb(vm);
            java.util.Collections.sort(vm.getDiskList(), new ImagesComparerByName());
            for (DiskImage diskImage : vm.getDiskMap().values()) {
                diskImage.getSnapshots().addAll(
                        ImagesHandler.getAllImageSnapshots(diskImage.getImageId(), diskImage.getit_guid()));
            }
        }
        getQueryReturnValue().setReturnValue(vms);
    }
}
