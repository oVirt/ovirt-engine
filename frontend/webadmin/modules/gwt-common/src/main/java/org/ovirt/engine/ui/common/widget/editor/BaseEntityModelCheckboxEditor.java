package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.PatternFlyCompatible;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.user.client.ui.CheckBox;

public abstract class BaseEntityModelCheckboxEditor<T> extends AbstractValidatedWidgetWithLabel<T, BaseEntityModelCheckbox<T>>
        implements IsEditor<WidgetWithLabelEditor<T, BaseEntityModelCheckboxEditor<T>>>, PatternFlyCompatible {

    private static final String CBE_LABEL_PFLY_FIX = "cbe_label_pfly_fix"; //$NON-NLS-1$

    private final WidgetWithLabelEditor<T, BaseEntityModelCheckboxEditor<T>> editor;

    private boolean useCheckBoxWidgetLabel;

    public BaseEntityModelCheckboxEditor(BaseEntityModelCheckbox<T> contentWidget) {
        this(Align.LEFT, contentWidget);
    }

    public BaseEntityModelCheckboxEditor(Align labelAlign, BaseEntityModelCheckbox<T> contentWidget) {
        this(labelAlign, contentWidget, new VisibilityRenderer.SimpleVisibilityRenderer());
    }

    public BaseEntityModelCheckboxEditor(Align labelAlign, BaseEntityModelCheckbox<T> contentWidget,
            VisibilityRenderer visibilityRenderer) {
        this(labelAlign, contentWidget, visibilityRenderer, false);
    }

    public BaseEntityModelCheckboxEditor(Align labelAlign, BaseEntityModelCheckbox<T> contentWidget,
            VisibilityRenderer visibilityRendere, boolean useFullWidthIfAvailable) {
        super(contentWidget, visibilityRendere);

        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
        this.useCheckBoxWidgetLabel = labelAlign == Align.RIGHT;

        // In case we use CheckBox widget label instead of declared LabelElement,
        // align content widget container to the left and hide the LabelElement
        if (useCheckBoxWidgetLabel) {
            getContentWidgetContainer().getElement().getStyle().setFloat(Float.LEFT);
            getFormLabel().asWidget().setVisible(false);
            if (useFullWidthIfAvailable) {
                getContentWidgetContainer().getElement().getStyle().setWidth(100, Unit.PCT);
            }
        }

        // patternfly hacks
        getInternalLabelElement().addClassName(CBE_LABEL_PFLY_FIX);
        getContentWidgetElement().getStyle().setVerticalAlign(VerticalAlign.TOP);

    }

    public CheckBox asCheckBox() {
        return getContentWidget().asCheckBox();
    }

    @Override
    public void setUsePatternFly(final boolean use) {
        super.setUsePatternFly(use);
        if (use) {
            getCheckboxWidgetLabel().getStyle().setPaddingLeft(10, Unit.PX);
            getCheckboxWidgetLabel().getStyle().setPaddingRight(10, Unit.PX);
            getCheckboxWidgetLabel().getStyle().setPosition(Position.RELATIVE);
            getCheckboxWidgetLabel().getStyle().setDisplay(Display.INLINE);
            noPaddingNoFixes();
        }
    }

    protected void noPaddingNoFixes() {
        getValidatedWidgetStyle().clearPadding();
        getCheckboxWidgetLabel().removeClassName(CBE_LABEL_PFLY_FIX);
        // checkboxes don't use form-control
        getContentWidgetElement().removeClassName(Styles.FORM_CONTROL);
        removeContentWidgetStyleName(Styles.FORM_CONTROL);
    }

    protected LabelElement getCheckboxWidgetLabel() {
        return LabelElement.as(Element.as(asCheckBox().getElement().getChild(1)));
    }

    @Override
    public WidgetWithLabelEditor<T, BaseEntityModelCheckboxEditor<T>> asEditor() {
        return editor;
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.NONE);
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        getValidatedWidgetStyle().setBorderStyle(BorderStyle.SOLID);
        getValidatedWidgetStyle().setBorderColor("red"); //$NON-NLS-1$
    }

    @Override
    protected Element getContentWidgetElement() {
        // Actual check box input element is the first child of CheckBox element
        Node input = asCheckBox().getElement().getChild(0);
        return Element.as(input);
    }

    protected Element getInternalLabelElement() {
        Node label = asCheckBox().getElement().getChild(1);
        return Element.as(label);
    }

    @Override
    protected void updateLabelElementId(String elementId) {
        if (useCheckBoxWidgetLabel) {
            LabelElement.as(Element.as(asCheckBox().getElement().getChild(1))).setHtmlFor(elementId);
        } else {
            super.updateLabelElementId(elementId);
        }
    }

    @Override
    public void hideLabel() {
        super.hideLabel();
        if (useCheckBoxWidgetLabel) {
            getCheckboxWidgetLabel().getStyle().setDisplay(Display.NONE);
        }
    }

    @Override
    public String getLabel() {
        if (useCheckBoxWidgetLabel) {
            return asCheckBox().getText();
        } else {
            return super.getLabel();
        }
    }

    public void useWidgetLabel() {
        useCheckBoxWidgetLabel = true;
    }

    @Override
    public void setLabel(String label) {
        if (useCheckBoxWidgetLabel) {
            asCheckBox().setText(label);
        } else {
            super.setLabel(label);
        }
    }

    @Override
    public void addLabelStyleName(String styleName) {
        if (useCheckBoxWidgetLabel) {
            getCheckboxWidgetLabel().addClassName(styleName);
        } else {
            super.addLabelStyleName(styleName);
        }
    }

    @Override
    public void setAddLabelStyleName(String styleName) {
        if (useCheckBoxWidgetLabel) {
            getCheckboxWidgetLabel().addClassName(styleName);
        } else {
            super.setAddLabelStyleName(styleName);
        }
    }

    @Override
    public void setLabelStyleName(String styleName) {
        if (useCheckBoxWidgetLabel) {
            getCheckboxWidgetLabel().setClassName(styleName);
        } else {
            super.setLabelStyleName(styleName);
        }
    }

    @Override
    public void removeLabelStyleName(String styleName) {
        if (useCheckBoxWidgetLabel) {
            getCheckboxWidgetLabel().removeClassName(styleName);
        } else {
            super.removeLabelStyleName(styleName);
        }
    }

}
