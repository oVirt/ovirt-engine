package org.ovirt.engine.ui.webadmin.widget.alert;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.widget.Kebab;
import org.ovirt.engine.ui.common.widget.action.ActionButton;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DrawerNotification extends Div {

    private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
    private static final DateTimeFormat timeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_MEDIUM);

    private Kebab notificationKebab = new Kebab();

    public DrawerNotification(AuditLog model) {
        addStyleName(PatternflyConstants.PF_DRAWER_NOTIFICATION);
        notificationKebab.addStyleName(Styles.PULL_RIGHT);
        add(notificationKebab);
        notificationKebab.setVisible(false);
        Span icon = new Span();
        icon.addStyleName(PatternflyConstants.PFICON);
        icon.addStyleName(Styles.PULL_LEFT);
        setIconType(icon, model);
        add(icon);
        Span message = new Span();
        message.addStyleName(PatternflyConstants.PF_DRAWER_NOTIFICATION_MESSAGE);
        message.setHTML(SafeHtmlUtils.htmlEscape(model.getMessage()));
        add(message);
        add(createDateInfo(model));
    }

    private Div createDateInfo(AuditLog model) {
        Div outerDiv = new Div();
        outerDiv.addStyleName(PatternflyConstants.PF_DRAWER_NOTIFICATION_INFO);
        Span date = new Span();
        date.addStyleName(PatternflyConstants.PF_DATE);
        date.setHTML(dateFormat.format(model.getLogTime()));
        outerDiv.add(date);
        Span time = new Span();
        time.addStyleName(PatternflyConstants.PF_TIME);
        time.setHTML(timeFormat.format(model.getLogTime()));
        outerDiv.add(time);
        return outerDiv;
    }

    private void setIconType(Span icon, AuditLog model) {
        switch (model.getSeverity()) {
        case ALERT:
            icon.addStyleName(PatternflyConstants.PFICON_WARNING_TRIANGLE_O);
            break;
        case ERROR:
            icon.addStyleName(PatternflyConstants.PFICON_ERROR_CIRCLE_O);
            break;
        case NORMAL:
            icon.addStyleName(PatternflyConstants.PFICON_INFO);
            break;
        case WARNING:
            icon.addStyleName(PatternflyConstants.PFICON_WARNING_TRIANGLE_O);
            break;
        default:
            icon.addStyleName(PatternflyConstants.PFICON_OK);
            break;
        }
    }

    public void addActionButton(ActionButton actionButton) {
        notificationKebab.addMenuItem(actionButton);
        notificationKebab.setVisible(notificationKebab.getWidgetCount() > 0);
    }

}
