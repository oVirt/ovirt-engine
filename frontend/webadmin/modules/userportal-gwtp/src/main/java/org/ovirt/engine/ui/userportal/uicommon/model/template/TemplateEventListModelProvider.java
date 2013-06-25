package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TemplateEventListModelProvider
        extends UserPortalSearchableDetailModelProvider<AuditLog, UserPortalTemplateListModel, UserPortalTemplateEventListModel> {

    @Inject
    public TemplateEventListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            UserPortalTemplateListProvider parentProvider,
            UserPortalModelResolver resolver) {
        super(eventBus, defaultConfirmPopupProvider, user,
                parentProvider, UserPortalTemplateEventListModel.class, resolver);
    }

    @Override
    protected UserPortalTemplateEventListModel createModel() {
        return new UserPortalTemplateEventListModel();
    }

}
