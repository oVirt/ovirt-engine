package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.List;

import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasLabel;
import org.ovirt.engine.ui.common.widget.HasValidation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Takes an AbstractValidatedWidgetWithLabel and decorates it with a detachable icon
 */
public class EntityModelDetachableWidgetWithLabel extends BaseEntityModelDetachableWidget implements HasLabel, HasEnabled, HasValidation {

    interface WidgetUiBinder extends UiBinder<Widget, EntityModelDetachableWidgetWithLabel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends BaseStyle {

        String labelEnabled();

        String labelDisabled();

        String wrapper();

        String contentWrapper();

        String contentWidgetContainer();
    }

    @UiField
    FormLabel label;

    @UiField(provided = true)
    FlowPanel contentWidgetContainer;

    @UiField
    Image attachedSeparatedImage;

    @UiField
    FlowPanel contentWrapper;

    @UiField
    Style style;

    @UiField
    FlowPanel imageContainer;

    @UiField
    FlowPanel wrapperPanel;

    SimplePanel sizeContainer;

    private AbstractValidatedWidgetWithLabel<?, ?> decorated;

    private boolean usePatternFly = false;

    public EntityModelDetachableWidgetWithLabel(AbstractValidatedWidgetWithLabel<?, ?> decorated) {
        this.decorated = decorated;
        contentWidgetContainer = decorated.getContentWidgetContainer();

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        initialize(contentWidgetContainer, attachedSeparatedImage, style);
    }

    @Override
    public String getLabel() {
        return label.getText();
    }

    @Override
    public void setLabel(String label) {
        this.label.setText(label);
    }

    @Override
    public void setLabelColSize(ColumnSize size) {
        label.addStyleName(size.getCssName());
    }

    public void setWidgetColSize(ColumnSize size) {
        if (sizeContainer != null) {
            sizeContainer.addStyleName(size.getCssName());
        }
    }

    @Override
    public void setUsePatternFly(boolean use) {
        usePatternFly = use;
        decorated.setUsePatternFly(use);
        if (use) {
            wrapperPanel.remove(contentWidgetContainer);
            sizeContainer = new SimplePanel();
            sizeContainer.setWidget(contentWidgetContainer);
            wrapperPanel.insert(sizeContainer, 2); //The label and the chain icon come first

            wrapperPanel.removeStyleName(style.wrapper());
            wrapperPanel.addStyleName(Styles.FORM_GROUP);
            contentWrapper.removeStyleName(style.contentWrapper());
            contentWidgetContainer.removeStyleName(style.contentWidgetContainer());
            contentWidgetContainer.removeStyleName(style.contentWidgetWithDetachable());
            contentWidgetContainer.removeStyleName(style.contentWidgetWithoutDetachable());
            label.getElement().replaceClassName(style.labelDisabled(), style.labelEnabled());
            imageContainer.addStyleName(ColumnSize.SM_1.getCssName());
        }
    }

    @Override
    public boolean isEnabled() {
        return decorated.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        decorated.setEnabled(enabled);

        if (enabled) {
            label.getElement().replaceClassName(style.labelDisabled(), style.labelEnabled());
        } else {
            label.getElement().replaceClassName(style.labelEnabled(), style.labelDisabled());
        }
    }

    protected void changeWidgetStyle(UIObject widget, boolean detachableIconVisible) {
        if (!usePatternFly) {
            super.changeWidgetStyle(widget, detachableIconVisible);
        }
    }

    public void addContentWrapperStypeName(String styleName) {
        contentWrapper.addStyleName(styleName);
    }

    @Override
    public void markAsValid() {
        decorated.markAsValid();
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        decorated.markAsInvalid(validationHints);
    }

    @Override
    public boolean isValid() {
        return decorated.isValid();
    }
}
