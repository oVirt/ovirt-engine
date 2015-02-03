package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.List;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.editor.WidgetWithLabelEditor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * Composite Editor that uses {@link EntityModelRadioButton}.
 */
public class EntityModelRadioButtonEditor extends AbstractValidatedWidgetWithLabel<Boolean, EntityModelRadioButton>
        implements IsEditor<WidgetWithLabelEditor<Boolean, EntityModelRadioButtonEditor>> {

    private final WidgetWithLabelEditor<Boolean, EntityModelRadioButtonEditor> editor;

    private final boolean useRadioButtonWidgetLabel;

    public EntityModelRadioButtonEditor(String group) {
        this(group, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public EntityModelRadioButtonEditor(String group, VisibilityRenderer visibilityRenderer) {
        this(group, Align.RIGHT, visibilityRenderer);
    }

    public EntityModelRadioButtonEditor(String group, Align labelAlign) {
        this(group, labelAlign, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public EntityModelRadioButtonEditor(String group, Align labelAlign, VisibilityRenderer visibilityRenderer) {
        super(new EntityModelRadioButton(group), visibilityRenderer);
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
        this.useRadioButtonWidgetLabel = labelAlign == Align.RIGHT;

        // In case we use RadioButton widget label instead of declared LabelElement,
        // align content widget container to the left and hide the LabelElement
        if (useRadioButtonWidgetLabel) {
            getContentWidgetContainer().getElement().getStyle().setFloat(Float.LEFT);
            getContentWidgetContainer().getElement().getStyle().setWidth(100, Unit.PCT);
            getFormLabel().setVisible(false);
        }
    }

    public RadioButton asRadioButton() {
        return getContentWidget().asRadioButton();
    }

    @Override
    public WidgetWithLabelEditor<Boolean, EntityModelRadioButtonEditor> asEditor() {
        return editor;
    }

    @Override
    protected void applyCommonValidationStyles() {
        // Suppress radio button styling, as different browsers behave
        // differently when styling radio button input elements
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
    }

    @Override
    protected Element getContentWidgetElement() {
        // Actual radio button input element is the first child of RadioButton element
        Node input = asRadioButton().getElement().getChild(0);
        return Element.as(input);
    }

    @Override
    protected void updateLabelElementId(String elementId) {
        if (useRadioButtonWidgetLabel) {
            LabelElement.as(Element.as(asRadioButton().getElement().getChild(1))).setHtmlFor(elementId);
        } else {
            super.updateLabelElementId(elementId);
        }
    }

    @Override
    public String getLabel() {
        if (useRadioButtonWidgetLabel) {
            return asRadioButton().getText();
        } else {
            return super.getLabel();
        }
    }

    @Override
    public void setLabel(String label) {
        if (useRadioButtonWidgetLabel) {
            asRadioButton().setText(label);
        } else {
            super.setLabel(label);
        }
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.SOLID);
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
    }

}
