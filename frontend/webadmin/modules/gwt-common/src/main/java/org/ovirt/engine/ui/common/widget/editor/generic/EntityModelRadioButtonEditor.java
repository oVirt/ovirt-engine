package org.ovirt.engine.ui.common.widget.editor.generic;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.editor.WidgetWithLabelEditor;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * Composite Editor that uses {@link EntityModelRadioButton}.
 */
public class EntityModelRadioButtonEditor extends AbstractValidatedWidgetWithLabel<Boolean, EntityModelRadioButton>
        implements IsEditor<WidgetWithLabelEditor<Boolean, EntityModelRadioButtonEditor>> {

    private static final String MAX_WIDTH = "maxWidth"; //$NON-NLS-1$

    private final WidgetWithLabelEditor<Boolean, EntityModelRadioButtonEditor> editor;

    private final boolean useRadioButtonWidgetLabel;

    @UiConstructor
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
            hideLabel();
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
    protected Element getContentWidgetElement() {
        // Actual radio button input element is the first child of RadioButton element
        Node input = asRadioButton().getElement().getChild(0);
        return Element.as(input);
    }

    @Override
    public void setUsePatternFly(final boolean use) {
        super.setUsePatternFly(use);
        if (use) {
            getRadioButtonWidgetLabel().getStyle().setPaddingLeft(10, Unit.PX);
            getRadioButtonWidgetLabel().getStyle().setPosition(Position.RELATIVE);
            getRadioButtonWidgetLabel().getStyle().setProperty(MAX_WIDTH, "94%"); //$NON-NLS-1$
            getValidatedWidgetStyle().clearPadding();
            // checkboxes don't use form-control
            getContentWidgetElement().removeClassName(Styles.FORM_CONTROL);
            getContentWidgetElement().getStyle().setVerticalAlign(VerticalAlign.TOP);
            removeContentWidgetStyleName(Styles.FORM_CONTROL);
        }
    }

    @Override
    protected void updateLabelElementId(String elementId) {
        if (useRadioButtonWidgetLabel) {
            LabelElement.as(Element.as(asRadioButton().getElement().getChild(1))).setHtmlFor(elementId);
        } else {
            super.updateLabelElementId(elementId);
        }
    }

    protected LabelElement getRadioButtonWidgetLabel() {
        return LabelElement.as(Element.as(asRadioButton().getElement().getChild(1)));
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
