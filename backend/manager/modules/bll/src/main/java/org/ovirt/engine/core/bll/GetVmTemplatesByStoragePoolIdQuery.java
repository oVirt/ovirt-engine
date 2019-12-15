package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetVmTemplatesByStoragePoolIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetVmTemplatesByStoragePoolIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> templates = vmTemplateDao.getAllForStoragePool(getParameters().getId());
        templates.forEach(this::loadVmInit);
        VmTemplate blank = vmTemplateDao.get(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        if (!templates.contains(blank)) {
            loadVmInit(blank);
            templates.add(0, blank);
        }
        setReturnValue(templates);
    }

    private void loadVmInit(VmTemplate template) {
        vmHandler.updateVmInitFromDB(template, true);
    }
}
