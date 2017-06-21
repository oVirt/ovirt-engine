package org.ovirt.engine.ui.webadmin.section.main.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.constants.Attributes;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractHeaderView;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.common.widget.PatternflyStyles;
import org.ovirt.engine.ui.common.widget.action.ActionAnchorListItem;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationDynamicMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.alert.ActionWidget;
import org.ovirt.engine.ui.webadmin.widget.alert.EventsListPopover;
import org.ovirt.engine.ui.webadmin.widget.alert.NotificationListWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;

public class HeaderView extends AbstractHeaderView implements HeaderPresenterWidget.ViewDef {

    private static final String GROUP_NAME = "group_name"; //$NON-NLS-1$
    private static final String SECONDARY_POST_FIX = "-secondary"; //$NON-NLS-1$
    private static final String ID = "id"; //$NON-NLS-1$

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HeaderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @WithElementId
    AnchorListItem configureLink;

    @UiField
    @WithElementId
    AnchorListItem bookmarks;

    @UiField
    @WithElementId
    AnchorButton help;

    @UiField
    @WithElementId
    AnchorButton settings;

    @UiField
    @WithElementId
    AnchorListItem tasks;

    @UiField
    @WithElementId
    AnchorListItem tags;

    @UiField (provided=true)
    @WithElementId
    EventsListPopover events;

    @UiField
    ListGroup mainNavbarNavContainer;

    @UiField
    Navbar mainNavBar;

    private NotificationListWidget eventsWidget;
    private NotificationListWidget alertsWidget;
    ActionAnchorListItem alertDismissAction;

    @Inject
    public HeaderView(ApplicationDynamicMessages dynamicMessages) {
        this.configureLink = new AnchorListItem(constants.configureLinkLabel());
        this.logoutLink = new AnchorListItem(constants.logoutLinkLabel());
        this.optionsLink = new AnchorListItem(constants.optionsLinkLabel());
        this.aboutLink = new AnchorListItem(constants.aboutLinkLabel());
        this.guideLink = new AnchorListItem(dynamicMessages.guideLinkLabel());
        events = new EventsListPopover(constants.notificationDrawer(), IconType.BELL);
        alertsWidget = new NotificationListWidget(constants.alertsEventFooter());
        alertsWidget.setStartCollapse(false);
        events.addNotificationListWidget(alertsWidget);
        eventsWidget = new NotificationListWidget(constants.eventsEventFooter());
        eventsWidget.setStartCollapse(true);
        events.addNotificationListWidget(eventsWidget);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        this.logoLink.setHref(FrontendUrlUtils.getWelcomePageLink(GWT.getModuleBaseURL()));

        mainNavBar.removeStyleName(PatternflyStyles.NAVBAR_DEFAULT);

        AnchorElement.as(help.getElement()).addClassName(NAV_ITEM_ICONIC);
        AnchorElement.as(settings.getElement()).addClassName(NAV_ITEM_ICONIC);
        tasks.getWidget(0).addStyleName(NAV_ITEM_ICONIC);
        tags.getWidget(0).addStyleName(NAV_ITEM_ICONIC);
        bookmarks.getWidget(0).addStyleName(NAV_ITEM_ICONIC);
        setUserIcon();
    }

    @Override
    public HasClickHandlers getConfigureLink() {
        return configureLink;
    }

    @Override
    public HasClickHandlers getTagsLink() {
        return tags;
    }

    @Override
    public void addTabGroup(String title, int index, HasCssName icon) {
        ListGroupItem group = new ListGroupItem();
        group.addStyleName(PatternflyStyles.SECONDARY_NAV_ITEM);
        String id = title.toLowerCase().replaceAll(" ", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        group.getElement().setAttribute(Attributes.DATA_TARGET, "#" //$NON-NLS-1$
                + id + SECONDARY_POST_FIX);
        group.getElement().setAttribute(GROUP_NAME, title);

        // Title
        group.add(createTextAnchor(TabDefinition.TAB_ID_PREFIX + id, title, icon));

        // Secondary menu
        group.add(createSecondaryMenu(title));

        insertOrAppendNavWidget(index, group);
    }

    private void insertOrAppendNavWidget(int index, ListGroupItem group) {
        // Insert into nav bar
        if (index >= mainNavbarNavContainer.getWidgetCount()) {
            mainNavbarNavContainer.add(group);
        } else {
            mainNavbarNavContainer.insert(group, index);
        }
    }

    private FlowPanel createSecondaryMenu(String title) {
        FlowPanel secondaryContainer = new FlowPanel();
        secondaryContainer.getElement().setAttribute(ID, title.toLowerCase() + SECONDARY_POST_FIX);
        secondaryContainer.addStyleName(PatternflyStyles.NAV_SECONDARY_NAV);
        FlowPanel secondaryHeader = new FlowPanel();
        secondaryHeader.addStyleName(PatternflyStyles.NAV_ITEM_HEADER);
        Anchor headerAnchor = new Anchor();
        headerAnchor.getElement().setAttribute(Attributes.DATA_TOGGLE, PatternflyStyles.NAV_COLLAPSE_SECONDARY_NAV);
        headerAnchor.getElement().setClassName(PatternflyStyles.SECONDARY_COLLAPSE_TOGGLE);
        Span subMenuHeaderLabel = new Span();
        subMenuHeaderLabel.setText(title);
        secondaryHeader.add(headerAnchor);
        secondaryHeader.add(subMenuHeaderLabel);
        secondaryContainer.add(secondaryHeader);
        secondaryContainer.add(new ListGroup());
        return secondaryContainer;
    }

    private Anchor createTextAnchor(String id, String title) {
        return createTextAnchor(id, title, null);
    }

    private Anchor createTextAnchor(String id, String title, HasCssName icon) {
        Anchor titleAnchor = new Anchor();
        if (icon != null) {
            Span iconSpan = new Span();
            if (icon instanceof IconType) {
                iconSpan.addStyleName(Styles.FONT_AWESOME_BASE);
            } else if (icon instanceof PatternflyIconType) {
                iconSpan.addStyleName(PatternflyIconType.PF_BASE.getCssName());
            }
            iconSpan.addStyleName(icon.getCssName());
            titleAnchor.add(iconSpan);
        }
        Span titleSpan = new Span();
        titleSpan.addStyleName(PatternflyStyles.LIST_GROUP_ITEM_VALUE);
        titleSpan.setText(title);
        titleAnchor.add(titleSpan);
        titleAnchor.setId(id);
        return titleAnchor;
    }

    @Override
    public void addTab(String title, int index, String id, String href, String groupTitle, int groupIndex, HasCssName icon) {
        if (groupTitle == null) {
            ListGroupItem item = new ListGroupItem();
            item.getElement().setAttribute(GROUP_NAME, title);
            // Not part of a group, so it needs an href and title.
            Anchor titleAnchor = createTextAnchor(id, title, icon);
            titleAnchor.setHref(href);
            item.add(titleAnchor);

            insertOrAppendNavWidget(index, item);
        } else {
            ensureGroupIsInserted(groupTitle, groupIndex, icon);
            for (int i = 0; i < mainNavbarNavContainer.getWidgetCount(); i++) {
                if (mainNavbarNavContainer.getWidget(i) instanceof ListGroupItem) {
                    ListGroupItem group = (ListGroupItem) mainNavbarNavContainer.getWidget(i);
                    String groupName = group.getElement().getAttribute(GROUP_NAME);
                    if (groupName != null && groupName.equals(groupTitle)) {
                        FlowPanel groupContainer = (FlowPanel) group.getWidget(2);
                        ListGroup listGroup = (ListGroup) groupContainer.getWidget(1);
                        ListGroupItem item = new ListGroupItem();
                        Anchor itemAnchor = createTextAnchor(id, title);
                        itemAnchor.setHref(href);
                        item.add(itemAnchor);
                        listGroup.insert(item, Math.min(index, listGroup.getWidgetCount() - 1 >= 0
                                ? listGroup.getWidgetCount() - 1 : 0));
                    }
                }
            }
        }
    }

    private void ensureGroupIsInserted(String groupTitle, int groupIndex, HasCssName icon) {
        boolean groupFound = false;
        for (int i = 0; i < mainNavbarNavContainer.getWidgetCount(); i++) {
            ListGroupItem group = (ListGroupItem) mainNavbarNavContainer.getWidget(i);
            String groupName = group.getElement().getAttribute(GROUP_NAME);
            if (groupName != null && groupName.equals(groupTitle)) {
                groupFound = true;
                break;
            }
        }
        if (!groupFound) {
            addTabGroup(groupTitle, groupIndex, icon);
        }
    }

    @Override
    public void removeTab(String title, String href) {
        for (int i = 0; i < mainNavbarNavContainer.getWidgetCount(); i++) {
            if (mainNavbarNavContainer.getWidget(i) instanceof ListGroupItem) {
                ListGroupItem group = (ListGroupItem) mainNavbarNavContainer.getWidget(i);
                String groupName = group.getElement().getAttribute(GROUP_NAME);
                if (title != null && title.equals(groupName)) {
                    mainNavbarNavContainer.remove(group);
                } else if (title != null && group.getWidgetCount() > 2) {
                    FlowPanel groupContainer = (FlowPanel) group.getWidget(2);
                    if (removeSubMenu(title, (ListGroup) groupContainer.getWidget(1))) {
                        break;
                    }
                }
            }
        }
    }

    private boolean removeSubMenu(String title, ListGroup container) {
        for (int i = 0; i < container.getWidgetCount(); i++) {
            if (container.getWidget(i) instanceof ListGroupItem) {
                ListGroupItem group = (ListGroupItem) container.getWidget(i);
                String groupName = group.getElement().getAttribute(GROUP_NAME);
                if (title.equals(groupName)) {
                    mainNavbarNavContainer.remove(group);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void updateTab(String title, String href, boolean accessible) {
        for (int i = 0; i < mainNavbarNavContainer.getWidgetCount(); i++) {
            if (mainNavbarNavContainer.getWidget(i) instanceof ListGroupItem) {
                ListGroupItem group = (ListGroupItem) mainNavbarNavContainer.getWidget(i);
                String groupName = group.getElement().getAttribute(GROUP_NAME);
                if (title != null && title.equals(groupName)) {
                    group.setVisible(accessible);
                } else if (title != null && group.getWidgetCount() > 2) {
                    FlowPanel groupContainer = (FlowPanel) group.getWidget(2);
                    if (updateSubMenu(title, (ListGroup) groupContainer.getWidget(1), accessible)) {
                        break;
                    }
                }
            }
        }
    }

    private boolean updateSubMenu(String title, ListGroup container, boolean accessible) {
        for (int i = 0; i < container.getWidgetCount(); i++) {
            if (container.getWidget(i) instanceof ListGroupItem) {
                ListGroupItem group = (ListGroupItem) container.getWidget(i);
                String groupName = group.getElement().getAttribute(GROUP_NAME);
                if (title != null && title.equals(groupName)) {
                    group.setVisible(accessible);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void markActiveTab(String title, String href) {
        for (int i = 0; i < mainNavbarNavContainer.getWidgetCount(); i++) {
            if (mainNavbarNavContainer.getWidget(i) instanceof ListGroupItem) {
                ListGroupItem group = (ListGroupItem) mainNavbarNavContainer.getWidget(i);
                String groupName = group.getElement().getAttribute(GROUP_NAME);
                if (title != null) {
                    if (group.getWidgetCount() > 2) {
                        FlowPanel groupContainer = (FlowPanel) group.getWidget(2);
                        if (markSecondaryMenuActive(title, href, (ListGroup) groupContainer.getWidget(1), groupName)) {
                            group.addStyleName(Styles.ACTIVE);
                        } else {
                            group.removeStyleName(Styles.ACTIVE);
                        }
                    } else if (group.getWidgetCount() > 1) {
                        Anchor groupAnchor = (Anchor) group.getWidget(1);
                        if (groupAnchor.getHref().endsWith(href)) {
                            group.addStyleName(Styles.ACTIVE);
                        } else {
                            group.removeStyleName(Styles.ACTIVE);
                        }
                    }
                }
            }
        }
    }

    private boolean markSecondaryMenuActive(String title, String href, ListGroup secondaryMenulistGroup,
            String groupName) {
        boolean result = false;
        for (int i = 0; i < secondaryMenulistGroup.getWidgetCount(); i++) {
            if (secondaryMenulistGroup.getWidget(i) instanceof ListGroupItem) {
                ListGroupItem group = (ListGroupItem) secondaryMenulistGroup.getWidget(i);
                if (group.getWidgetCount() > 1) {
                    Anchor groupAnchor = (Anchor) group.getWidget(1);
                    if (groupAnchor.getHref().endsWith(href)) {
                        group.addStyleName(Styles.ACTIVE);
                        result = true;
                    } else {
                        group.removeStyleName(Styles.ACTIVE);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public HasData<AuditLog> getEventDropdown() {
        return eventsWidget;
    }

    @Override
    public HasData<AuditLog> getAlertDropdown() {
        return alertsWidget;
    }

    @Override
    public HasClickHandlers getTasksWidget() {
        return tasks;
    }

    @Override
    public void setRunningTaskCount(int count) {
        tasks.setBadgeText(String.valueOf(count));
    }

    @Override
    public HasClickHandlers getBookmarkLink() {
        return bookmarks;
    }

    @Override
    public ActionWidget getEventActionWidget() {
        return eventsWidget;
    }

    @Override
    public ActionWidget getAlertActionWidget() {
        return alertsWidget;
    }

    @Override
    public void setAlertCount(int count) {
        events.setBadgeText(String.valueOf(count));
    }
}
