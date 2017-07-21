package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabPanel;

/**
 * Base class used to implement GWTP TabPanel widgets.
 * <p>
 * Subclasses are free to style the UI, given that they declare:
 * <ul>
 * <li>{@link #tabContentContainer} widget for displaying tab contents
 * </ul>
 */
public abstract class AbstractTabPanel extends Composite implements TabPanel, DynamicTabPanel {

    @UiField
    public Panel tabContentContainer;

    private Tab activeTab;
    private String activeTabHistoryToken;

    // This is here so sub classes don't have to implement these methods if they don't want to
    @Override
    public void removeTab(Tab tab) {
    }

    // This is here so sub classes don't have to implement these methods if they don't want to
    @Override
    public void removeTabs() {
    }

    @Override
    public void setActiveTab(Tab tab) {
        if (activeTab != null) {
            activeTab.deactivate();
        }

        if (tab != null) {
            tab.activate();
        }

        activeTab = tab;
    }

    @Override
    public void changeTab(Tab tab, TabData tabData, String historyToken) {
        tab.setText(tabData.getLabel());
        tab.setTargetHistoryToken(historyToken);
    }

    @Override
    public void setActiveTabHistoryToken(String historyToken) {
        this.activeTabHistoryToken = historyToken;
    }

    protected String getActiveTabHistoryToken() {
        return activeTabHistoryToken;
    }

    /**
     * Sets a content widget to be displayed for the active tab.
     */
    public void setTabContent(IsWidget content) {
        tabContentContainer.clear();

        if (content != null) {
            tabContentContainer.add(content);
        }
    }

    /**
     * Ensures that the specified tab is visible or hidden as it should.
     */
    public void updateTab(TabDefinition tab) {
        tab.asWidget().setVisible(tab.isAccessible());
    }

    public void setTabVisible(TabData tabData, boolean visible) {
    }

    /**
     * Adds a tab widget to this tab panel at the given position.
     */
    public abstract void addTabDefinition(Tab tab, int index);

    /**
     * Returns a new tab widget based on the given data.
     */
    protected abstract TabDefinition createNewTab(TabData tabData);

}
