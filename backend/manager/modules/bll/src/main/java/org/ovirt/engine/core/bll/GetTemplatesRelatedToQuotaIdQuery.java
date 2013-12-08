package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetTemplatesRelatedToQuotaIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetTemplatesRelatedToQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> vmTemplates =
                getDbFacade().getVmTemplateDao().getAllTemplatesRelatedToQuotaId(getParameters().getId());
        for (VmTemplate vmTemplate : vmTemplates) {
            VmTemplateHandler.updateDisksFromDb(vmTemplate);
        }
        getQueryReturnValue().setReturnValue(vmTemplates);
    }
}
