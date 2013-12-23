package org.ovirt.engine.ui.webadmin.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;


public class HumanReadableTimeRenderer extends AbstractRenderer<Double> {
    private static final int SECONDS_IN_A_MINUTE = 60;
    private static final int SECONDS_IN_AN_HOUR = SECONDS_IN_A_MINUTE * 60;
    private static final int SECONDS_IN_A_DAY = SECONDS_IN_AN_HOUR * 24;
    @Override
    public String render(Double time) {
        StringBuilder formatTime = new StringBuilder();

        if ( time > SECONDS_IN_A_DAY) {
            formatTime.append((int) (time/SECONDS_IN_A_DAY));
            formatTime.append(" days " );//$NON-NLS-1$
            time = time % SECONDS_IN_A_DAY;
        }
        if( time > SECONDS_IN_AN_HOUR ) {
            formatTime.append((int) (time/SECONDS_IN_AN_HOUR));
            formatTime.append(" h ");//$NON-NLS-1$
            time = time % SECONDS_IN_AN_HOUR;
        }
        if( time > SECONDS_IN_A_MINUTE) {
            formatTime.append((int) (time/SECONDS_IN_A_MINUTE));
            formatTime.append(" m ");//$NON-NLS-1$
            time = time % SECONDS_IN_A_MINUTE;
        }
        if(time >= 0){
            formatTime.append(time.intValue());
            formatTime.append(" s ");//$NON-NLS-1$
        }

        return formatTime.toString();
    }

}
