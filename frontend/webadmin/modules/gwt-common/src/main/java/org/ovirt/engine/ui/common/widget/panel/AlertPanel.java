package org.ovirt.engine.ui.common.widget.panel;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.shared.event.AlertClosedEvent;
import org.gwtbootstrap3.client.shared.event.AlertClosedHandler;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Badge;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Renders a PatternFly alert panel. @see https://www.patternfly.org/widgets/#alerts
 */
public class AlertPanel extends Composite {

    interface ViewUiBinder extends UiBinder<IsWidget, AlertPanel> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
    }

    @UiField
    Span icon;

    @UiField
    Div messagePanel;

    @UiField
    Badge badge;

    @UiField
    Text badgeText;

    @UiField(provided = true)
    Alert alert;

    @UiField
    WidgetTooltip alertTooltip;

    private Type type;
    private ColumnSize widgetColumnSize;
    private int count = 1;
    private List<SafeHtml> messagesList = new ArrayList<>();

    private final HandlerRegistration closedHandlerReg;

    /**
     * The types of PatternFly alerts (currently 4).
     *
     * @see <a href="https://www.patternfly.org/widgets/#alerts">Patternfly Alerts</a>
     */
    public enum Type {

        DANGER(AlertType.DANGER, PatternflyConstants.PFICON_ERROR_CIRCLE_O),
        WARNING(AlertType.WARNING, PatternflyConstants.PFICON_WARNING_TRIANGLE_O),
        SUCCESS(AlertType.SUCCESS, PatternflyConstants.PFICON_OK),
        INFO(AlertType.INFO, PatternflyConstants.PFICON_INFO);

        private AlertType alertType;
        private String iconStyleName;

        Type(AlertType alertType, String iconStyleName) {
            this.alertType = alertType;
            this.iconStyleName = iconStyleName;
        }

        public static Type from(String name) {
            try {
                return Type.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Type.INFO;
            }
        }

    }

    /**
     * Create a new alert panel of type 'info'.
     */
    public AlertPanel() {
        alert = new Alert() {
            /**
             * Need to override this method to avoid an exception thrown when
             * the parent widget doesn't implement the HasWidgets interface.
             */
            @Override
            public void removeFromParent() {
                Widget parent = getParent();
                if (parent instanceof HasWidgets) {
                    ((HasWidgets) parent).remove(this);
                } else {
                    if (RootPanel.isInDetachList(this)) {
                        RootPanel.detachNow(this);
                    }
                }
            }
        };

        closedHandlerReg = alert.addClosedHandler(new AlertClosedHandler() {
            @Override
            public void onClosed(AlertClosedEvent evt) {
                ElementTooltipUtils.hideAllTooltips();
                closedHandlerReg.removeHandler();
            }
        });

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this).asWidget());
        setType(Type.INFO);
        setWidgetColumnSize(ColumnSize.SM_11);
        badge.setVisible(false);
    }

    /**
     * Create a new alert panel with a custom type and message.
     */
    public AlertPanel(Type type, SafeHtml message) {
        this();
        setType(type);
        addMessage(message);
    }

    /**
     * Create a new alert panel with a custom type and message, and a CSS class name to apply to the message.
     */
    public AlertPanel(Type type, SafeHtml message, String styleName) {
        this();
        setType(type);
        addMessage(message, styleName);
    }

    public void incCount() {
        count++;
        updateBadge();
    }

    private void updateBadge() {
        badge.setVisible(count != 1);
        badgeText.setText(String.valueOf(count));
        if (badge.isVisible()) {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            for (SafeHtml message: messagesList) {
                builder.append(message);
            }
            alertTooltip.setHtml(builder.toSafeHtml());
        }
    }

    /**
     * Clear all messages from the panel.
     */
    public void clearMessages() {
        messagePanel.clear();
    }

    /**
     * Set a List of messages into the alert panel. Apply the same CSS class names to each message.
     * Note that this clears any existing messages.
     */
    public void setMessages(List<SafeHtml> messages, String... styleNames) {
        clearMessages();
        for (SafeHtml message: messages) {
            HTMLPanel messageLabel = createMessageLabel(message);
            for (String s : styleNames) {
                messageLabel.addStyleName(s);
            }
            messagePanel.add(messageLabel);
        }
    }

    /**
     * Add a message to the alert panel. Note that this does not clear any messages already in the panel.
     */
    public void addMessage(SafeHtml message) {
        messagePanel.add(createMessageLabel(message));
    }

    private HTMLPanel createMessageLabel(SafeHtml message) {
        messagesList.add(message);
        HTMLPanel messageLabel = new HTMLPanel(message);
        return messageLabel;
    }

    /**
     * Add a message to the alert panel. Apply the CSS class name to the message.
     * Note that this does not clear any messages already in the panel.
     */
    public void addMessage(SafeHtml message, String styleName) {
        HTMLPanel messageLabel = createMessageLabel(message);
        messageLabel.addStyleName(styleName);
        messagePanel.add(messageLabel);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        getWidget().setType(type.alertType);

        // clear all other icon style names
        for (Type t : type.getDeclaringClass().getEnumConstants()) {
            icon.removeStyleName(t.iconStyleName);
        }

        icon.addStyleName(type.iconStyleName);
    }

    /**
     * Apply the given {@linkplain ColumnSize Bootstrap column size}
     * style to the alert widget, replacing one that was set previously.
     * <p>
     * Pass {@code null} to effectively remove the column size style.
     */
    public void setWidgetColumnSize(ColumnSize newColumnSize) {
        // clear existing style
        if (widgetColumnSize != null) {
            getWidget().removeStyleName(widgetColumnSize.getCssName());
        }

        widgetColumnSize = newColumnSize;

        // apply new style
        if (widgetColumnSize != null) {
            getWidget().addStyleName(widgetColumnSize.getCssName());
        }
    }

    public Alert getWidget() {
        return (Alert) super.getWidget();
    }

    public HTMLPanel getMessageAt(int index) {
        return (HTMLPanel) messagePanel.getWidget(index);
    }

    public WidgetTooltip getAlertTooltip() {
        return alertTooltip;
    }

}
