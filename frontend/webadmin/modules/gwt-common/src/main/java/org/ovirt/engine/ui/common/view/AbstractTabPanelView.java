package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;

import com.google.gwt.user.client.ui.IsWidget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabView;

/**
 * Base class for views displaying a tab panel.
 *
 * @see AbstractTabPanel
 */
public abstract class AbstractTabPanelView extends AbstractSingleSlotView implements TabView, DynamicTabPanel {

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
    public void changeTab(Tab tab, TabData tabData, String historyToken) {
        getTabPanel().changeTab(tab, tabData, historyToken);
    }

    @Override
    public void setActiveTabHistoryToken(String historyToken) {
        getTabPanel().setActiveTabHistoryToken(historyToken);
    }

    @Override
    protected void setContent(IsWidget content) {
        getTabPanel().setTabContent(content);
    }

    protected abstract AbstractTabPanel getTabPanel();

}
