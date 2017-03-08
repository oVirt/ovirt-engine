package org.ovirt.engine.ui.webadmin.widget.alert;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.tooltip.OvirtPopover;
import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.HasCssName;

public class EventsListPopover extends OvirtPopover implements ToggleHandler {

    private static final String TEMPLATE = "<div class=\"drawer-pf drawer-pf-notifications-non-clickable\">" //$NON-NLS-1$
            + "<div class=\"popover-content\" style=\"padding: 0px\"></div>" //$NON-NLS-1$
            + "</div>"; //$NON-NLS-1$

    private AnchorListItem eventListButton = new AnchorListItem();
    private boolean expanded = false;
    private NotificationListWidget notificationList;

    public EventsListPopover(HasCssName iconType, String headerTitle) {
        eventListButton.addStyleName(PatternflyConstants.PF_DRAWER_TRIGGER);
        eventListButton.addStyleName(Styles.DROPDOWN);
        Anchor anchor = (Anchor) eventListButton.getWidget(0);
        anchor.addStyleName(PatternflyConstants.NAV_ITEM_ICONIC);
        anchor.addStyleName(PatternflyConstants.PF_DRAWER_TRIGGER_ICON);
        Span iconPanel = new Span();
        iconPanel.addStyleName(Styles.FONT_AWESOME_BASE);
        iconPanel.addStyleName(iconType.getCssName());
        anchor.add(iconPanel);
        setContainer(iconPanel);
        setWidget(eventListButton);
        notificationList = new NotificationListWidget(headerTitle);
        notificationList.setToggleHandler(this);
        addContent(notificationList, iconType.getCssName());
        setTrigger(Trigger.CLICK);
        setPlacement(Placement.BOTTOM);
        setAlternateTemplate(TEMPLATE);
        addHideHandler(e -> {
            if (expanded) {
                toggle();
            }
        });
    }

    public void setBadgeText(String text) {
        this.eventListButton.setBadgeText(text);
    }

    @Override
    public void toggle() {
        expanded = !expanded;
        toggleExpand(getWidget().getElement());
    }

    public void addAction(String buttonLabel, UICommand command, AuditLogActionCallback callback) {
        notificationList.addActionCallback(buttonLabel, command, callback);
    }

    public void addAllAction(String label, UICommand command, AuditLogActionCallback callback) {
        notificationList.addAllActionCallback(label, command, callback);
    }

    private native void toggleExpand(Element e) /*-{
        var popover = $wnd.jQuery(e).next();
        popover.css('left', '');
        popover.toggleClass('drawer-pf-expanded');
    }-*/;

}
