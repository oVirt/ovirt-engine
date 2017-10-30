package org.ovirt.engine.ui.common.widget;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.utils.ElementUtils;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel.Type;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Handles display of application-wide alert messages to the user via PatternFly
 * based {@link AlertPanel} widget.
 * <p>
 * If a message is longer than a single line in the alert box, its text will be
 * truncated and an ellipsis will show. On hover, a tooltip will show the full
 * text of the alert.
 *
 * @see AlertPanel
 */
public class AlertManager {

    private static final String ALERT_PANEL_CLASS = "ovirt-alert-main"; //$NON-NLS-1$
    private static final String ALERT_PANEL_DANGER_CLASS = "ovirt-alert-main-danger"; //$NON-NLS-1$
    private static final String ALERT_PANEL_WARNING_CLASS = "ovirt-alert-main-warning"; //$NON-NLS-1$
    private static final String ALERT_PANEL_SUCCESS_CLASS = "ovirt-alert-main-success"; //$NON-NLS-1$
    private static final String ALERT_PANEL_INFO_CLASS = "ovirt-alert-main-info"; //$NON-NLS-1$
    private static final String ALERT_MESSAGE_CLASS = "ovirt-alert-main-message"; //$NON-NLS-1$
    private static final String BOOTSTRAP_CENTER_CLASS = "center-block"; //$NON-NLS-1$

    private static final String RELOAD_LINK = "javascript:window.location.reload(true)"; //$NON-NLS-1$

    private static final CommonApplicationConstants CONSTANTS = AssetProvider.getConstants();
    private static final CommonApplicationMessages MESSAGES = AssetProvider.getMessages();

    // TODO(vs) why are we holding reference to a specific instance?
    private AlertPanel alert;

    /**
     * Informs the user about an uncaught exception.
     */
    public void showUncaughtExceptionAlert(Throwable t) {
        SafeHtmlBuilder alertMessage = new SafeHtmlBuilder()
                .appendHtmlConstant(MESSAGES.uncaughtExceptionAlertMessage(RELOAD_LINK));

        String errorDetails = t.getMessage();
        if (StringHelper.isNotNullOrEmpty(errorDetails)) {
            alertMessage.appendEscaped(CONSTANTS.space());
            alertMessage.appendEscaped(MESSAGES.uncaughtExceptionAlertMessageDetails(errorDetails));
            // new line
            alertMessage.appendHtmlConstant("<br />"); //$NON-NLS-1$
            alertMessage.appendEscaped(CONSTANTS.checkUiLogs());
        }

        showAlert(Type.DANGER, alertMessage.toSafeHtml());
    }

    /**
     * Displays an application-wide alert message that must be closed manually.
     */
    public void showAlert(Type type, SafeHtml message) {
        showAlert(type, message, 0);
    }

    /**
     * Displays an application-wide alert message that auto-hides automatically.
     */
    public void showAlert(Type type, SafeHtml message, int autoHideMs) {
        if (alert == null) {
            alert = createAlert(type, message);
            attachAlert(alert);
        } else {
            alert.incCount();
            updateAlert(type, message, alert);
        }

        if (autoHideMs > 0) {
            final Timer timer = new Timer() {
                @Override
                public void run() {
                    detachAlert(alert);
                }
            };
            alert.getWidget().addCloseHandler(evt -> {
                timer.cancel();
                alert = null;
            });
            timer.schedule(autoHideMs);
        } else {
            alert.getWidget().addCloseHandler(evt -> alert = null);
        }
    }

    void attachAlert(AlertPanel alertPanel) {
        // Add widget's DOM element straight into HTML body
        RootPanel.get().add(alertPanel);

        // Use tool tip in case the textual content overflows
        Element messageDivElement = alertPanel.getMessageAt(0).getElement();
        if (ElementUtils.detectHorizontalOverflow(messageDivElement)) {
            alertPanel.getAlertTooltip().setText(messageDivElement.getInnerText());
            alertPanel.getAlertTooltip().setPlacement(Placement.BOTTOM);
        }
    }

    void detachAlert(AlertPanel alertPanel) {
        RootPanel.get().remove(alertPanel);
    }

    AlertPanel createAlert(Type type, SafeHtml message) {
        AlertPanel alertPanel = new AlertPanel();
        updateAlert(type, message, alertPanel);
        return alertPanel;
    }

    private void updateAlert(Type type, SafeHtml message, AlertPanel alertPanel) {
        alertPanel.clearMessages();
        alertPanel.setType(type);
        alertPanel.setWidgetColumnSize(null);
        alertPanel.getWidget().setDismissable(true);

        alertPanel.addStyleName(ALERT_PANEL_CLASS);
        alertPanel.addStyleName(BOOTSTRAP_CENTER_CLASS);

        switch (type) {
            case DANGER:
                alertPanel.addStyleName(ALERT_PANEL_DANGER_CLASS);
                break;
            case WARNING:
                alertPanel.addStyleName(ALERT_PANEL_WARNING_CLASS);
                break;
            case SUCCESS:
                alertPanel.addStyleName(ALERT_PANEL_SUCCESS_CLASS);
                break;
            case INFO:
                alertPanel.addStyleName(ALERT_PANEL_INFO_CLASS);
                break;
        }

        alertPanel.addMessage(message, ALERT_MESSAGE_CLASS);
    }

}
