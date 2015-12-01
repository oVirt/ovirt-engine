package org.ovirt.engine.ui.common.widget.tab;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.presenter.DynamicTabContainerPresenter.DynamicTabPanel;
import org.ovirt.engine.ui.common.utils.FloatingPointHelper;
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

    // List of tabs managed by this tab panel, sorted by tab priority
    private final List<TabDefinition> tabList = new ArrayList<>();

    private Tab activeTab;
    private String activeTabHistoryToken;

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        TabDefinition newTab = createNewTab(tabData);

        int beforeIndex;
        for (beforeIndex = 0; beforeIndex < tabList.size(); ++beforeIndex) {
            TabDefinition currentTab = tabList.get(beforeIndex);

            if (FloatingPointHelper.epsCompare(newTab.getPriority(), currentTab.getPriority()) < 0) {
                break;
            }
        }

        newTab.setTargetHistoryToken(historyToken);
        newTab.setText(tabData.getLabel());
        addTabWidget(newTab.asWidget(), beforeIndex);
        tabList.add(beforeIndex, newTab);

        updateTab(newTab);

        // Try to retain active tab by its history token
        if (activeTabHistoryToken != null && activeTabHistoryToken.equals(historyToken)) {
            setActiveTab(newTab);
        }

        return newTab;
    }

    @Override
    public void removeTab(Tab tab) {
        removeTabWidget(tab.asWidget());
        tabList.remove(tab);
    }

    @Override
    public void removeTabs() {
        for (Tab tab : tabList) {
            removeTabWidget(tab.asWidget());
        }

        tabList.clear();
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

    /**
     * Adds a tab widget to this tab panel at the given position.
     */
    public abstract void addTabWidget(IsWidget tabWidget, int index);

    /**
     * Removes a tab widget from this tab panel.
     */
    public abstract void removeTabWidget(IsWidget tabWidget);

    /**
     * Returns a new tab widget based on the given data.
     */
    protected abstract TabDefinition createNewTab(TabData tabData);

}
