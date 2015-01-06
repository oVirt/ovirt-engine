package org.ovirt.engine.ui.common.widget.tooltip;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;

/**
 * Constant configuration values shared across all tooltips.
 */
public class TooltipConfig {

    public enum Width {
        W220 ("tooltip-w220"), //$NON-NLS-1$
        W320 ("tooltip-w320"), //$NON-NLS-1$
        W420 ("tooltip-w420"), //$NON-NLS-1$
        W520 ("tooltip-w520"), //$NON-NLS-1$
        W620 ("tooltip-w620"); //$NON-NLS-1$

        private final String widthClass; // in px

        Width(String widthClass) {
            this.widthClass = widthClass;
        }

        public String getWidthClass() {
            return widthClass;
        }
    }

    public final static boolean IS_ANIMATED = true;
    public final static boolean IS_HTML = true;
    public final static Placement PLACEMENT = Placement.TOP;
    public final static Trigger TRIGGER = Trigger.HOVER;
    public final static String CONTAINER = "body"; //$NON-NLS-1$
    public final static int HIDE_DELAY_MS = 0;
    public final static int SHOW_DELAY_MS = 500;

}
