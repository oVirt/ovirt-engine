package org.ovirt.engine.ui.userportal.uicommon.model.template;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TemplateGeneralModelProvider extends UserPortalDetailModelProvider<UserPortalTemplateListModel, TemplateGeneralModel> {

    @Inject
    public TemplateGeneralModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            UserPortalTemplateListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(eventBus, defaultConfirmPopupProvider, parentModelProvider, TemplateGeneralModel.class, resolver);
    }

}
