package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.widget.AbstractValidatedWidgetWithLabel;
import org.ovirt.engine.ui.common.widget.Align;

import com.google.gwt.editor.client.IsEditor;

/**
 * A composite Editor which uses {@link EntityModelInputWithSlider}.
 */
public class EntityModelSliderWithTextBoxEditor extends AbstractValidatedWidgetWithLabel<Object, EntityModelInputWithSlider>
        implements IsEditor<WidgetWithLabelEditor<Object, EntityModelSliderWithTextBoxEditor>> {

    private final WidgetWithLabelEditor<Object, EntityModelSliderWithTextBoxEditor> editor;

    public EntityModelSliderWithTextBoxEditor(int min, int max) {
        this(Align.RIGHT, min, max);
    }

    public EntityModelSliderWithTextBoxEditor(Align labelAlign, int min, int max) {
        super(new EntityModelInputWithSlider(min, max));
        this.editor = WidgetWithLabelEditor.of(getContentWidget().asEditor(), this);
    }

    @Override
    public WidgetWithLabelEditor<Object, EntityModelSliderWithTextBoxEditor> asEditor() {
        return editor;
    }

    public void setMin(int min) {
        getContentWidget().asSlider().setMinValue(min);
    }

    public void setMax(int max) {
        getContentWidget().asSlider().setMaxValue(max);
    }

    public void setStepSize(int stepSize) {
        getContentWidget().asSlider().setStepSize(stepSize);
    }

    @Override
    protected void applyCommonValidationStyles() {
        // Needed in order not to have the border in the element style
    }

}
