package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.common.CommonApplicationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Render the refresh panel 'seconds' indicator.
 */
public class MillisecondRenderer extends AbstractRenderer<Integer> {
    /**
     * GWT CommonApplicationConstants to get sec label.
     */
    private static final CommonApplicationMessages MESSAGES = GWT.create(CommonApplicationMessages.class);
    /**
     * What to divide a number by to turn milli seconds into seconds.
     */
    private static final Integer MILLI_SECOND_DIVISION = 1000;
    /**
     * The object instance.
     */
    private static final MillisecondRenderer INSTANCE = new MillisecondRenderer();

    /**
     * Get the object instance.
     * @return {@code MilisecondRenderer} object instance.
     */
    public static MillisecondRenderer getInstance() {
        return INSTANCE;
    }

    @Override
    public String render(final Integer milliSeconds) {
        return MESSAGES.refreshRateSeconds(milliSeconds / MILLI_SECOND_DIVISION);
    }

}
