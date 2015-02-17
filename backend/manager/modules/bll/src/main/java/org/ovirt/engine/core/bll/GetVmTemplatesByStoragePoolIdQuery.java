package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

import java.util.List;

public class GetVmTemplatesByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVmTemplatesByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> templateList = DbFacade.getInstance().getVmTemplateDao().getAllForStoragePool(getParameters().getId());
            // Load VmInit and disks
        for (VmTemplate template : templateList) {
            VmHandler.updateVmInitFromDB(template, true);
            VmTemplateHandler.updateDisksFromDb(template);
        }
        VmTemplate blank = DbFacade.getInstance().getVmTemplateDao()
                .get(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        if (!templateList.contains(blank)) {
            VmHandler.updateVmInitFromDB(blank, true);
            templateList.add(0, blank);
        }
        getQueryReturnValue().setReturnValue(templateList);
    }
}
