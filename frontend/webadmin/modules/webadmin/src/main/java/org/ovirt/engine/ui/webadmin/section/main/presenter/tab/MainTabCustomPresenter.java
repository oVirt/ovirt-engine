package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class MainTabCustomPresenter extends Presenter<MainTabCustomPresenter.ViewDef, MainTabCustomPresenter.ProxyDef> {

    // No need for @ProxyCodeSplit, we will provide our own proxy implementation
    public interface ProxyDef extends TabContentProxyPlace<MainTabCustomPresenter> {
    }

    public interface ViewDef extends View {

        void setContentUrl(String url);

    }

    private final String contentUrl;

    // No need for @Inject, this presenter will be created manually by MainTabCustomPresenterProvider
    public MainTabCustomPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy, String contentUrl) {
        super(eventBus, view, proxy);
        this.contentUrl = contentUrl;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected void onBind() {
        super.onBind();

        getView().setContentUrl(contentUrl);
    }

}
