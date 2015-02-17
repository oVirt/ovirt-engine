package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.Linq;

public class UserPortalTemplateEventListModel extends TemplateEventListModel {

    @Override
    protected void refreshModel() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch(VdcQueryType.GetAllAuditLogsByVMTemplateId,
                new IdQueryParameters(getEntity().getId()));

    }

    @Override
    protected void preSearchCalled(VmTemplate template) {
        // no search string for the userportal
    }

    @Override
    public void setItems(Collection<AuditLog> value) {
        List<AuditLog> list = (List<AuditLog>) value;
        if (list != null) {
            Collections.sort(list, Collections.reverseOrder(new Linq.AuditLogComparer()));
        }
        super.setItems(list);
    }
}
