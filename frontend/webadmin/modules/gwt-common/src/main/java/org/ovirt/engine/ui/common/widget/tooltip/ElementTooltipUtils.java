package org.ovirt.engine.ui.common.widget.tooltip;

import java.util.Set;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;

/**
 * jQuery/Bootstrap tooltip utility methods.
 *
 * @see org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip
 */
public class ElementTooltipUtils {

    public static void addTooltipsEvents(Set<String> set) {
        set.add(BrowserEvents.MOUSEOVER);
    }

    /**
     * Apply tooltip on the given element.
     */
    public static void setTooltipOnElement(String tooltip, Element element) {
        if (tooltip!= null) {
            element.setTitle(tooltip);
        }
        else {
            element.setTitle(""); //$NON-NLS-1$
        }
    }
}
