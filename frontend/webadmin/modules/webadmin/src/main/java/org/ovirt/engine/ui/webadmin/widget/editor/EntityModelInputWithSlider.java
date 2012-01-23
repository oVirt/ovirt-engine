package org.ovirt.engine.ui.webadmin.widget.editor;

import org.ovirt.engine.ui.webadmin.widget.IntegerSlider;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * This class extends Composite instead of InputText and embeds a slider widget for setting its value.
 */
public class EntityModelInputWithSlider extends Composite implements
        EditorWidget<Object, LeafValueEditor<Object>>, TakesValue<Object>,
        HasValueChangeHandlers<Object> {

    private TakesValueWithChangeHandlersEditor<Object> editor;
    private TextBox t;
    private IntegerSlider slider;
    private Label maxValueLabel;
    private Label minValueLabel;

    public EntityModelInputWithSlider(int min, int max) {
        HorizontalPanel panel = new HorizontalPanel();

        t = new TextBox();
        t.setEnabled(false);
        t.setVisibleLength(2);

        minValueLabel = new Label(Integer.toString(min));
        minValueLabel.setStyleName("gwt-SliderBar-minrange-label");
        maxValueLabel = new Label(Integer.toString(max));
        maxValueLabel.setStyleName("gwt-SliderBar-maxrange-label");

        slider = new IntegerSlider(min, max) {
            @Override
            public void setMinValue(double minValue) {
                super.setMinValue(minValue);
                minValueLabel.setText(Integer.toString(Double.valueOf(minValue).intValue()));
            }

            @Override
            public void setMaxValue(double maxValue) {
                super.setMaxValue(maxValue);
                maxValueLabel.setText(Integer.toString(Double.valueOf(maxValue).intValue()));
            }

            @Override
            public void setStepSize(double stepSize) {
                super.setStepSize(stepSize);
                setNumTicks((Double.valueOf((getMaxValue() - getMinValue()) / stepSize).intValue()));
            };
        };

        slider.setNumTicks(max - 1);
        slider.setStepSize(1);
        slider.setNumLabels(0);
        slider.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                t.setValue(NumberFormat.getFormat("#").format(event.getValue()));
            }
        });

        panel.add(t);
        panel.setCellVerticalAlignment(t, HasVerticalAlignment.ALIGN_BOTTOM);
        panel.add(minValueLabel);
        panel.add(slider);
        panel.setCellWidth(slider, "80%");
        panel.add(maxValueLabel);

        initWidget(panel);
    }

    @Override
    public TakesValueWithChangeHandlersEditor<Object> asEditor() {
        if (editor == null) {
            editor = TakesValueWithChangeHandlersEditor.of(this, this);
        }
        return editor;
    }

    public IntegerSlider asSlider() {
        return slider;
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return asSlider().addKeyUpHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return asSlider().addKeyDownHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return asSlider().addKeyPressHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return asSlider().getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        asSlider().setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        asSlider().setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        asSlider().setTabIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return asSlider().isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        asSlider().setEnabled(enabled);
    }

    @Override
    public Object getValue() {
        return asSlider().getValue();
    }

    @Override
    public void setValue(Object value) {
        asSlider().setValue((Integer) value);
        t.setValue(value.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        return asSlider().addValueChangeHandler(handler);
    }
}
