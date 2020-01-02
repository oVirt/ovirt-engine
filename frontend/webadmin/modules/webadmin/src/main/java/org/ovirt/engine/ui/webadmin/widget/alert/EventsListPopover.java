package org.ovirt.engine.ui.webadmin.widget.alert;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.PanelGroup;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.tooltip.OvirtPopover;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.HasCssName;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

public class EventsListPopover extends OvirtPopover {

    private static final String TEMPLATE = "<div class=\"drawer-pf drawer-pf-notifications-non-clickable\">" //$NON-NLS-1$
            + "<div class=\"popover-content\"></div>" //$NON-NLS-1$
            + "</div>"; //$NON-NLS-1$

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private static final String EVENT_ANCHOR_ID = "event_notification_anchor"; // $NON-NLS-1$
    private static final String EVENT_ACCORDION = "event_notification_accordion"; // $NON-NLS-1$
    private static final String CONTENT = "event_notification_content"; // $NON-NLS-1$

    private AnchorListItem eventListButton;
    private Span eventListButtonBadge;
    private boolean expanded = false;
    private WidgetTooltip eventListButtonTooltip;

    private PanelGroup contentPanel;

    private List<NotificationListWidget> notificationWidgetList = new ArrayList<>();

    private int footerHeight = 0;

    public EventsListPopover(String title, HasCssName iconType) {
        eventListButton = new AnchorListItem();
        eventListButton.addStyleName(PatternflyConstants.PF_DRAWER_TRIGGER);
        eventListButton.addStyleName(Styles.DROPDOWN);
        eventListButton.addClickHandler(e -> {
            if (isVisible()) {
                hide();
            } else {
                show();
            }
        });

        eventListButtonTooltip = new WidgetTooltip(eventListButton);
        eventListButtonTooltip.setHtml(SafeHtmlUtils.fromSafeConstant(constants.eventsAndAlerts()));
        eventListButtonTooltip.setPlacement(Placement.BOTTOM);

        Span iconPanel = new Span();
        iconPanel.addStyleName(Styles.FONT_AWESOME_BASE);
        iconPanel.addStyleName(iconType.getCssName());

        eventListButtonBadge = new Span();
        eventListButtonBadge.setStyleName(Styles.BADGE);

        Anchor anchor = (Anchor) eventListButton.getWidget(0);
        anchor.setId(EVENT_ANCHOR_ID);
        anchor.addStyleName(PatternflyConstants.NAV_ITEM_ICONIC);
        anchor.addStyleName(PatternflyConstants.PF_DRAWER_TRIGGER_ICON);
        anchor.add(iconPanel);
        anchor.add(eventListButtonBadge);

        setContainer(iconPanel);
        setWidget(eventListButton);

        contentPanel = new PanelGroup();
        contentPanel.setId(EVENT_ACCORDION);
        contentPanel.getElement().getStyle().setOverflowY(Overflow.HIDDEN);

        FlowPanel content = new FlowPanel();
        content.add(createTitleHeader(title));
        content.add(contentPanel);
        addContent(content, CONTENT);

        setTrigger(Trigger.MANUAL);
        setPlacement(Placement.BOTTOM);
        setAlternateTemplate(TEMPLATE);
        addHideHandler(e -> {
            if (expanded) {
                toggle();
            }
        });
        addShownHandler(e -> {
            setChildWidgetHeight();
        });
    }

    private void setChildWidgetHeight() {
        int calculatedHeight = contentPanel.getOffsetHeight();
        // We assume all the title and footer heights will be the same.
        if (!notificationWidgetList.isEmpty()) {
            int widgetFooterHeight = notificationWidgetList.get(0).getFooterHeight();
            if (widgetFooterHeight > 0) {
                footerHeight = widgetFooterHeight;
            }
            calculatedHeight -= footerHeight;
            calculatedHeight -= notificationWidgetList.get(0).getHeaderTitleHeight() * notificationWidgetList.size();
            for (NotificationListWidget widget: this.notificationWidgetList) {
                widget.setContainerHeight(calculatedHeight);
            }
        }
    }

    private IsWidget createTitleHeader(String title) {
        FlowPanel header = new FlowPanel();
        header.addStyleName(PatternflyConstants.PF_DRAWER_TITLE);

        Anchor titleAnchor = new Anchor();
        titleAnchor.addStyleName(PatternflyConstants.PF_DRAWER_TOGGLE_EXPAND);
        titleAnchor.addClickHandler(e -> toggle());
        header.add(titleAnchor);

        Anchor closeAnchor = new Anchor();
        closeAnchor.addStyleName(PatternflyConstants.PF_DRAWER_CLOSE);
        closeAnchor.addStyleName(PatternflyConstants.PFICON);
        closeAnchor.addStyleName(PatternflyConstants.PFICON_CLOSE);
        closeAnchor.addClickHandler(e -> hide());
        header.add(closeAnchor);

        Heading titleHeading = new Heading(HeadingSize.H3, title);
        titleHeading.addStyleName(PatternflyConstants.CENTER_TEXT);
        header.add(titleHeading);
        return header;
    }

    public void setBadgeText(String text) {
        this.eventListButtonBadge.setText(text);
    }

    @Override
    public void toggle() {
        expanded = !expanded;
        toggleExpand(getWidget().getElement());
    }

    public void addNotificationListWidget(NotificationListWidget widget) {
        widget.setDataToggleInfo(Toggle.COLLAPSE, EVENT_ACCORDION);
        contentPanel.add(widget.content);
        notificationWidgetList.add(widget);
    }

    private native void toggleExpand(Element e) /*-{
        var popover = $wnd.jQuery(e).next();
        popover.css('left', '');
        popover.toggleClass('drawer-pf-expanded');
    }-*/;

}
