package org.ovirt.engine.ui.common.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidatedPanelWidget extends AbstractValidatedWidget {

    @UiField
    SimplePanel panel;

    interface WidgetUiBinder extends UiBinder<Widget, ValidatedPanelWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public ValidatedPanelWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public ValidatedPanelWidget(Widget contentWidget) {
        this();
        setWidget(contentWidget);
    }

    public void setWidget(Widget widget) {
        panel.setWidget(widget);
    }

    @Override
    protected Widget getValidatedWidget() {
        return panel;
    }
}
