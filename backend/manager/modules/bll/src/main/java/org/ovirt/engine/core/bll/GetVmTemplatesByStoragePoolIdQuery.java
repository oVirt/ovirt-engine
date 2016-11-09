package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetVmTemplatesByStoragePoolIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetVmTemplatesByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> templateList = vmTemplateDao.getAllForStoragePool(getParameters().getId());
        // Load VmInit
        for (VmTemplate template : templateList) {
            vmHandler.updateVmInitFromDB(template, true);
        }
        VmTemplate blank = vmTemplateDao.get(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        if (!templateList.contains(blank)) {
            vmHandler.updateVmInitFromDB(blank, true);
            templateList.add(0, blank);
        }
        getQueryReturnValue().setReturnValue(templateList);
    }
}
