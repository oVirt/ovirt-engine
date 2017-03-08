package org.ovirt.engine.ui.webadmin.widget.tab;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.NavTabs;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.widget.OvirtBreadCrumbs;
import org.ovirt.engine.ui.common.widget.tab.AbstractTab;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabFactory;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
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

    final OvirtBreadCrumbs<?, ?> breadCrumbs;

    @UiField
    Column breadCrumbsContainer;

    private final DetailTabLayout tabLayout;
    private NavTabs navTabs;
    private HandlerRegistration keyHandler;

    public SimpleTabPanel(OvirtBreadCrumbs<?, ?> breadCrumbs, DetailTabLayout tabLayout) {
        navTabs = createPatternFlyNavTabs();
        this.tabLayout = tabLayout;
        this.breadCrumbs = breadCrumbs;
        initWidget();
        tabContainer.add(navTabs);
        if (breadCrumbs != null) {
            breadCrumbsContainer.add(breadCrumbs);
        }
    }

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        TabDefinition newTab = createNewTab(tabData);
        newTab.setTargetHistoryToken(historyToken);
        newTab.setText(tabData.getLabel());
        int index = tabLayout.addGroupedTabData(tabData);
        addTabDefinition(newTab, index);
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
                if (breadCrumbs != null) {
                    if (event.getTypeInt() == Event.ONKEYDOWN) {
                        if (nativeEvent.getCharCode() == 's' || nativeEvent.getKeyCode() == KeyCodes.KEY_S) {
                            if (nativeEvent.getCtrlKey() && nativeEvent.getAltKey()) {
                                nativeEvent.preventDefault();
                                nativeEvent.stopPropagation();
                                SimpleTabPanel.this.breadCrumbs.toggleSearchWidget();
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
        if (breadCrumbs != null) {
            if (breadCrumbsContainer.getWidgetCount() == 0) {
                breadCrumbsContainer.add(breadCrumbs);
            }
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
        for(int i = 0; i < navTabs.getWidgetCount(); i++) {
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
        if (breadCrumbs != null) {
            breadCrumbs.setActiveSubTab(title);
        }
    }

    private String calculateHref(Tab tab) {
        String href = "#"; //$NON-NLS-1$
        if (tab instanceof AbstractTab) {
            href = ((AbstractTab)tab).getTargetHistoryToken();
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
    public void removeTabDefinition(Tab tab) {
        String title = tab.getText();
        String href = calculateHref(tab);
        for(int i = 0; i < navTabs.getWidgetCount(); i++) {
            IsWidget widget = navTabs.getWidget(i);
            if (widget instanceof AnchorListItem) {
                AnchorListItem item = (AnchorListItem) widget;
                if (item.getText().equals(title) && item.getHref().endsWith(href)) {
                    navTabs.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public void updateTab(TabDefinition tab) {
        super.updateTab(tab);
        String title = tab.getText();
        String href = calculateHref(tab);
        for(int i = 0; i < navTabs.getWidgetCount(); i++) {
            IsWidget widget = navTabs.getWidget(i);
            if (widget instanceof AnchorListItem) {
                AnchorListItem item = (AnchorListItem) widget;
                if (item.getText().equals(title) && item.getHref().endsWith(href)) {
                    item.setVisible(tab.isAccessible());
                    break;
                }
            }
        }
    }
}
