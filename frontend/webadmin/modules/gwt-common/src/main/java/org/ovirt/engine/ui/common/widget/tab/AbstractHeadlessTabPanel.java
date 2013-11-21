package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * An {@link AbstractTabPanel} whose tab widgets should be rendered outside the tab panel.
 * <p>
 * This class delegates the responsibility of adding/removing tab widgets to other classes through
 * {@link TabWidgetHandler} interface.
 */
public abstract class AbstractHeadlessTabPanel extends AbstractTabPanel {

    private TabWidgetHandler tabWidgetHandler;

    public void setTabWidgetHandler(TabWidgetHandler tabWidgetHandler) {
        this.tabWidgetHandler = tabWidgetHandler;
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        if (tabWidgetHandler != null) {
            tabWidgetHandler.addTabWidget(tabWidget, index);
        }
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        if (tabWidgetHandler != null) {
            tabWidgetHandler.removeTabWidget(tabWidget);
        }
    }

}
