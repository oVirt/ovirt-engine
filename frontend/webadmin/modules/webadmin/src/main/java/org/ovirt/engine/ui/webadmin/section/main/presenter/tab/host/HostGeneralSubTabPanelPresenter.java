package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.presenter.ScrollableTabBarPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractSubTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
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

public class HostGeneralSubTabPanelPresenter extends AbstractSubTabPanelPresenter<
    HostGeneralSubTabPanelPresenter.ViewDef, HostGeneralSubTabPanelPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<HostGeneralSubTabPanelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPanelPresenter.ViewDef, DynamicTabPanel {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<>();

    private AbstractSubTabPresenter<VDS, HostListModel<Void>, HostGeneralModel,
        ? extends AbstractSubTabPresenter.ViewDef<VDS>, ? extends TabContentProxyPlace<?>> lastPresenter;

    @Inject
    public HostGeneralSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            ScrollableTabBarPresenterWidget tabBar, HostMainTabSelectedItems selectedItems,
            SubTabHostGeneralInfoPresenter infoPresenter) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab, tabBar, selectedItems,
                HostSubTabPanelPresenter.TYPE_SetTabContent);
        lastPresenter = infoPresenter;
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData(
            DetailModelProvider<HostListModel<Void>, HostGeneralModel> modelProvider) {
        return new ModelBoundTabData(constants.hostGeneralSubTabLabel(), 0, modelProvider);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setInSlot(Object slot, PresenterWidget<?> content) {
        super.setInSlot(slot, content);
        if (content instanceof SubTabHostGeneralInfoPresenter
                || content instanceof SubTabHostGeneralSoftwarePresenter
                || content instanceof SubTabHostGeneralHardwarePresenter
                || content instanceof SubTabHostGeneralHostErrataPresenter) {
            lastPresenter = (AbstractSubTabPresenter<VDS, HostListModel<Void>, HostGeneralModel,
                    ? extends AbstractSubTabPresenter.ViewDef<VDS>, ? extends TabContentProxyPlace<?>>) content;
        }
        TabContentProxy<?> proxy = lastPresenter.getProxy();
        super.setInSlot(TYPE_SetTabContent, lastPresenter);
        getView().setActiveTab(proxy.getTab());
        getView().setActiveTabHistoryToken(proxy.getTargetHistoryToken());
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.hostMainTabPlace);
    }
}
