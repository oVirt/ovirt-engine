package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.css.PatternflyConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class WarningNotificationLabel extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, WarningNotificationLabel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    HTMLPanel iconContainer;

    @UiField
    Label text;

    public WarningNotificationLabel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        iconContainer.addStyleName(PatternflyConstants.PFICON);
        iconContainer.addStyleName(PatternflyConstants.PFICON_WARNING_TRIANGLE_O);
    }

    public void setText(String alertText) {
        text.setText(alertText);
    }
}
