package org.ovirt.engine.ui.common.widget;

import com.google.gwt.user.client.ui.Widget;

/**
 * Gets the desired visibility and does some post calculation to get the real visibility (e.g. some field is switched to
 * be visible by UICommon but it has to be hidden because it is visible in advanced mode only)
 */
public interface VisibilityRenderer {

    boolean render(Widget source, boolean desiredVisibility);

    public static class SimpleVisibilityRenderer implements VisibilityRenderer {

        @Override
        public boolean render(Widget source, boolean desiredVisibility) {
            return desiredVisibility;
        }

    }
}
