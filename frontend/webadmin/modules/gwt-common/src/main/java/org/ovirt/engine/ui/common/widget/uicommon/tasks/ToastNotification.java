package org.ovirt.engine.ui.common.widget.uicommon.tasks;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonDismiss;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ToastNotification extends Composite {

    public enum NotificationStatus {
        INFO("alert-info", "pficon-info"), // $NON-NLS-1$ $NON-NLS-2$
        SUCCESS("alert-success", "pficon-ok"), // $NON-NLS-1$ $NON-NLS-2$
        WARNING("alert-warning", "pficon-warning-triangle-o"), // $NON-NLS-1$ $NON-NLS-2$
        DANGER("alert-danger", "pficon-error-circle-o"); // $NON-NLS-1$ $NON-NLS-2$

        private final String styleName;
        private final String iconStyleName;

        NotificationStatus(String styleName, String iconStyleName) {
            this.styleName = styleName;
            this.iconStyleName = iconStyleName;
        }

        public String getStyleName() {
            return styleName;
        }

        public String getIconStyleName() {
            return iconStyleName;
        }

        public static NotificationStatus from(String name) {
            try {
                return NotificationStatus.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return INFO;
            }
        }
    }

    public interface Style extends CssResource {
        String container();
    }

    interface ViewUiBinder extends UiBinder<Widget, ToastNotification> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }


    @UiField
    FlowPanel widgetWrapper;

    @UiField
    Button closeButton;

    @UiField
    Span label;

    @UiField
    Span icon;

    public ToastNotification(String text, NotificationStatus status) {
        super();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        configureCloseButton();

        label.getElement().setInnerText(text);
        setStatus(status);
    }

    private void configureCloseButton() {
        closeButton.setDataDismiss(ButtonDismiss.ALERT);
        Span closeButtonSpan = new Span();
        closeButtonSpan.addStyleName(PatternflyIconType.PF_BASE.getCssName());
        closeButtonSpan.addStyleName(PatternflyIconType.PF_CLOSE.getCssName());
        closeButton.add(closeButtonSpan);
    }

    public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
        return closeButton.addClickHandler(handler);
    }

    public void setStatus(NotificationStatus status) {
        // Remove existing status first.
        for (NotificationStatus s : NotificationStatus.values()) {
            widgetWrapper.removeStyleName(s.getStyleName());
            icon.removeStyleName(s.getIconStyleName());
        }
        widgetWrapper.addStyleName(status.getStyleName());
        icon.addStyleName(status.getIconStyleName());
    }

}
