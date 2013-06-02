package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.CommonApplicationResources;

/**
 * Convenience {@link SliderBar} with Integer values and 10 steps.
 */
public class IntegerSlider extends SliderBar {

    public static class IntegerFormatter implements LabelFormatter {

        @Override
        public String formatLabel(SliderBar slider, double value) {
            return String.valueOf((int) value);
        }
    }

    public IntegerSlider(int minValue, int maxValue,
            CommonApplicationResources resources) {
        this(minValue, maxValue, new IntegerFormatter(), resources);
    }

    public IntegerSlider(int minValue, int maxValue, LabelFormatter formatter,
            CommonApplicationResources resources) {
        super(minValue, maxValue, formatter, resources);
        this.setNumLabels(9);
        this.setNumTicks(9);
        this.setStepSize(((double) maxValue - (double) minValue + 1f) / 10f);
        this.setPixelSize(166, 30);
    }

}
