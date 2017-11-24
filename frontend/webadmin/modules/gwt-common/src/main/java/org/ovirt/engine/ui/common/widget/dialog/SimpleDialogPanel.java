package org.ovirt.engine.ui.common.widget.dialog;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleDialogPanel extends AbstractDialogPanel {

    // 100 pixels is the height of the header and the footer combined. Excluding the margins
    private static final int HEADER_FOOTER_HEIGHT = 100;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    interface WidgetUiBinder extends UiBinder<Widget, SimpleDialogPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    protected interface Style extends CssResource {
        String footerButton();
    }

    @UiField
    SimplePanel contentPanel;

    @UiField
    FlowPanel footerButtonPanel;

    @UiField
    FlowPanel footerStatusPanel;

    @UiField
    ScrollPanel contentScrollPanel;

    @UiField
    Button closeButton;

    @UiField
    FlowPanel header;

    @UiField
    Anchor infoAnchor;

    @UiField
    WidgetTooltip helpIconButtonTooltip;

    @UiField
    Style style;

    private UICommand helpCommand;

    public SimpleDialogPanel() {
        setWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        setDraggable(true);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
        setDraggable(false);
    }

    private native void setDraggable(boolean dragEnabled) /*-{
        var containerElement = this.@org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel::getContainerElement()();
        var $popupContent = $wnd.jQuery(containerElement).parent();

        if (dragEnabled) {
            $popupContent.draggable({ handle: '.modal-header' });
        } else {
            $popupContent.draggable('destroy');
        }
    }-*/;

    @Override
    @UiChild(tagname = "header", limit = 1)
    public void setHeader(String headerTitle) {
        HTMLPanel headerTitlePanel = new HTMLPanel("H4", headerTitle); //$NON-NLS-1$
        headerTitlePanel.addStyleName(Styles.MODAL_TITLE);
        headerTitlePanel.getElement().getStyle().setFloat(Float.LEFT);
        // Close button and IconType makes 2 widgets
        while (header.getWidgetCount() > 2) {
            header.remove(header.getWidgetCount() - 1);
        }
        header.add(headerTitlePanel);
        addHelpButtonHandler();
        helpIconButtonTooltip.setText(constants.clickForHelp());
    }

    @Override
    @UiChild(tagname = "content", limit = 1)
    public void setContent(Widget widget) {
        contentPanel.setWidget(widget);
    }

    @Override
    public Widget getContent() {
        return contentPanel.getWidget();
    }

    @Override
    @UiChild(tagname = "footerButton")
    public void addFooterButton(Widget button) {
        button.addStyleName(style.footerButton());
        footerButtonPanel.add(button);
    }

    @Override
    public void show() {
        super.show();
        contentScrollPanel.getElement().getStyle().setHeight(getOffsetHeight() - HEADER_FOOTER_HEIGHT, Unit.PX);
        contentPanel.getElement().getStyle().setProperty("minHeight", getOffsetHeight() - HEADER_FOOTER_HEIGHT, Unit.PX); //$NON-NLS-1$
    }

    public void setNoScroll(boolean value) {
        if (value) {
            contentScrollPanel.getElement().getStyle().setOverflow(Overflow.VISIBLE);
        } else {
            contentScrollPanel.getElement().getStyle().setOverflow(Overflow.AUTO);
        }
    }

    @Override
    public void addStatusWidget(Widget widget) {
        footerStatusPanel.add(widget);
    }

    @Override
    public void removeFooterButtons() {
        footerButtonPanel.clear();
    }

    @Override
    public void setFooterPanelVisible(boolean visible) {
        footerButtonPanel.setVisible(visible);
    }

    @Override
    public void addContentStyleName(String styleName) {
        contentPanel.addStyleName(styleName);
    }

    @Override
    public HasClickHandlers getCloseIconButton() {
        return closeButton;
    }

    @Override
    public void setCloseIconButtonVisible(boolean visible) {
        closeButton.setVisible(visible);
    }

    private void addHelpButtonHandler() {
        infoAnchor.addClickHandler(event -> helpCommand.execute());
    }

    @Override
    public void setHelpCommand(UICommand command) {
        helpCommand = command;
        infoAnchor.setVisible(command != null);
    }

}
