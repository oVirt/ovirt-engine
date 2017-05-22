package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

/* Returns a list of all templates under required base template - including the base template */
public class GetVmTemplatesByBaseTemplateIdQuery<P extends GetVmTemplateParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmTemplateDao vmTemplateDao;

    @Inject
    private VmTemplateHandler vmTemplateHandler;

    public GetVmTemplatesByBaseTemplateIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> templateList = vmTemplateDao.getTemplateVersionsForBaseTemplate(getParameters().getId());
        if (templateList != null) {
            VmTemplate baseTemplate = vmTemplateDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
            if (baseTemplate != null) {
                templateList.add(baseTemplate);
            }
            // Load VmInit and disks
            for (VmTemplate template : templateList) {
                vmHandler.updateVmInitFromDB(template, true);
                vmTemplateHandler.updateDisksFromDb(template);
            }
        }
        getQueryReturnValue().setReturnValue(templateList);
    }
}
