package org.ovirt.engine.ui.common.widget.dialog.tab;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidget;
import org.ovirt.engine.ui.common.widget.HasLabel;
import org.ovirt.engine.ui.common.widget.HasValidation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

public class DialogTab extends AbstractValidatedWidget implements HasClickHandlers, HasLabel, HasElementId, HasKeyUpHandlers, FocusableComponentsContainer {

    interface WidgetUiBinder extends UiBinder<Widget, DialogTab> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
        String obrand_active();

        String inactive();
    }

    @UiField
    FocusPanel tabContainer;

    @UiField
    InlineLabel tabLabel;

    @UiField
    Style style;

    private Widget tabContent;
    private boolean isActive;

    public DialogTab() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected Widget getValidatedWidget() {
        return tabContainer;
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        getValidatedWidgetStyle().clearBorderColor();
        tabContainer.getElement().addClassName(isActive ? style.obrand_active() : style.inactive());
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return tabContainer.addDomHandler(handler, ClickEvent.getType());
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return tabContainer.addKeyUpHandler(handler);
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
        tabContainer.getElement().replaceClassName(style.inactive(), style.obrand_active());
    }

    public void deactivate() {
        isActive = false;
        tabContainer.getElement().replaceClassName(style.obrand_active(), style.inactive());
    }

    public void setTabLabelStyle(String styleName) {
        tabLabel.setStyleName(styleName);
    }

    @Override
    public void setElementId(String elementId) {
        tabContainer.getElement().setId(elementId);
    }

    public InlineLabel getTabLabel() {
        return tabLabel;
    }

    /**
     * Disables the content widget recursively, using {@link HasEnabled} interface.
     */
    public void disableContent() {
        disable(getContent());
    }

    private void disable(Widget content) {
        if (content instanceof IndexedPanel) {
            for (int i = 0; i < ((IndexedPanel) content).getWidgetCount(); i++) {
                disable(((IndexedPanel) content).getWidget(i));
            }
        } else if (content instanceof HasEnabled) {
            ((HasEnabled) content).setEnabled(false);
        }
    }

    public List<HasValidation> getInvalidWidgets() {
        return getInvalidWidgets(getContent());
    }

    private List<HasValidation> getInvalidWidgets(Widget content) {
        List<HasValidation> hasValidations = new ArrayList<>();
        if (content instanceof IndexedPanel) {
            for (int i = 0; i < ((IndexedPanel) content).getWidgetCount(); i++) {
                hasValidations.addAll(getInvalidWidgets(((IndexedPanel) content).getWidget(i)));
            }
        } else if (content instanceof HasValidation && !((HasValidation) content).isValid()) {
            hasValidations.add((HasValidation) content);
        }

        return hasValidations;
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        tabContainer.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }
}
