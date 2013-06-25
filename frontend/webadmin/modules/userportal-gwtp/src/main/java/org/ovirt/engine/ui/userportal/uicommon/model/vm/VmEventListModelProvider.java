package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalVmEventListModel;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmEventListModelProvider
        extends UserPortalSearchableDetailModelProvider<AuditLog, UserPortalListModel, UserPortalVmEventListModel> {

    @Inject
    public VmEventListModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            CurrentUser user,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(eventBus, defaultConfirmPopupProvider, user,
                parentModelProvider, UserPortalVmEventListModel.class, resolver);
    }

    @Override
    protected UserPortalVmEventListModel createModel() {
        return new UserPortalVmEventListModel();
    }

}
