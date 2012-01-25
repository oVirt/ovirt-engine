package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended;

import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SideTabExtendedTemplatePresenter extends Presenter<SideTabExtendedTemplatePresenter.ViewDef, SideTabExtendedTemplatePresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedTemplateSideTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SideTabExtendedTemplatePresenter> {
    }

    public interface ViewDef extends View {
    }

    @TabInfo(container = MainTabExtendedPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new TabDataBasic(ginjector.getApplicationConstants().extendedTemplateSideTabLabel(), 1);
    }

    @Inject
    public SideTabExtendedTemplatePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy) {
        super(eventBus, view, proxy);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainTabExtendedPresenter.TYPE_SetTabContent, this);
    }

}
