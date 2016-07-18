package org.ovirt.engine.ui.common.widget.tooltip;

import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.ui.common.utils.JqueryUtils;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;

/**
 *
 * Set of static methods used by tooltip-capable Cells to group the tooltip logic in one place.
 *
 * TODO When we have Java 8 mixins, consider using those in place of this class.
 *
 * There is currently some GWT or ovirt bug causing duplicate mouseover and mouseout events.
 * It doesn't affect the logic, but be aware of it when working on this code.
 *
 */
public class TooltipMixin {

    private static final Logger logger = Logger.getLogger(TooltipMixin.class.getName());

    /**
     * Add events that tooltips care about (over, down, out) to a cell's Set of sunk events.
     */
    public static void addTooltipsEvents(Set<String> set) {
        set.add(BrowserEvents.MOUSEOVER);
        set.add(BrowserEvents.MOUSEOUT);
        set.add(BrowserEvents.MOUSEDOWN);
    }

    /**
     * Hijack mouseover event and use it to create the tooltip. This is done the first time a Cell is moused-over
     * because that's the only place GWT gives us access to the actual Element.
     *
     * Once the tooltip is configured, we need to fire a new mouseenter event at the cell so jQuery can pick it
     * up and show the tooltip.
     */
    public static void configureTooltip(final Element parent, SafeHtml tooltipContent, final NativeEvent event) {
        if (tooltipContent == null || tooltipContent.asString().trim().isEmpty()) {
            // there is no tooltip for this render. no-op.
            logger.finer("null or empty tooltip content"); //$NON-NLS-1$
        }
        else if (isTooltipConfigured(parent)) {
            logger.finer("tooltip already configured"); //$NON-NLS-1$

            // should this bad tooltip be showing and it's not?
            checkForceShow(event);
        }
        else {

            logger.finer("tooltip not configured yet -- adding"); //$NON-NLS-1$
            addTooltipToElement(tooltipContent, parent);

            logger.finer("firing native event to jquery tooltip"); //$NON-NLS-1$

            // kill this event -- we abused it to configure the tooltip
            event.stopPropagation();
            event.preventDefault();

            // and fire another event for jquery to handle
            Node node = parent.getChild(0);
            if (node instanceof Element) {
                Element e = (Element) node;

                NativeEvent newEvent = Document.get().createMouseOverEvent(0, event.getScreenX(),
                        event.getScreenY(), event.getClientX(), event.getClientY(), event.getCtrlKey(),
                        event.getAltKey(), event.getShiftKey(), event.getMetaKey(), event.getButton(), e);
                e.dispatchEvent(newEvent);
            }
        }
    }

    public static void reapAllTooltips() {
        // all tooltips should be reaped
        ElementTooltip.reapAll();
    }

    public static void hideAllTooltips() {
        // all tooltips should be hidden
        ElementTooltip.hideAll();
    }

    public static void handleTooltipEvent(Element parent, SafeHtml tooltipContent, NativeEvent event) {

        if (BrowserEvents.MOUSEOVER.equals(event.getType())) {
            configureTooltip(parent, tooltipContent, event);
        }

        if (BrowserEvents.MOUSEOUT.equals(event.getType())) {
            reapAllTooltips();
        }

        if (BrowserEvents.MOUSEDOWN.equals(event.getType())) {
            hideAllTooltips();
        }
    }

    public static ElementTooltip addTooltipToElement(SafeHtml tooltipContent, Element element, Placement placement) {
        ElementTooltip tooltip = new ElementTooltip(element);

        tooltip.setContent(tooltipContent);
        tooltip.setPlacement(placement);
        tooltip.reconfigure();

        String cellId = element.getId();
        if (cellId == null || cellId.isEmpty()) {
            cellId = DOM.createUniqueId();
            element.setId(cellId);
        }

        // add tooltip to registry -- key by element-id, save the tooltip and the html of the element
        ElementTooltipDetails details = new ElementTooltipDetails();
        details.setTooltip(tooltip);
        details.setInnerHTML(element.getInnerHTML());
        ElementTooltip.getRegistry().put(cellId, details);

        return tooltip;
    }

    public static ElementTooltip addTooltipToElement(SafeHtml tooltipContent, Element element) {
        return addTooltipToElement(tooltipContent, element, TooltipConfig.PLACEMENT);
    }

    public static boolean isTooltipConfigured(Element parent) {
        return ElementTooltip.isTooltipConfigured(parent.getId());
    }

    public static void updateTooltipContent(SafeHtml newContent, Element element) {
        ElementTooltip toolTip = ElementTooltip.getTooltip(element.getId());
        toolTip.setContent(newContent);
        toolTip.reconfigure();
    }

    /**
     * mouseover and mouseout aren't perfect
     * so give tooltip some time (50ms) to show, and then check to see if we should force show it
     * TODO-GWT try using mouseenter and mouseleave, if GWT adds support for these.
     */
    public static void checkForceShow(final NativeEvent event) {
        Timer timer = new Timer() {
            @Override
            public void run() {
                String[] pos = JqueryUtils.getMousePosition().split(","); //$NON-NLS-1$
                int x = Integer.parseInt(pos[0]);
                int y = Integer.parseInt(pos[1]);

                logger.finer("checking for force show. any tooltip visible? " + JqueryUtils.anyTooltipVisible()); //$NON-NLS-1$
                if (!JqueryUtils.anyTooltipVisible()) {
                    logger.finer("force showing closed tooltip"); //$NON-NLS-1$
                    JqueryUtils.fireMouseEnter(x, y);
                }
            }
        };
        timer.schedule(50);
    }

}
