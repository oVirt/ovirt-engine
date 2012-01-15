package org.ovirt.engine.ui.webadmin.view;

import org.ovirt.engine.ui.webadmin.widget.tab.AbstractTabPanel;

import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabView;

/**
 * Base class for views displaying a tab panel.
 *
 * @see AbstractTabPanel
 */
public abstract class AbstractTabPanelView extends AbstractSingleSlotView implements TabView {

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        return getTabPanel().addTab(tabData, historyToken);
    }

    @Override
    public void removeTab(Tab tab) {
        getTabPanel().removeTab(tab);
    }

    @Override
    public void removeTabs() {
        getTabPanel().removeTabs();
    }

    @Override
    public void setActiveTab(Tab tab) {
        getTabPanel().setActiveTab(tab);
    }

    @Override
    protected void setContent(Widget content) {
        getTabPanel().setTabContent(content);
    }

    protected abstract AbstractTabPanel getTabPanel();

}
