package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class NetworkSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<NetworkSubTabPanelPresenter.ViewDef, NetworkSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<NetworkSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    public static final NestedSlot TYPE_SetTabContent = new NestedSlot();

    @Inject
    private MainModelProvider<NetworkView, NetworkListModel> modelProvider;

    @Inject
    public NetworkSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            NetworkMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        NetworkListModel mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.NETWORK_GENERAL, mainModel.getGeneralModel());
        mapping.put(DetailTabDataIndex.NETWORK_PROFILE, mainModel.getProfileListModel());
        mapping.put(DetailTabDataIndex.NETWORK_EXTERNAL_SUBNET, mainModel.getExternalSubnetListModel());
        mapping.put(DetailTabDataIndex.NETWORK_CLUSTERS, mainModel.getClusterListModel());
        mapping.put(DetailTabDataIndex.NETWORK_HOST, mainModel.getHostListModel());
        mapping.put(DetailTabDataIndex.NETWORK_VM, mainModel.getVmListModel());
        mapping.put(DetailTabDataIndex.NETWORK_TEMPLATE, mainModel.getTemplateListModel());
        mapping.put(DetailTabDataIndex.NETWORK_PERMISSION, mainModel.getPermissionListModel());
    }

}
