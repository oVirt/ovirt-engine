package org.ovirt.engine.ui.userportal.section.main.presenter.tab;

import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxy;

public class MainTabExtendedPresenter extends TabContainerPresenter<MainTabExtendedPresenter.ViewDef, MainTabExtendedPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends TabContentProxy<MainTabExtendedPresenter> {
    }

    public interface ViewDef extends TabView {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<RequestTabsHandler>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<RevealContentHandler<?>>();

    @TabInfo(container = MainTabPanelPresenter.class, nameToken = ApplicationPlaces.extendedVirtualMachineSideTabPlace)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedMainTabLabel(), 1);
    }

    @Inject
    public MainTabExtendedPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainTabPanelPresenter.TYPE_SetTabContent, this);
    }

}
