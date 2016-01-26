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

    public static final String TEMPLATE = "<div class=\"tooltip\"><div class=\"tooltip-arrow\"></div><div class=\"tooltip-inner\"></div></div>"; //$NON-NLS-1$
    public static final boolean IS_ANIMATED = true;
    public static final boolean IS_HTML = true;
    public static final Placement PLACEMENT = Placement.TOP;
    public static final Trigger TRIGGER = Trigger.HOVER;
    public static final String CONTAINER = "body"; //$NON-NLS-1$
    public static final int HIDE_DELAY_MS = 0;
    public static final int SHOW_DELAY_MS = 500;

}
