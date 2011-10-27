package org.ovirt.engine.ui.webadmin.widget.renderer;

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

        String reply = "";

        if (days > 0) {
            reply += days + (days == 1 ? " day " : " days ");
        } else if (hours > 0) {
            reply += hours + " h ";
        } else if (minutes > 0) {
            reply += minutes + " min ";
        } else if (seconds > 0) {
            reply += seconds + " sec";
        }

        return reply;
    }

}
