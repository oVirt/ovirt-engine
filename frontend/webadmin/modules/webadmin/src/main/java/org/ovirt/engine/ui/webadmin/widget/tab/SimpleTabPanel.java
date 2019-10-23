package org.ovirt.engine.ui.webadmin.widget.tab;

import java.util.HashMap;
import java.util.Map;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.OvirtBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractTab;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabFactory;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;

public class SimpleTabPanel extends AbstractTabPanel {

    private static final String NAV_TABS_PF = "nav-tabs-pf"; //$NON-NLS-1$

    interface WidgetUiBinder extends UiBinder<FlowPanel, SimpleTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel tabContainer;

    @UiField
    FlowPanel breadCrumbsContainer;

    @UiField
    FlowPanel mainActionPanel;

    private final OvirtBreadCrumbsPresenterWidget<?, ?> breadCrumbs;
    private final ActionPanelPresenterWidget<?, ?, ?> actionPanel;
    private final DetailTabLayout tabLayout;

    private NavTabs navTabs;
    private HandlerRegistration keyHandler;

    private final Map<TabData, Widget> actualTabWidgets = new HashMap<>();
    private final Map<TabData, String> tabHistoryTokens = new HashMap<>();

    public SimpleTabPanel(OvirtBreadCrumbsPresenterWidget<?, ?> breadCrumbs,
            ActionPanelPresenterWidget<?, ?, ?> actionPanel, DetailTabLayout tabLayout) {
        navTabs = createPatternFlyNavTabs();
        this.tabLayout = tabLayout;
        this.breadCrumbs = breadCrumbs;
        this.actionPanel = actionPanel;
        initWidget();
        tabContainer.add(navTabs);
    }

    /**
     * TODO(vs) newTab (Tab impl.) is effectively an unattached empty div element.
     * This means methods like updateTab currently have no effect. The AnchorListItem
     * widget created in addTabDefinition is the actual tab widget impl.
     */
    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        TabDefinition newTab = createNewTab(tabData);
        newTab.setTargetHistoryToken(historyToken);
        newTab.setText(tabData.getLabel());
        int index = tabLayout.addGroupedTabData(tabData);
        addTabDefinition(newTab, index);
        actualTabWidgets.put(tabData, navTabs.getWidget(index));
        tabHistoryTokens.put(tabData, historyToken);
        // Update tabs to show/hide if needed.
        updateTab(newTab);
        return newTab;
    }

    private NavTabs createPatternFlyNavTabs() {
        NavTabs pfNav = new NavTabs();
        pfNav.addStyleName(NAV_TABS_PF);
        return pfNav;
    }

    protected void initWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        keyHandler = Event.addNativePreviewHandler(event -> {
                NativeEvent nativeEvent = event.getNativeEvent();
                if (breadCrumbs != null && AbstractPopupPresenterWidget.getActivePopupCount() == 0) {
                    if (event.getTypeInt() == Event.ONKEYDOWN) {
                        if (nativeEvent.getCharCode() == 's' || nativeEvent.getKeyCode() == KeyCodes.KEY_S) {
                            if (nativeEvent.getCtrlKey() && nativeEvent.getAltKey()) {
                                nativeEvent.preventDefault();
                                nativeEvent.stopPropagation();
                                SimpleTabPanel.this.breadCrumbs.toggleSearch();
                                event.cancel();
                            }
                        } else if (!SimpleTabPanel.this.breadCrumbs.isSearchVisible() && nativeEvent.getKeyCode() == KeyCodes.KEY_DOWN) {
                            nativeEvent.preventDefault();
                            nativeEvent.stopPropagation();
                            SimpleTabPanel.this.breadCrumbs.nextEntity();
                            event.cancel();
                        } else if (!SimpleTabPanel.this.breadCrumbs.isSearchVisible() && nativeEvent.getKeyCode() == KeyCodes.KEY_UP) {
                            nativeEvent.preventDefault();
                            nativeEvent.stopPropagation();
                            SimpleTabPanel.this.breadCrumbs.previousEntity();
                            event.cancel();
                        }
                    }
                }
            }
        );
        if (breadCrumbs != null && breadCrumbsContainer.getWidgetCount() == 0) {
            breadCrumbsContainer.add(breadCrumbs);
        }
        if (actionPanel != null && mainActionPanel.getWidgetCount() == 0) {
            mainActionPanel.add(actionPanel);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        keyHandler.removeHandler();
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return TabFactory.createTab(tabData, this, ClientGinjectorProvider.getEventBus());
    }

    @Override
    public void setActiveTab(Tab tab) {
        super.setActiveTab(tab);
        String title = tab.getText();
        String href = calculateHref(tab);
        for (int i = 0; i < navTabs.getWidgetCount(); i++) {
            IsWidget widget = navTabs.getWidget(i);
            if (widget instanceof AnchorListItem) {
                AnchorListItem item = (AnchorListItem) widget;
                if (item.getText().equals(title) && item.getHref().endsWith(href)) {
                    item.addStyleName(Styles.ACTIVE);
                } else {
                    item.removeStyleName(Styles.ACTIVE);
                }
            }
        }
    }

    private String calculateHref(Tab tab) {
        String href = "#"; //$NON-NLS-1$
        if (tab instanceof AbstractTab) {
            href = ((AbstractTab) tab).getTargetHistoryToken();
        }
        return href;
    }

    @Override
    public void addTabDefinition(Tab tab, int index) {
        String href = calculateHref(tab);
        AnchorListItem item = new AnchorListItem(tab.getText());
        item.setHref(href);
        navTabs.insert(item, index);
    }

    @Override
    public void setTabVisible(TabData tabData, boolean visible) {
        Widget tabWidget = actualTabWidgets.get(tabData);
        if (tabWidget != null) {
            // update tab visibility
            tabWidget.setVisible(visible);

            // handle the case when currently active tab becomes hidden
            String activeTabHistoryToken = getActiveTabHistoryToken();
            if (!visible && activeTabHistoryToken != null &&
                    activeTabHistoryToken.equals(tabHistoryTokens.get(tabData))) {
                Scheduler.get().scheduleDeferred(() -> {
                    String href = getFirstVisibleTabHref();
                    if (href != null) {
                        String historyToken = href.substring(href.indexOf("#") + 1); //$NON-NLS-1$
                        History.newItem(historyToken);
                    }
                });
            }
        }
    }

    private String getFirstVisibleTabHref() {
        for (int i = 0; i < navTabs.getWidgetCount(); i++) {
            Widget tabWidget = navTabs.getWidget(i);
            if (tabWidget instanceof AnchorListItem && tabWidget.isVisible()) {
                return ((AnchorListItem) navTabs.getWidget(i)).getHref();
            }
        }
        return null;
    }

}
