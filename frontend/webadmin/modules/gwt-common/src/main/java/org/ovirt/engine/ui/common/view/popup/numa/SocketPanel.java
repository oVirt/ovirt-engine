package org.ovirt.engine.ui.common.view.popup.numa;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class SocketPanel extends Composite {
    interface WidgetUiBinder extends UiBinder<Widget, SocketPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel container;

    @UiField
    Label headerLabel;

    public SocketPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setHeaderText(SafeHtml text) {
        headerLabel.setText(text.asString());
    }

    public void addWidget(IsWidget widget) {
        container.add(widget);
    }
}
