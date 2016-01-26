package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter;

import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.presenter.ScrollableTabBarPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.ModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxy;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class DataCenterQosSubTabPanelPresenter extends
    AbstractSubTabPanelPresenter<DataCenterQosSubTabPanelPresenter.ViewDef, DataCenterQosSubTabPanelPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.dataCenterQosSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<DataCenterQosSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<>();

    private Presenter<?, ?> lastPresenter;

    @Inject
    public DataCenterQosSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            ScrollableTabBarPresenterWidget tabBar, DataCenterMainTabSelectedItems selectedItems,
            SubTabDataCenterStorageQosPresenter dataCenterStorageQosPresenter) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, tabBar, selectedItems,
                DataCenterSubTabPanelPresenter.TYPE_SetTabContent);
        this.lastPresenter = dataCenterStorageQosPresenter;
    }

    @TabInfo(container = DataCenterSubTabPanelPresenter.class)
    static TabData getTabData(
            ModelProvider<DataCenterListModel> modelProvider) {
        return new ModelBoundTabData(constants.dataCenterQosSubTabLabel(), 2, modelProvider);
    }

    @Override
    public void setInSlot(Object slot, PresenterWidget<?> content) {
        super.setInSlot(slot, content);
        if (content instanceof SubTabDataCenterStorageQosPresenter
                || content instanceof SubTabDataCenterNetworkQoSPresenter
                || content instanceof SubTabDataCenterCpuQosPresenter
                || content instanceof SubTabDataCenterHostNetworkQosPresenter) {
            lastPresenter = (Presenter<?, ?>) content;
        }
        TabContentProxy<?> proxy = (TabContentProxy<?>) lastPresenter.getProxy();
        super.setInSlot(TYPE_SetTabContent, lastPresenter);
        getView().setActiveTab(proxy.getTab());
        getView().setActiveTabHistoryToken(proxy.getTargetHistoryToken());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.dataCenterMainTabPlace);
    }
}
