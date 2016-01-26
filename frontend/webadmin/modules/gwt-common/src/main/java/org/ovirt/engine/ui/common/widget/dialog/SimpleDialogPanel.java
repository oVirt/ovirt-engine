package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleDialogPanel extends AbstractDialogPanel {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    interface WidgetUiBinder extends UiBinder<Widget, SimpleDialogPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    protected interface Style extends CssResource {

        String footerButton();

        String contentWidget();

        String obrand_dialogHeaderTitleElement();
    }

    @UiField
    SimplePanel logoPanel;

    @UiField
    FlowPanel headerTitlePanel;

    @UiField
    SimplePanel contentPanel;

    @UiField
    FlowPanel footerButtonPanel;

    @UiField
    FlowPanel footerStatusPanel;

    @UiField
    ButtonBase helpIconButton;

    @UiField
    ButtonBase closeIconButton;

    @UiField
    WidgetTooltip helpIconButtonTooltip;

    @UiField
    Style style;

    private UICommand helpCommand;

    public SimpleDialogPanel() {
        setWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        getElement().getStyle().setZIndex(1);
        addHelpButtonHandler();
        helpIconButtonTooltip.setText(constants.clickForHelp());
        helpIconButtonTooltip.reconfigure();
    }

    @Override
    @UiChild(tagname = "header", limit = 1)
    public void setHeader(Widget widget) {
        widget.addStyleName(style.obrand_dialogHeaderTitleElement());
        headerTitlePanel.insert(widget, 0); //Put the label at the front.
    }

    @UiChild(tagname = "logo", limit = 1)
    public void setLogo(Widget widget) {
        logoPanel.setWidget(widget);
    }

    @Override
    @UiChild(tagname = "content", limit = 1)
    public void setContent(Widget widget) {
        contentPanel.setWidget(widget);
        widget.addStyleName(style.contentWidget());
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

    private void addHelpButtonHandler() {
        helpIconButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                helpCommand.execute();
            }
        });
    }

    @Override
    public void setHelpCommand(UICommand command) {
        helpCommand = command;
        helpIconButton.setVisible(command != null);
    }

    @Override
    public HasClickHandlers getCloseIconButton() {
        return closeIconButton;
    }

    @Override
    public void setCloseIconButtonVisible(boolean visible) {
        closeIconButton.setVisible(visible);
    }

}
