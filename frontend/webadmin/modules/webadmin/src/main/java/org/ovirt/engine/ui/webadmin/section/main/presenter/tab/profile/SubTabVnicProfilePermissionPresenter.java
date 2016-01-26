package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabVnicProfilePermissionPresenter
    extends AbstractSubTabVnicProfilePresenter<PermissionListModel<VnicProfileView>,
        SubTabVnicProfilePermissionPresenter.ViewDef, SubTabVnicProfilePermissionPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.vnicProfilePermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVnicProfilePermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VnicProfileView> {
    }

    @TabInfo(container = VnicProfileSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<Permission, VnicProfileListModel,
            PermissionListModel<VnicProfileView>> modelProvider) {
        return new ModelBoundTabData(constants.vnicProfilePermissionSubTabLabel(), 2,
                modelProvider);
    }

    @Inject
    public SubTabVnicProfilePermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VnicProfileMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<Permission, VnicProfileListModel,
            PermissionListModel<VnicProfileView>> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                VnicProfileSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
