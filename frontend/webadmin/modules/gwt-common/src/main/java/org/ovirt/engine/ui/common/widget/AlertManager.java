package org.ovirt.engine.ui.common.widget;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.shared.event.AlertCloseEvent;
import org.gwtbootstrap3.client.shared.event.AlertCloseHandler;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.utils.ElementUtils;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel.Type;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipMixin;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Handles display of application-wide alert messages to the user via PatternFly
 * based {@link AlertPanel} widget.
 * <p>
 * Includes functionality to {@linkplain #setCanShowAlerts disable showing alerts},
 * which queues up any alert messages sent while showing alerts is disabled. Queued
 * up messages will be replayed to the user once showing alerts is re-enabled.
 * <p>
 * If a message is longer than a single line in the alert box, its text will be
 * truncated and an ellipsis will show. On hover, a tooltip will show the full text
 * of the alert.
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

    private final List<ScheduledCommand> deferredAlertCommands = new ArrayList<>();

    private boolean canShowAlerts;

    private AlertPanel alert;

    public AlertManager() {
        setCanShowAlerts(false);
    }

    /**
     * Controls whether alerts can be shown (<code>canShowAlerts=true</code>)
     * or queued up to be shown later on (<code>canShowAlerts=false</code>).
     */
    public void setCanShowAlerts(boolean canShowAlerts) {
        this.canShowAlerts = canShowAlerts;

        if (canShowAlerts) {
            // Replay deferred alerts
            for (ScheduledCommand command : deferredAlertCommands) {
                command.execute();
            }
            deferredAlertCommands.clear();
        }
    }

    /**
     * Informs the user about an uncaught exception.
     */
    public void showUncaughtExceptionAlert(Throwable t) {
        SafeHtmlBuilder alertMessage = new SafeHtmlBuilder()
                .appendHtmlConstant(MESSAGES.uncaughtExceptionAlertMessage(RELOAD_LINK));

        String errorDetails = t.getMessage();
        if (!StringUtils.isEmpty(errorDetails)) {
            alertMessage.appendEscaped(CONSTANTS.space());
            alertMessage.appendEscaped(MESSAGES.uncaughtExceptionAlertMessageDetails(errorDetails));
            //new line
            alertMessage.appendHtmlConstant("<br />"); //$NON-NLS-1$
            alertMessage.appendEscaped(CONSTANTS.checkUiLogs());
        }

        showAlert(Type.DANGER, alertMessage.toSafeHtml());
    }

    /**
     * Displays an application-wide alert message.
     * <p>
     * If showing alerts is disabled, the alert will be queued up and replayed
     * once showing alerts is re-enabled.
     *
     * @see #setCanShowAlerts
     */
    public void showAlert(final Type type, final SafeHtml message) {
        showAlert(type, message, 0);
    }

    public void showAlert(final Type type, final SafeHtml message, final int autoHideMs) {
        ScheduledCommand command = new ScheduledCommand() {
            @Override
            public void execute() {
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
                    alert.getWidget().addCloseHandler(new AlertCloseHandler() {
                        @Override
                        public void onClose(AlertCloseEvent evt) {
                            timer.cancel();
                            alert = null;
                        }
                    });
                    timer.schedule(autoHideMs);
                } else {
                    alert.getWidget().addCloseHandler(new AlertCloseHandler() {
                        @Override
                        public void onClose(AlertCloseEvent evt) {
                            alert = null;
                        }
                    });
                }
            }
        };

        if (canShowAlerts) {
            command.execute();
        } else {
            deferredAlertCommands.add(command);
        }
    }

    void attachAlert(AlertPanel alertPanel) {
        // Add widget's DOM element straight into HTML body
        RootPanel.get().add(alertPanel);

        // Use tool tip in case the textual content overflows
        Element messageDivElement = alertPanel.getMessageAt(0).getElement();
        if (ElementUtils.detectOverflowUsingScrollWidth(messageDivElement)) {
            SafeHtml tooltipContent = SafeHtmlUtils.fromString(messageDivElement.getInnerText());
            TooltipMixin.addTooltipToElement(tooltipContent, alertPanel.getElement(), Placement.BOTTOM);
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
