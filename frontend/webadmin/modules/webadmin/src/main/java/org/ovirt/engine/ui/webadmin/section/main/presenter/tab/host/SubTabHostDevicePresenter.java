package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.hostdev.HostDeviceListModel;
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

public class SubTabHostDevicePresenter
    extends AbstractSubTabHostPresenter<HostDeviceListModel, SubTabHostDevicePresenter.ViewDef,
        SubTabHostDevicePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostDeviceSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostDevicePresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData() {
        return new GroupedTabData(constants.hostDeviceSubTabLabel(), 3);
    }

    @Inject
    public SubTabHostDevicePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, HostMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<HostDeviceView, HostListModel<Void>, HostDeviceListModel> modelProvider) {
        // No action buttons, pass null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                HostSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
