package org.ovirt.engine.ui.common.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for uptime strings.
 */
public class UptimeRenderer extends AbstractRenderer<Double> {

    private static final int SECONDS_IN_A_MINUTE = 60;
    private static final int SECONDS_IN_AN_HOUR = SECONDS_IN_A_MINUTE * 60;
    private static final int SECONDS_IN_A_DAY = SECONDS_IN_AN_HOUR * 24;

    @Override
    public String render(Double data) {
        int totalSeconds = data.intValue();

        int days = totalSeconds / SECONDS_IN_A_DAY;
        int hours = (totalSeconds % SECONDS_IN_A_DAY) / SECONDS_IN_AN_HOUR;
        int minutes = (totalSeconds % SECONDS_IN_AN_HOUR) / SECONDS_IN_A_MINUTE;
        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;

        String reply = ""; //$NON-NLS-1$

        if (days > 0) {
            reply += days + (days == 1 ? " day " : " days "); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (hours > 0) {
            reply += hours + " h "; //$NON-NLS-1$
        } else if (minutes > 0) {
            reply += minutes + " min "; //$NON-NLS-1$
        } else if (seconds > 0) {
            reply += seconds + " sec"; //$NON-NLS-1$
        }

        return reply;
    }

}
