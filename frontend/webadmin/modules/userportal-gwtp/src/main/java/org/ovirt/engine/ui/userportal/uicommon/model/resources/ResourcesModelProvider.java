package org.ovirt.engine.ui.userportal.uicommon.model.resources;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ResourcesModelProvider extends UserPortalDataBoundModelProvider<VM, ResourcesModel> {

    @Inject
    public ResourcesModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user) {
        super(eventBus, defaultConfirmPopupProvider, user);
    }

    @Override
    protected ResourcesModel createModel() {
        return new ResourcesModel();
    }

}
