package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.users.UserGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserGeneralView;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabUserGeneralPresenter
    extends AbstractSubTabUserPresenter<UserGeneralModel, SubTabUserGeneralView, SubTabUserGeneralPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.userGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabUserGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<DbUser> {
    }

    @TabInfo(container = UserSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<UserListModel, UserGeneralModel> modelProvider) {
        return new ModelBoundTabData(constants.userGeneralSubTabLabel(), 0, modelProvider);
    }

    @Inject
    public SubTabUserGeneralPresenter(EventBus eventBus, SubTabUserGeneralView view, ProxyDef proxy,
            PlaceManager placeManager, UserMainTabSelectedItems selectedItems,
            DetailModelProvider<UserListModel, UserGeneralModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                UserSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
