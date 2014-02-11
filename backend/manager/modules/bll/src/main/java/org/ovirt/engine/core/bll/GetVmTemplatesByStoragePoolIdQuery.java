package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVmTemplatesByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVmTemplatesByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        StoragePool pool = DbFacade.getInstance().getStoragePoolDao().get(getParameters().getId());
        List<VmTemplate> templateList = DbFacade.getInstance().getVmTemplateDao().getAllForStorageDomain(pool.getId());
            // Load VmInit and disks
        for (VmTemplate template : templateList) {
            VmHandler.updateVmInitFromDB(template, true);
            VmTemplateHandler.updateDisksFromDb(template);
        }
        VmTemplate blank = DbFacade.getInstance().getVmTemplateDao()
                .get(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        if (!templateList.contains(blank)) {
            templateList.add(0, blank);
        }
        getQueryReturnValue().setReturnValue(templateList);
    }
}
