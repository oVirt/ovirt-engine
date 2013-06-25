package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class PoolInterfaceListModelProvider extends UserPortalSearchableDetailModelProvider<VmNetworkInterface, UserPortalListModel, PoolInterfaceListModel> {

    @Inject
    public PoolInterfaceListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(eventBus, defaultConfirmPopupProvider, user,
                parentModelProvider, PoolInterfaceListModel.class, resolver);
    }

    @Override
    protected PoolInterfaceListModel createModel() {
        return new PoolInterfaceListModel();
    }

}
