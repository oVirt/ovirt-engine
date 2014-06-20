package org.ovirt.engine.ui.common.widget.editor.generic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.HasLabel;
import org.ovirt.engine.ui.common.widget.HasValidation;

import java.util.List;

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
    }

    @UiField
    LabelElement label;

    @UiField(provided = true)
    SimplePanel contentWidgetContainer;

    @UiField
    Image attachedSeparatedImage;

    @UiField
    FlowPanel contentWrapper;

    @UiField
    Style style;

    private AbstractValidatedWidgetWithLabel decorated;

    public EntityModelDetachableWidgetWithLabel(AbstractValidatedWidgetWithLabel decorated) {
        this.decorated = decorated;
        contentWidgetContainer = decorated.getContentWidgetContainer();

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));

        initialize(contentWidgetContainer, attachedSeparatedImage, style);
    }

    @Override
    public String getLabel() {
        return label.getInnerText();
    }

    @Override
    public void setLabel(String label) {
        this.label.setInnerText(label);
    }

    @Override
    public boolean isEnabled() {
        return decorated.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        decorated.setEnabled(enabled);

        if (enabled) {
            label.replaceClassName(style.labelDisabled(), style.labelEnabled());
        } else {
            label.replaceClassName(style.labelEnabled(), style.labelDisabled());
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
