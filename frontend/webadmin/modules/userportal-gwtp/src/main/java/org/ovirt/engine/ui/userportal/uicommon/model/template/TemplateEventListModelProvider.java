package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.inject.Inject;

public class TemplateEventListModelProvider
        extends UserPortalSearchableDetailModelProvider<AuditLog, UserPortalTemplateListModel, UserPortalTemplateEventListModel> {

    @Inject
    public TemplateEventListModelProvider(ClientGinjector ginjector,
            UserPortalTemplateListProvider parentProvider,
            UserPortalModelResolver resolver,
            CurrentUser user) {
        super(ginjector, parentProvider, UserPortalTemplateEventListModel.class, resolver, user);
    }

    @Override
    protected UserPortalTemplateEventListModel createModel() {
        return new UserPortalTemplateEventListModel();
    }

}
