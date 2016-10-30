package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class GetTemplatesRelatedToQuotaIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private VmTemplateDao vmTemplateDao;

    public GetTemplatesRelatedToQuotaIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VmTemplate> vmTemplates = vmTemplateDao.getAllTemplatesRelatedToQuotaId(getParameters().getId());
        for (VmTemplate vmTemplate : vmTemplates) {
            VmTemplateHandler.updateDisksFromDb(vmTemplate);
        }
        getQueryReturnValue().setReturnValue(vmTemplates);
    }
}
