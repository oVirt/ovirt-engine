package org.ovirt.engine.ui.common.widget;


import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * This widget is to be used to provide alerts to the user. The widget uses Patternfly
 * icons, which are displayed to the left of the alert text. GWT-Bootstrap AlertTypes
 * are used to control the color of the alert box and text.
 */
public class AlertWithIcon extends Composite {

    interface WidgetUiBinder extends UiBinder<Alert, AlertWithIcon> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Alert alert;

    @UiField
    Span iconContainer;

    @UiField
    Label alertTextLabel;

    public AlertWithIcon() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        setAlertType(AlertType.INFO);
    }

    public AlertWithIcon(AlertType alertType) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        setAlertType(alertType);
    }

    public void setAlertType(AlertType alertType) {
        alert.setType(alertType);
        iconContainer.addStyleName(PatternflyConstants.PFICON);
        switch (alertType) {
            case SUCCESS:
                iconContainer.addStyleName(PatternflyConstants.PFICON_OK);
                break;
            case WARNING:
                iconContainer.addStyleName(PatternflyConstants.PFICON_WARNING_TRIANGLE_O);
                break;
            case DANGER:
                iconContainer.addStyleName(PatternflyConstants.PFICON_ERROR_CIRCLE_O);
                break;
            case INFO:
            default:
                iconContainer.addStyleName(PatternflyConstants.PFICON_INFO);
                break;
        }
    }

    public void setText(String alertText) {
        alertTextLabel.setText(alertText);
    }

    public void setHtmlText(SafeHtml html) {
        alertTextLabel.getElement().setInnerSafeHtml(html);
    }

    public Alert asAlert() {
        return alert;
    }
}
