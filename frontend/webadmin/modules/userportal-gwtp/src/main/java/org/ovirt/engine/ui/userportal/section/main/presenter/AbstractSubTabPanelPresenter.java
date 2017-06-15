package org.ovirt.engine.ui.userportal.section.main.presenter;

import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.gwtplatform.mvp.client.ChangeTabHandler;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.RequestTabsHandler;
import com.gwtplatform.mvp.client.TabContainerPresenter;
import com.gwtplatform.mvp.client.TabView;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Base class for sub tab panel presenters.
 *
 * @param <V>
 *            View type.
 * @param <P>
 *            Proxy type.
 */
public abstract class AbstractSubTabPanelPresenter<V extends AbstractSubTabPanelPresenter.ViewDef, P extends Proxy<?>>
    extends TabContainerPresenter<V, P> implements TabWidgetHandler {

    public interface ViewDef extends TabView, HasUiHandlers<TabWidgetHandler> {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetTabBar = new Type<>();

    public AbstractSubTabPanelPresenter(EventBus eventBus, V view, P proxy,
            Object tabContentSlot,
            Type<RequestTabsHandler> requestTabsEventType,
            Type<ChangeTabHandler> changeTabEventType, GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, tabContentSlot, requestTabsEventType, changeTabEventType, slot);
    }

    @Override
    public void addTabWidget(TabDefinition tab, int index) {
    }

    @Override
    public void removeTabWidget(TabDefinition tab) {
    }

    @Override
    public void updateTab(TabDefinition tab) {
    }

    @Override
    public void setActiveTab(TabDefinition tab) {
    }
}
