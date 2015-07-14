package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetVmTemplateParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/* Returns a list of all templates under required base template - including the base template */
public class GetVmTemplatesByBaseTemplateIdQuery<P extends GetVmTemplateParameters>
        extends QueriesCommandBase<P> {
    public GetVmTemplatesByBaseTemplateIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> templateList =
                DbFacade.getInstance().getVmTemplateDao().getTemplateVersionsForBaseTemplate(getParameters().getId());
        if (templateList != null) {
            VmTemplate baseTemplate = DbFacade.getInstance().getVmTemplateDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
            if (baseTemplate != null) {
                templateList.add(baseTemplate);
            }
            // Load VmInit and disks
            for (VmTemplate template : templateList) {
                VmHandler.updateVmInitFromDB(template, true);
                VmTemplateHandler.updateDisksFromDb(template);
            }
        }
        getQueryReturnValue().setReturnValue(templateList);
    }
}
