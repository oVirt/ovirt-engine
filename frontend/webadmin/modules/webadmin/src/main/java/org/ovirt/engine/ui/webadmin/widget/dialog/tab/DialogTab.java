package org.ovirt.engine.ui.webadmin.widget.dialog.tab;

import org.ovirt.engine.ui.webadmin.idhandler.HasElementId;
import org.ovirt.engine.ui.webadmin.widget.AbstractValidatedWidget;
import org.ovirt.engine.ui.webadmin.widget.HasLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DialogTab extends AbstractValidatedWidget implements HasClickHandlers, HasLabel, HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, DialogTab> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {

        String active();

        String inactive();

    }

    private Widget tabContent;

    private boolean isActive;

    @UiField
    SimplePanel tabContainer;

    @UiField
    InlineLabel tabLabel;

    @UiField
    Style style;

    public DialogTab() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected Widget getValidatedWidget() {
        return tabContainer;
    }

    @Override
    public void markAsValid() {
        tabContainer.getElement().addClassName(isActive ? style.active() : style.inactive());
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return tabContainer.addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    public String getLabel() {
        return tabLabel.getText();
    }

    @Override
    public void setLabel(String label) {
        tabLabel.setText(label);
    }

    @UiChild(tagname = "content", limit = 1)
    public void setContent(Widget widget) {
        this.tabContent = widget;
    }

    public Widget getContent() {
        return tabContent;
    }

    public void activate() {
        isActive = true;
        tabContainer.getElement().replaceClassName(style.inactive(), style.active());
    }

    public void deactivate() {
        isActive = false;
        tabContainer.getElement().replaceClassName(style.active(), style.inactive());
    }

    public void setTabLabelStyle(String styleName) {
        tabLabel.setStyleName(styleName);
    }

    @Override
    public void setElementId(String elementId) {
        tabContainer.getElement().setId(elementId);
    }

}
