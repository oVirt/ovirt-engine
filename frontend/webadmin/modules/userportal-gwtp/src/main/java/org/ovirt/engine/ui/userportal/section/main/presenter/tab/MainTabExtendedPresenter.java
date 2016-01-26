package org.ovirt.engine.ui.userportal.section.main.presenter.tab;

import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelInitEvent.UserPortalModelInitHandler;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.UserPortalListProvider;
import org.ovirt.engine.ui.userportal.utils.ConnectAutomaticallyManager;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ChangeTab;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.NonLeafTabContentProxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class MainTabExtendedPresenter extends TabContainerPresenter<MainTabExtendedPresenter.ViewDef, MainTabExtendedPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    public interface ProxyDef extends NonLeafTabContentProxy<MainTabExtendedPresenter> {
    }

    public interface ViewDef extends TabView {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<>();

    @ChangeTab
    public static final Type<ChangeTabHandler> TYPE_ChangeTab = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<>();

    @TabInfo(container = MainTabPanelPresenter.class, nameToken = UserPortalApplicationPlaces.extendedVirtualMachineSideTabPlace)
    static TabData getTabData() {
        return new TabDataBasic(constants.extendedMainTabLabel(), 1);
    }

    @Inject
    public MainTabExtendedPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            final ConnectAutomaticallyManager connectAutomaticallyManager,
            final UserPortalListProvider provider) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs, TYPE_ChangeTab,
                MainTabPanelPresenter.TYPE_SetTabContent);

        connectAutomaticallyManager.registerModel(provider.getModel());

        getEventBus().addHandler(UserPortalModelInitEvent.getType(), new UserPortalModelInitHandler() {
            @Override
            public void onUserPortalModelInit(UserPortalModelInitEvent event) {
                connectAutomaticallyManager.unregisterModels();
                connectAutomaticallyManager.registerModel(provider.getModel());
            }
        });
    }

}
