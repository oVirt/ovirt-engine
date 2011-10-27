package org.ovirt.engine.ui.webadmin.widget.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleDialogPanel extends DialogBox {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleDialogPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    protected interface Style extends CssResource {

        String footerButton();

        String contentWidget();
    }

    @UiField
    SimplePanel headerCenterPanel;

    @UiField
    SimplePanel headerLeftPanel;

    @UiField
    SimplePanel contentPanel;

    @UiField
    FlowPanel footerButtonPanel;

    @UiField
    Style style;

    public SimpleDialogPanel() {
        setWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        getElement().getStyle().setZIndex(1);
    }

    @UiChild(tagname = "header", limit = 1)
    public void setHeader(Widget widget) {
        headerCenterPanel.setWidget(widget);
    }

    @UiChild(tagname = "logo", limit = 1)
    public void setLogo(Widget widget) {
        headerLeftPanel.setWidget(widget);
    }

    @UiChild(tagname = "content", limit = 1)
    public void setContent(Widget widget) {
        contentPanel.setWidget(widget);
        widget.addStyleName(style.contentWidget());
    }

    public Widget getContent() {
        return contentPanel.getWidget();
    }

    @UiChild(tagname = "footerButton")
    public void addFooterButton(Widget button) {
        button.addStyleName(style.footerButton());
        footerButtonPanel.add(button);
    }

    public void removeFooterButtons() {
        footerButtonPanel.clear();
    }

}
