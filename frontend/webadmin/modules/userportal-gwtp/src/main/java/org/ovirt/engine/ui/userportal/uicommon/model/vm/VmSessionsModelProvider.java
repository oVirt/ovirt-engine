package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSessionsModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmSessionsModelProvider extends UserPortalDetailModelProvider<UserPortalListModel, VmSessionsModel> {

    @Inject
    public VmSessionsModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver modelResolver) {
        super(eventBus, defaultConfirmPopupProvider, parentModelProvider, VmSessionsModel.class, modelResolver);
    }

}
