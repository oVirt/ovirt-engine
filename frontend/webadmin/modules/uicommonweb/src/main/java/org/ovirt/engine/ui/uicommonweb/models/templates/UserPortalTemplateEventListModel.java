package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.GetAllAuditLogsByVMTemplateNameParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class UserPortalTemplateEventListModel extends TemplateEventListModel {

    @Override
    protected void refreshModel() {
        if (getEntity() == null) {
            return;
        }

        super.SyncSearch(VdcQueryType.GetAllAuditLogsByVMTemplateName,
                new GetAllAuditLogsByVMTemplateNameParameters(getEntity().getName()));

    }

    @Override
    protected void preSearchCalled(VmTemplate template) {
        // no search string for the userportal
    }
}
