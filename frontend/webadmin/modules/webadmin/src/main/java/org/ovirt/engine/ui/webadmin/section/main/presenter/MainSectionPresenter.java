package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.widget.AlertManager;
import org.ovirt.engine.ui.webadmin.plugin.PluginManager;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class MainSectionPresenter extends Presenter<MainSectionPresenter.ViewDef, MainSectionPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<MainSectionPresenter> {
    }

    public interface ViewDef extends View, HasUiHandlers<MainTabBarOffsetUiHandlers> {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetHeader = new Type<>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetMainContent = new Type<>();

    private final HeaderPresenterWidget header;
    private final PluginManager pluginManager;
    private final AlertManager alertManager;

    @Inject
    public MainSectionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            HeaderPresenterWidget header, PluginManager pluginManager, AlertManager alertManager) {
        super(eventBus, view, proxy, RevealType.RootLayout);
        this.header = header;
        this.pluginManager = pluginManager;
        this.alertManager = alertManager;
        getView().setUiHandlers(header);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetHeader, header);

        // Enable plugin invocation within the scope of main section
        pluginManager.enablePluginInvocation();

        // Enable alerts within the scope of main section
        alertManager.setCanShowAlerts(true);
    }

    @Override
    protected void onHide() {
        super.onHide();

        // Disable plugin invocation outside the scope of main section
        pluginManager.disablePluginInvocation();

        // Disable alerts outside the scope of main section
        alertManager.setCanShowAlerts(false);
    }

}
