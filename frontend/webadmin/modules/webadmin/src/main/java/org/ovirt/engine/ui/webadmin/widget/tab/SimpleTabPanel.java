package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabFactory;
import org.ovirt.engine.ui.common.widget.tab.TabWidgetHandler;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.TabData;

public class SimpleTabPanel extends AbstractTabPanel implements HasUiHandlers<TabWidgetHandler> {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel tabContainer;

    TabWidgetHandler uiHandlers;

    public SimpleTabPanel() {
        initWidget();
    }

    protected void initWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setTabBar(IsWidget widget) {
        tabContainer.clear();
        if (widget != null) {
            tabContainer.add(widget);
        }
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return TabFactory.createTab(tabData, this, ClientGinjectorProvider.getEventBus());
    }

    @Override
    public void setUiHandlers(TabWidgetHandler uiHandlers) {
        this.uiHandlers = uiHandlers;
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        if (uiHandlers != null) {
            uiHandlers.addTabWidget(tabWidget, index);
        }
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        if (uiHandlers != null) {
            uiHandlers.removeTabWidget(tabWidget);
        }
    }

}
