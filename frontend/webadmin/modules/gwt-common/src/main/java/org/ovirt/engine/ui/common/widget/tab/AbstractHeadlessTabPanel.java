package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.user.client.ui.Widget;

/**
 * An {@link AbstractTabPanel} whose tab widgets should be rendered outside the tab panel.
 * <p>
 * This class delegates the responsibility of adding/removing tab widgets to other classes through
 * {@link TabWidgetHandler} interface.
 */
public abstract class AbstractHeadlessTabPanel extends AbstractTabPanel {

    public interface TabWidgetHandler {

        void addTabWidget(Widget tabWidget, int index);

        void removeTabWidget(Widget tabWidget);

    }

    private TabWidgetHandler tabWidgetHandler;

    public void setTabWidgetHandler(TabWidgetHandler tabWidgetHandler) {
        this.tabWidgetHandler = tabWidgetHandler;
    }

    @Override
    public void addTabWidget(Widget tabWidget, int index) {
        if (tabWidgetHandler != null) {
            tabWidgetHandler.addTabWidget(tabWidget, index);
        }
    }

    @Override
    protected void removeTabWidget(Widget tabWidget) {
        if (tabWidgetHandler != null) {
            tabWidgetHandler.removeTabWidget(tabWidget);
        }
    }

}
