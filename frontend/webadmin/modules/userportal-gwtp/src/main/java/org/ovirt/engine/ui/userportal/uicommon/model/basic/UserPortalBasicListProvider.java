package org.ovirt.engine.ui.userportal.uicommon.model.basic;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalBasicListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncInfoModel;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalBasicListProvider extends UserPortalDataBoundModelProvider<UserPortalItemModel, UserPortalBasicListModel> {

    private Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider;

    @Inject
    public UserPortalBasicListProvider(ClientGinjector ginjector,
           Provider<VncInfoPopupPresenterWidget> vncInfoPopupProvider,
            CurrentUser user) {
        super(ginjector, user);
        this.vncInfoPopupProvider = vncInfoPopupProvider;
    }

    @Override
    protected UserPortalBasicListModel createModel() {
        return new UserPortalBasicListModel();
    }

    @Override
    public AbstractModelBoundPopupPresenterWidget<? extends Model, ?> getModelPopup(UserPortalBasicListModel source,
            UICommand lastExecutedCommand,
            Model windowModel) {
        if(windowModel instanceof VncInfoModel){
            return vncInfoPopupProvider.get();
        }

        return super.getModelPopup(source, lastExecutedCommand, windowModel);
    }

}

