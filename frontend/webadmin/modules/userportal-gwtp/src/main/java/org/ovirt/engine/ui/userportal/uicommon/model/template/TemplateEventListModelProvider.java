package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class TemplateEventListModelProvider
        extends UserPortalSearchableDetailModelProvider<AuditLog, UserPortalTemplateListModel, TemplateEventListModel> {

    @Inject
    public TemplateEventListModelProvider(ClientGinjector ginjector,
            UserPortalTemplateListProvider parentProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentProvider, TemplateEventListModel.class, resolver);
    }

    @Override
    protected TemplateEventListModel createModel() {
        return new TemplateEventListModel();
    }

}
