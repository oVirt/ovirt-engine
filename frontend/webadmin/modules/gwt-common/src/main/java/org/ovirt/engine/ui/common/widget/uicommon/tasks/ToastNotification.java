package org.ovirt.engine.ui.common.widget.uicommon.tasks;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonDismiss;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ToastNotification extends Composite {

    public enum NotificationStatus {
        INFO("alert-info", "pficon-info"), // $NON-NLS-1$ $NON-NLS-2$
        SUCCESS("alert-success", "pficon-ok"), // $NON-NLS-1$ $NON-NLS-2$
        WARNING("alert-warning", "pficon-warning-triangle-o"), // $NON-NLS-1$ $NON-NLS-2$
        DANGER("alert-danger", "pficon-error-circle-o"); // $NON-NLS-1$ $NON-NLS-2$

        private final String styleName;
        private final String iconStyleName;

        private NotificationStatus(String styleName, String iconStyleName) {
            this.styleName = styleName;
            this.iconStyleName = iconStyleName;
        }

        public String getStyleName() {
            return styleName;
        }

        public String getIconStyleName() {
            return iconStyleName;
        }
    }

    public interface Style extends CssResource {
        String container();
    }

    interface ViewUiBinder extends UiBinder<Widget, ToastNotification> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final int CLOSE_DELAY = 8000;

    private static FlowPanel container = new FlowPanel();

    static {
        RootPanel.get().add(container);
    }

    @UiField
    FlowPanel widgetWrapper;

    @UiField
    Button closeButton;

    @UiField
    HTMLPanel label;

    @UiField
    HTMLPanel icon;

    HandlerRegistration clickRegistration;

    @UiField
    Style style;

    private ToastNotification(String text) {
        super();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        configureCloseButton();
        label.getElement().setInnerText(text);
        setStatus(NotificationStatus.INFO);
        clickRegistration = closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                cleanup();
            }

        });

        Timer closeTimer = new Timer() {

            @Override
            public void run() {
                cleanup();
            }

        };
        closeTimer.schedule(CLOSE_DELAY);
    }

    private void configureCloseButton() {
        closeButton.setDataDismiss(ButtonDismiss.ALERT);
        Span closeButtonSpan = new Span();
        closeButtonSpan.addStyleName(PatternflyIconType.PF_BASE.getCssName());
        closeButtonSpan.addStyleName(PatternflyIconType.PF_CLOSE.getCssName());
        closeButton.add(closeButtonSpan);
    }

    private void cleanup() {
        // Clean myself up.
        clickRegistration.removeHandler();
        ToastNotification.this.removeFromParent();
        if (container.getWidgetCount() == 0) {
            container.setVisible(false);
        }
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return closeButton.addClickHandler(handler);
    }

    public void setStatus(NotificationStatus status) {
        // Remove existing status first.
        for (NotificationStatus loopStatus : NotificationStatus.values()) {
            widgetWrapper.removeStyleName(loopStatus.getStyleName());
            icon.removeStyleName(loopStatus.getIconStyleName());
        }
        widgetWrapper.addStyleName(status.getStyleName());
        icon.addStyleName(status.getIconStyleName());
    }

    private Style getStyle() {
        return style;
    }

    public static ToastNotification createNotification(String text) {
        ToastNotification result = new ToastNotification(text);
        container.addStyleName(result.getStyle().container());
        container.add(result);
        container.setVisible(true);
        return result;
    }

}
