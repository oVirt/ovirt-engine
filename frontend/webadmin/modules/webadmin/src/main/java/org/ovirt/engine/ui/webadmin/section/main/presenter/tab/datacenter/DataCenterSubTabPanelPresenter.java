package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
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

public class DataCenterSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<DataCenterSubTabPanelPresenter.ViewDef, DataCenterSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<DataCenterSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    public static final NestedSlot TYPE_SetTabContent = new NestedSlot();

    @Inject
    private MainModelProvider<StoragePool, DataCenterListModel> modelProvider;

    @Inject
    public DataCenterSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            DataCenterMainSelectedItems selectedItems) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, selectedItems);
    }

    @Override
    protected void initDetailTabToModelMapping(Map<TabData, Model> mapping) {
        DataCenterListModel mainModel = modelProvider.getModel();
        mapping.put(DetailTabDataIndex.DATACENTER_STORAGE, mainModel.getStorageListModel());
        mapping.put(DetailTabDataIndex.DATACENTER_NETWORKS, mainModel.getNetworkListModel());
        mapping.put(DetailTabDataIndex.DATACENTER_ISCSI_MULTIPATHING, mainModel.getIscsiBondListModel());
        mapping.put(DetailTabDataIndex.DATACENTER_CLUSTERS, mainModel.getClusterListModel());
        mapping.put(DetailTabDataIndex.DATACENTER_QOS, mainModel.getStorageQosListModel());
        mapping.put(DetailTabDataIndex.DATACENTER_QUOTA, mainModel.getQuotaListModel());
        mapping.put(DetailTabDataIndex.DATACENTER_PERMISSIONS, mainModel.getPermissionListModel());
        mapping.put(DetailTabDataIndex.DATACENTER_EVENTS, mainModel.getEventListModel());
    }

}
