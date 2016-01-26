package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileVmListModel;
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

public class SubTabVnicProfileVmPresenter
    extends AbstractSubTabVnicProfilePresenter<VnicProfileVmListModel, SubTabVnicProfileVmPresenter.ViewDef,
        SubTabVnicProfileVmPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.vnicProfileVmSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVnicProfileVmPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VnicProfileView> {
    }

    @TabInfo(container = VnicProfileSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel> modelProvider) {
        return new ModelBoundTabData(constants.vnicProfileVmSubTabLabel(), 0,
                modelProvider);
    }

    @Inject
    public SubTabVnicProfileVmPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VnicProfileMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<VM, VnicProfileListModel, VnicProfileVmListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                VnicProfileSubTabPanelPresenter.TYPE_SetTabContent);
    }
}

