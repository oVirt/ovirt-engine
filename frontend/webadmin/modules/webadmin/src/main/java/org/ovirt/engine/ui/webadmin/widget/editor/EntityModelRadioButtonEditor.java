package org.ovirt.engine.ui.webadmin.widget.editor;

import org.ovirt.engine.ui.webadmin.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.webadmin.widget.Align;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * Composite Editor that uses {@link EntityModelRadioButton}.
 */
public class EntityModelRadioButtonEditor extends AbstractValidatedWidgetWithLabel<Object, EntityModelRadioButton>
        implements IsEditor<WidgetWithLabelEditor<Object, TakesValueWithChangeHandlersEditor<Object>, EntityModelRadioButtonEditor>> {

    private final WidgetWithLabelEditor<Object, TakesValueWithChangeHandlersEditor<Object>, EntityModelRadioButtonEditor> editor;

    private final boolean useRadioButtonWidgetLabel;

    public EntityModelRadioButtonEditor(String group) {
        this(group, Align.RIGHT);
    }

    public EntityModelRadioButtonEditor(String group, Align labelAlign) {
        super(new EntityModelRadioButton(group));
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
        this.useRadioButtonWidgetLabel = labelAlign == Align.RIGHT;

        // In case we use RadioButton widget label instead of declared LabelElement,
        // align content widget container to the left and hide the LabelElement
        if (useRadioButtonWidgetLabel) {
            getContentWidgetContainer().getElement().getStyle().setFloat(Float.LEFT);
            getLabelElement().getStyle().setDisplay(Display.NONE);
        }
    }

    public RadioButton asRadioButton() {
        return getContentWidget().asRadioButton();
    }

    @Override
    public WidgetWithLabelEditor<Object, TakesValueWithChangeHandlersEditor<Object>, EntityModelRadioButtonEditor> asEditor() {
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

}
