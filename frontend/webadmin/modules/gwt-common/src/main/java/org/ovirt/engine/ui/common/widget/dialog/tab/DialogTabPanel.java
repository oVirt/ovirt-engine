package org.ovirt.engine.ui.common.widget.dialog.tab;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.TabContent;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.TabPanel;
import org.gwtbootstrap3.client.ui.constants.Styles;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DialogTabPanel extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, DialogTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    NavTabs navTabs;

    @UiField
    TabContent tabContent;

    @UiField
    TabPanel tabPanel;

    @UiField
    FlowPanel tabContainer;

    @UiField
    SimplePanel tabHeaderContainer;

    private TabListItem activeTab;

    private final String height;

    @UiConstructor
    public DialogTabPanel(String height) {
        this.height = height;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        removeTabClasses();
    }

    @UiChild(tagname = "header", limit = 1)
    public void setHeader(Widget widget) {
        tabHeaderContainer.getElement().getStyle().setDisplay(Display.BLOCK);
        tabHeaderContainer.setWidget(widget);
        tabHeaderContainer.addStyleName("dialog_headerSeparator"); //$NON-NLS-1$
    }

    @UiChild(tagname = "tab")
    public void addTab(final DialogTab tab) {
        navTabs.add(tab.getTabListItem());
        tab.getTabListItem().addStyleName(Styles.LIST_GROUP_ITEM);
        String tabId = "tab" + navTabs.getWidgetCount(); //$NON-NLS-1$
        tab.getTabListItem().setDataTarget(tabId);
        tab.getTabListItem().addShownHandler(event -> switchTab(event.getTab()));

        TabPane pane = new TabPane();
        FlowPanel panel = new FlowPanel();
        panel.add(tab.getContent());
        pane.add(panel);
        pane.setId(tabId);
        tabContent.add(pane);

        // Switch to first tab automatically
        if (tabContent.getWidgetCount() == 1) {
            switchTab(tab);
        }
    }

    private void removeTabClasses() {
        tabPanel.removeStyleName(Styles.TABBABLE);
        navTabs.removeStyleName(Styles.NAV);
        navTabs.removeStyleName(Styles.NAV_TABS);
    }

    public void setBarStyle(String styleName) {
        tabContainer.addStyleName(styleName);
    }

    public FlowPanel getBar() {
        return tabContainer;
    }

    public void setContentStyle(String styleName) {
        tabContent.addStyleName(styleName);
    }

    public void setNavStyle(String styleName) {
        navTabs.addStyleName(styleName);
    }

    public void switchTab(DialogTab tab) {
        switchTab(tab.getTabListItem());
    }

    public void switchTab(TabListItem tabItem) {
        boolean found = false;
        for (int i = 0; i < navTabs.getWidgetCount(); i++) {
            TabListItem currentTabItem = (TabListItem) navTabs.getWidget(i);
            if (tabItem.getText().equals(currentTabItem.getText())) {
                currentTabItem.showTab();
                TabPane tabPane = (TabPane) tabContent.getWidget(i);
                // Detach and immediately re-attach so the content gets the onAttach event so they can react to being
                // made visible. Otherwise there is no way for the content to know if they are visible or not due to
                // the visibility being controlled by the 'active' class on the tab, which doesn't trigger any GWT
                // events when added.
                tabContent.remove(tabPane);
                tabContent.insert(tabPane, i);
                ((FlowPanel) tabPane.getWidget(0)).insert(tabHeaderContainer, 0);
                tabPane.getWidget(0).setHeight(height);
                tabPane.setActive(true);
                activeTab = (TabListItem) navTabs.getWidget(i);
                found = true;
            } else {
                TabPane tabPane = (TabPane) tabContent.getWidget(i);
                ((FlowPanel) tabPane.getWidget(0)).remove(tabHeaderContainer);
                tabPane.setActive(false);
            }
        }
        // If not found, set first active.
        if (!found && navTabs.getWidgetCount() > 0) {
            ((TabListItem) navTabs.getWidget(0)).showTab();
            TabPane tabPane = (TabPane) tabContent.getWidget(0);
            ((FlowPanel) tabPane.getWidget(0)).insert(tabHeaderContainer, 0);
            tabPane.setActive(true);
        }
    }

    public TabListItem getActiveTab() {
        return activeTab;
    }

    public void setHeaderVisible(boolean visible) {
        if (visible) {
            tabHeaderContainer.getElement().getStyle().setDisplay(Display.BLOCK);
        } else {
            tabHeaderContainer.getElement().getStyle().setDisplay(Display.NONE);
        }
    }

    public List<OvirtTabListItem> getTabs() {
        List<OvirtTabListItem> tabs = new ArrayList<>();
        for (int i = 0; i < navTabs.getWidgetCount(); i++) {
            Widget tab = navTabs.getWidget(i);
            if (tab instanceof OvirtTabListItem) {
                tabs.add((OvirtTabListItem) tab);
            }
        }
        return tabs;
    }

}
