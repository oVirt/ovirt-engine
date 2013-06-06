package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.IntegerSlider;

import com.google.gwt.core.client.GWT;
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
public class EntityModelInputWithSlider extends Composite implements EditorWidget<Object, LeafValueEditor<Object>>, TakesValue<Object>,
        HasValueChangeHandlers<Object> {

    private static final CommonApplicationResources RESOURCES = GWT.create(CommonApplicationResources.class);

    private TakesValueWithChangeHandlersEditor<Object> editor;
    private TextBox textBox;
    private IntegerSlider slider;
    private Label maxValueLabel;
    private Label minValueLabel;

    public EntityModelInputWithSlider(int min, int max) {
        HorizontalPanel panel = new HorizontalPanel();

        textBox = new TextBox();
        textBox.setVisibleLength(2);

        minValueLabel = new Label(Integer.toString(min));
        minValueLabel.setStyleName("gwt-SliderBar-minrange-label"); //$NON-NLS-1$
        maxValueLabel = new Label(Integer.toString(max));
        maxValueLabel.setStyleName("gwt-SliderBar-maxrange-label"); //$NON-NLS-1$

        slider = new IntegerSlider(min, max, RESOURCES) {
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
                textBox.setValue(NumberFormat.getFormat("#").format(event.getValue())); //$NON-NLS-1$
            }
        });

        textBox.addValueChangeHandler(new InputWithSliderInputChangeHandler() {
            @Override
            public void onValueChange(final ValueChangeEvent<String> event) {
                if (isCorrectValue(event.getValue())) {
                    slider.setValue(Integer.parseInt(event.getValue()));
                } else {
                    // fallback when the passed value is not correct
                    textBox.setValue(Integer.toString(slider.getValue()), true);
                }
            }

        });

        panel.add(textBox);
        panel.setCellVerticalAlignment(textBox, HasVerticalAlignment.ALIGN_BOTTOM);
        panel.add(minValueLabel);
        panel.add(slider);
        panel.setCellWidth(slider, "80%"); //$NON-NLS-1$
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
        textBox.setEnabled(enabled);
    }

    @Override
    public Object getValue() {
        return asSlider().getValue();
    }

    @Override
    public void setValue(Object value) {
        asSlider().setValue(asInt(value));
        textBox.setValue(Integer.toString(asInt(value)));
    }

    private int asInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }

        return 0;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        return new CompositeHandleRegistration(
                textBox.addValueChangeHandler(new SelectiveValueChangeHandlerDecorator(handler)),
                asSlider().addValueChangeHandler(handler));
    }

    abstract class InputWithSliderInputChangeHandler implements ValueChangeHandler<String> {
        /**
         * Returns true, if the specified string is: <li>a double value <li>bigger or equal than the min value <li>
         * smaller or equal than the max value <li>reachable from min += stepSize <br>
         * In other case it returns false
         */
        protected boolean isCorrectValue(String newStringValue) {
            try {
                double newValue = Double.parseDouble(newStringValue);
                if (newValue <= slider.getMaxValue() && newValue >= slider.getMinValue()) {

                    // checks if it is an enabled value
                    for (double i = slider.getMinValue(); i <= newValue; i += slider.getStepSize()) {
                        if (Double.valueOf(i).equals(newValue)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

        }
    }

    /**
     * Calls the onValueChange on the decorated field only if the new value was correct (e.g. it will be preserved so it
     * makes sense to process it)
     */
    @SuppressWarnings("rawtypes")
    class SelectiveValueChangeHandlerDecorator extends InputWithSliderInputChangeHandler {

        private ValueChangeHandler decorated;

        public SelectiveValueChangeHandlerDecorator(ValueChangeHandler decorated) {
            this.decorated = decorated;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
            if (isCorrectValue(event.getValue())) {
                decorated.onValueChange(new StringToIntConvertingEvent(event, asInt(event.getValue())));
            }
        }

    }

    class StringToIntConvertingEvent extends ValueChangeEvent<Integer> {

        private final ValueChangeEvent<String> event;

        protected StringToIntConvertingEvent(ValueChangeEvent<String> event, Integer value) {
            super(value);
            this.event = event;
        }

        @Override
        public Object getSource() {
            return event.getSource();
        }

    }

    class CompositeHandleRegistration implements HandlerRegistration {

        private final HandlerRegistration[] registrations;

        public CompositeHandleRegistration(HandlerRegistration... registrations) {
            this.registrations = registrations;
        }

        @Override
        public void removeHandler() {
            if (registrations == null) {
                return;
            }

            for (HandlerRegistration registration : registrations) {
                registration.removeHandler();
            }
        }

    }

}
