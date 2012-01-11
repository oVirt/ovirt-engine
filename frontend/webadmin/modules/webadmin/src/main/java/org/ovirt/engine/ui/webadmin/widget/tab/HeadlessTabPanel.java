package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class HeadlessTabPanel extends AbstractTabPanel {

    interface WidgetUiBinder extends UiBinder<Widget, HeadlessTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public interface TabWidgetHandler {

        void addTabWidget(Widget tabWidget, int index);

        void removeTabWidget(Widget tabWidget);

    }

    private TabWidgetHandler tabWidgetHandler;

    public HeadlessTabPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setTabWidgetHandler(TabWidgetHandler tabWidgetHandler) {
        this.tabWidgetHandler = tabWidgetHandler;
    }

    @Override
    public void addTabWidget(Widget tabWidget, int index) {
        if (tabWidgetHandler != null)
            tabWidgetHandler.addTabWidget(tabWidget, index);
    }

    @Override
    protected void removeTabWidget(Widget tabWidget) {
        if (tabWidgetHandler != null)
            tabWidgetHandler.removeTabWidget(tabWidget);
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return TabFactory.createTab(tabData, this);
    }

}
