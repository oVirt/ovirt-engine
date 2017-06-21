package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;

import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;

/**
 * An {@link AbstractTabPanel} whose tab widgets should be rendered outside the tab panel.
 * <p>
 * This class delegates the responsibility of adding/removing tab widgets to other classes through
 * {@link TabWidgetHandler} interface.
 */
public abstract class AbstractHeadlessTabPanel extends AbstractTabPanel {

    private final MenuLayout menuLayout;

    private TabWidgetHandler tabWidgetHandler;

    public AbstractHeadlessTabPanel(MenuLayout menuLayout) {
        this.menuLayout = menuLayout;
    }

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        TabDefinition newTab = createNewTab(tabData);

        if (tabData instanceof GroupedTabData) {
            menuLayout.addMenuItem((GroupedTabData) tabData);
            newTab.setTargetHistoryToken(historyToken);
            newTab.setId(TabDefinition.TAB_ID_PREFIX + historyToken);
            newTab.setText(tabData.getLabel());
            addTabDefinition(newTab, menuLayout.getMenuIndex((GroupedTabData) tabData));
        } else {
            throw new RuntimeException("Adding non GroupedTabData"); // $NON-NLS-1$
        }

        updateTab(newTab);

        // Try to retain active tab by its history token
        if (getActiveTabHistoryToken() != null && getActiveTabHistoryToken().equals(historyToken)) {
            setActiveTab(newTab);
        }

        return newTab;
    }

    public void setTabWidgetHandler(TabWidgetHandler tabWidgetHandler) {
        this.tabWidgetHandler = tabWidgetHandler;
    }

    @Override
    public void addTabDefinition(Tab tab, int index) {
        if (tabWidgetHandler != null && tab instanceof TabDefinition) {
            tabWidgetHandler.addTabWidget((TabDefinition) tab, index);
        }
    }

    @Override
    public void removeTabDefinition(Tab tab) {
        if (tabWidgetHandler != null && tab instanceof TabDefinition) {
            tabWidgetHandler.removeTabWidget((TabDefinition) tab);
        }
    }

    @Override
    public void updateTab(TabDefinition tab) {
        super.updateTab(tab);
        if (tabWidgetHandler != null) {
            tabWidgetHandler.updateTab((TabDefinition) tab);
        }
    }

    @Override
    public void setActiveTab(Tab tab) {
        super.setActiveTab(tab);
        if (tabWidgetHandler != null) {
            tabWidgetHandler.setActiveTab((TabDefinition) tab);
        }
    }
}
