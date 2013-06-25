package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmAppListModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmAppListModelProvider extends UserPortalSearchableDetailModelProvider<String, UserPortalListModel, VmAppListModel> {

    @Inject
    public VmAppListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(eventBus, defaultConfirmPopupProvider, user,
                parentModelProvider, VmAppListModel.class, resolver);
    }

    @Override
    protected VmAppListModel createModel() {
        return new VmAppListModel();
    }

}
