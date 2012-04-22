package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer that sums up Double values.
 */
public class SumUpRenderer extends AbstractRenderer<Double[]> {

    @Override
    public String render(Double[] values) {
        double sum = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                return "[N/A]"; //$NON-NLS-1$
            }
            sum += values[i];
        }

        int intVal = (int) sum;
        return Integer.toString(intVal);
    }

}
