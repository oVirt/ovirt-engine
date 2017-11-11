package org.ovirt.engine.ui.webadmin.section.main.view;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorButton;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.Navbar;
import org.gwtbootstrap3.client.ui.NavbarBrand;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.common.widget.PatternflyStyles;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
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
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;

public class HeaderView extends AbstractView implements HeaderPresenterWidget.ViewDef {

    protected static final String NAV_ITEM_ICONIC = "nav-item-iconic"; //$NON-NLS-1$

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    interface ViewUiBinder extends UiBinder<Widget, HeaderView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HeaderView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    Anchor configureLink;

    @UiField
    @WithElementId
    AnchorListItem bookmarks;

    @UiField
    @WithElementId
    AnchorButton help;

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

    @UiField
    @WithElementId("userName")
    public AnchorButton userName;

    @UiField
    public WidgetTooltip userNameTooltip;

    @UiField
    public NavbarBrand logoLink;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem logoutLink = null;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem guideLink = null;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem aboutLink = null;

    @UiField(provided = true)
    @WithElementId
    public AnchorListItem optionsLink = null;

    @Inject
    public HeaderView(ApplicationDynamicMessages dynamicMessages) {
        configureLink = new Anchor();
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
    public HasData<AuditLog> getEventDropdown() {
        return eventsWidget.asHasData();
    }

    @Override
    public HasData<AuditLog> getAlertDropdown() {
        return alertsWidget.asHasData();
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

    protected void setUserIcon() {
        AnchorElement.as(this.userName.getElement()).addClassName(NAV_ITEM_ICONIC);
    }

    public void setUserName(String userName) {
        userNameTooltip.setText(userName);
        // Put PF user icon on the drop down instead of the FA one.
        Widget userNameWidget = this.userName.getWidget(0);
        userNameWidget.removeStyleName(Styles.FONT_AWESOME_BASE);
        userNameWidget.removeStyleName(IconType.USER.getCssName());
        userNameWidget.addStyleName(PatternflyIconType.PF_BASE.getCssName());
        userNameWidget.addStyleName(PatternflyIconType.PF_USER.getCssName());
    }

    public HasClickHandlers getLogoutLink() {
        return logoutLink;
    }

    public HasClickHandlers getAboutLink() {
        return aboutLink;
    }

    public HasClickHandlers getGuideLink() {
        return guideLink;
    }

    public HasClickHandlers getOptionsLink() {
        return optionsLink;
    }

}
