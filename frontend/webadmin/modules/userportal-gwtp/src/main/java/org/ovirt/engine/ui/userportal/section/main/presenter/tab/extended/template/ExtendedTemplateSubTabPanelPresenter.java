package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.RequestTabs;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class ExtendedTemplateSubTabPanelPresenter extends TabContainerPresenter<ExtendedTemplateSubTabPanelPresenter.ViewDef, ExtendedTemplateSubTabPanelPresenter.ProxyDef> {

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<ExtendedTemplateSubTabPanelPresenter> {
    }

    public interface ViewDef extends TabView {
    }

    @RequestTabs
    public static final Type<RequestTabsHandler> TYPE_RequestTabs = new Type<RequestTabsHandler>();

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabContent = new Type<RevealContentHandler<?>>();

    @Inject
    public ExtendedTemplateSubTabPanelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy) {
        super(eventBus, view, proxy, TYPE_SetTabContent, TYPE_RequestTabs);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, SideTabExtendedTemplatePresenter.TYPE_SetSubTabPanelContent, this);
    }
}
