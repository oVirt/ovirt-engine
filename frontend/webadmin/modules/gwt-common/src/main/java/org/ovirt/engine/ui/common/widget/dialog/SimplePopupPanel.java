package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SimplePopupPanel extends DialogBoxWithKeyHandlers {

    interface WidgetUiBinder extends UiBinder<Widget, SimplePopupPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    public SimplePanel headerPanel;

    @UiField
    public SimplePanel contentPanel;

    public SimplePopupPanel() {
        setWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @UiChild(tagname = "header", limit = 1)
    public void setHeader(Widget widget) {
        headerPanel.setWidget(widget);
    }

    @UiChild(tagname = "content", limit = 1)
    public void setContent(Widget widget) {
        contentPanel.setWidget(widget);
    }

    @Override
    protected void beginDragging(MouseDownEvent event) {
        // Disable dragging
    }

}
