package org.ovirt.engine.ui.common.widget.tooltip;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.base.HasHover;
import org.gwtbootstrap3.client.ui.base.HasId;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.ovirt.engine.ui.common.utils.ElementUtils;
import org.ovirt.engine.ui.common.utils.JqueryUtils;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;

/**
 * <p>
 * Implementation of Bootstrap tooltips that are capable of wrapping non-GWT elements.
 * This is designed primarily for use in grids, but could be used in any Cell. Since
 * Cells don't support Widget's event system, we must use a reaper timer to check for
 * when a tooltip's Element has been detached from the document.
 * </p>
 * <p>
 * Bootstrap tooltips use jQuery under the hood. jQuery must be present for this widget
 * to function.
 * </p>
 * <p>
 * Inspired by gwtbootstrap3's Tooltip.
 * </p>
 * <p>
 * <br/>
 * See also: <br/>
 * <a href="https://github.com/gwtbootstrap3/gwtbootstrap3">https://github.com/gwtbootstrap3/gwtbootstrap3</a><br/>
 * <a href="http://getbootstrap.com/javascript/#tooltips">Bootstrap Documentation</a>
 * <br/>
 * See also: <br/>
 * {@link Tooltip}
 * </p>
 * <p>
 * ** Must call reconfigure() after altering any/all Tooltips!
 * </p>
 */
public class ElementTooltip implements HasId, HasHover {
    private static final String TOGGLE = "toggle"; //$NON-NLS-1$
    private static final String SHOW = "show"; //$NON-NLS-1$
    private static final String HIDE = "hide"; //$NON-NLS-1$
    private static final String DESTROY = "destroy"; //$NON-NLS-1$

    private boolean isAnimated = TooltipConfig.IS_ANIMATED;
    private boolean isHTML = TooltipConfig.IS_HTML;
    private Placement placement = TooltipConfig.PLACEMENT;
    private Trigger trigger = TooltipConfig.TRIGGER;
    private SafeHtml content = SafeHtmlUtils.EMPTY_SAFE_HTML;
    private String container = TooltipConfig.CONTAINER;
    private final String selector = null;

    private int hideDelayMs = TooltipConfig.HIDE_DELAY_MS;
    private int showDelayMs = TooltipConfig.SHOW_DELAY_MS;
    private int reaperInterval = 200;
    private Timer reaperTimer = null;

    private Element element;
    private String id;

    private static final Logger logger = Logger.getLogger(ElementTooltip.class.getName());

    /**
     * A static registry of all tooltips in existence. Keyed by the id of the element the
     * tooltip is bound to.
     */
    private static Map<String, ElementTooltipDetails> tooltipRegistry = new HashMap<>();

    /**
     * Creates an empty ElementTooltip
     */
    public ElementTooltip() {
    }

    /**
     * Creates the tooltip around this element
     *
     * @param e Element for the tooltip
     */
    public ElementTooltip(final Element e) {
        setElement(e);
    }

    /**
     * Sets the Element that this tooltip hovers over.
     * @param e Element for the tooltip
     */
    public void setElement(final Element e) {
        element = e;
        bindJavaScriptEvents(element);
    }

    /**
     * Return the Element that this tooltip hovers over.
     */
    public Element getElement() {
        return element;
    }

    /**
     * Return the tooltip registry.
     */
    public static Map<String, ElementTooltipDetails> getRegistry() {
        return tooltipRegistry;
    }

    /**
     * Return a tooltip.
     */
    public static ElementTooltip getTooltip(String id) {
        if (isTooltipConfigured(id)) {
            return tooltipRegistry.get(id).getTooltip();
        }
        return null;
    }

    /**
     * Is a tooltip in the registry for this id?
     */
    public static boolean isTooltipConfigured(String id) {
        logger.finer("checking tooltip registry for " + id); //$NON-NLS-1$

        if (id == null || id.isEmpty()) {
            return false;
        }
        return ElementTooltip.getRegistry().containsKey(id);
    }

    /**
     * Return the reaper interval.
     */
    public int getReaperInterval() {
        return reaperInterval;
    }

    /**
     * Sets the reaper interval.
     */
    public void setReaperInterval(int reaperInterval) {
        this.reaperInterval = reaperInterval;
    }

    /**
     * <p>
     * Starts a timer that checks for this tooltip to be hanging open.
     * This can happen when the Element or one of its ancestors is detached from the document.
     * Such detaching happens frequently when Grids are refreshed -- GWT replaces an entire
     * grid row, but doesn't actually delete the row. The row just gets removed from its parent
     * table. To detect this, we search up the ancestor tree for a null ancestor.
     * </p>
     * <p>
     * This should only be called for a tooltip when it is shown. It will be reaped very quickly,
     * within 5 seconds. And since only visible tooltips start the timer, the timer won't run that
     * much. In other words, this should not be a performance concern.
     * </p>
     */
    public void startHangingTooltipReaper() {
        if (reaperTimer != null && reaperTimer.isRunning()) {
            return;
        }

        reaperTimer = new Timer() {

            @Override
            public void run() {
                logger.finer("reaper timer"); //$NON-NLS-1$
                if (hasNullAncestor()) {
                    reap();
                    cancel(); // cancel this timer since this tooltip is dead
                }
            }
        };

        reaperTimer.scheduleRepeating(getReaperInterval());
    }

    /**
     * Check up this Elements ancestor tree, and return true if we find a null parent.
     * @return true if a null parent is found, false if this Element has body as an ancestor
     * (meaning it's still attached).
     */
    public boolean hasNullAncestor() {
        return ElementUtils.hasNullAncestor(element);
    }

    /**
     * Walk through the tooltip registry and hide and delete any tooltips who are orphaned.
     */
    public static void reapAll() {
        for (Iterator<Entry<String, ElementTooltipDetails>> i = tooltipRegistry.entrySet().iterator(); i.hasNext(); ) {
            Entry<String, ElementTooltipDetails> entry = i.next();
            ElementTooltip tooltip = entry.getValue().getTooltip();
            if (tooltip.hasNullAncestor()) {
                tooltip.hide();
                i.remove();
            }
        }
    }

    /**
     * Hide all tooltips.
     */
    public static void hideAll() {
        for (Iterator<Entry<String, ElementTooltipDetails>> i = tooltipRegistry.entrySet().iterator(); i.hasNext(); ) {
            Entry<String, ElementTooltipDetails> entry = i.next();
            ElementTooltip tooltip = entry.getValue().getTooltip();
            tooltip.hide();
        }
    }

    /**
     * <p>
     * Reap this tooltip. That is, hide it and remove it from the registry.
     * </p>
     * <p>
     * Tooltips get reaped primarily when GWT removes their elements from the Document
     * (happens every 5 seconds in grid refreshes, for example). They are watching for mouseover
     * on an element that will never be visible again. So we make sure they are hidden and deleted
     * from the registry.
     * </p>
     * <p>
     * The reap *won't* happen if the mouse cursor is currently over an element that is
     * identical to this tooltip's element. In other words, if I'm hovered over an icon in
     * a grid, and GWT redraws the grid and puts an identical icon where the old one was,
     * leave this open tooltip showing. (It'll end up getting reaped when that cell is
     * moused-out of via reapAll().
     */
    public void reap() {
        for (Iterator<Entry<String, ElementTooltipDetails>> i = tooltipRegistry.entrySet().iterator(); i.hasNext(); ) {
            Entry<String, ElementTooltipDetails> entry = i.next();
            ElementTooltip tooltip = entry.getValue().getTooltip();
            if (this.equals(tooltip)) {

                String[] pos = JqueryUtils.getMousePosition().split(","); //$NON-NLS-1$
                int x = Integer.parseInt(pos[0]);
                int y = Integer.parseInt(pos[1]);

                // can we find a replacement element using mouse coordinates?
                Element replacement = ElementUtils.getElementFromPoint(x, y);
                if (replacement == null) {
                    logger.finer("can't detect potential replacement element. reaping"); //$NON-NLS-1$
                    hide();
                    i.remove();
                    return;
                }

                // we found a potential replacement element. compare the html to see if this
                // element is identical to the old one.
                String html = entry.getValue().getInnerHTML();
                if (html.contains(replacement.getInnerHTML())) {
                    // yep, element was replaced with an identical element. DON'T reap.
                    logger.finer("mouse is hovering over my replacement. not reaping"); //$NON-NLS-1$
                }
                else {
                    logger.finer("mouse is hovering over an element, but it's not identical to previous " //$NON-NLS-1$
                            + "element. reaping"); //$NON-NLS-1$
                    hide();
                    i.remove();
                    return;
                }

                return;
            }
        }
    }

    /**
     * Called when the tooltip is shown. Starts the hanging tooltip reaper timer.
     *
     * This method also does two very important checks.
     *
     * First, it checks for "orphaned tooltips." Because of the high-refresh nature of our application,
     * tooltipped Elements are often deleted before the tooltip can finish its render. So we check for
     * this condition, and simply cancel the render if it is true. Then we fire another mouseover event
     * at the current mouse coordinates, assuming the mouse is over the refreshed Element. No harm done
     * if the mouse has moved away.
     *
     * Second, it checks for "abandoned tooltips." mouseover and mouseout are not perfect in any browser.
     * Occasionally a mouseover will trigger a tooltip render, and then the user will quickly move the
     * mouse away -- but for some reason the mouseout doesn't fire. In this case, a tooltip will render
     * over an Element the mouse is no longer over. So we check for this condition, and simply cancel
     * the render if it is true.
     *
     * If any issues with hanging tooltips creep up, start debugging here :)
     *
     * @param event Event
     */
    protected void onShow(final Event event) {

        // handle the case where the element i'm attached to was just removed from the document ("orphaned tooltip")
        if (hasNullAncestor()) {
            event.preventDefault();
            logger.finer("orphaned tooltip. canceling render, re-firing mouseover"); //$NON-NLS-1$

            // trigger another mouseover on the new element
            String[] pos = JqueryUtils.getMousePosition().split(","); //$NON-NLS-1$
            int x = Integer.parseInt(pos[0]);
            int y = Integer.parseInt(pos[1]);
            Element replacement = ElementUtils.getElementFromPoint(x, y);
            if (replacement != null) {

                // TODO-GWT use mouseenter if it ever becomes supported
                NativeEvent newEvent = Document.get().createMouseOverEvent(0, event.getScreenX(),
                        event.getScreenY(), event.getClientX(), event.getClientY(), event.getCtrlKey(),
                        event.getAltKey(), event.getShiftKey(), event.getMetaKey(), event.getButton(), replacement);

                replacement.dispatchEvent(newEvent);
                return;
            }
        }
        // handle the case where the mouse is not over me anymore ("abandoned tooltip")
        else {
            String[] pos = JqueryUtils.getMousePosition().split(","); //$NON-NLS-1$
            int x = Integer.parseInt(pos[0]);
            int y = Integer.parseInt(pos[1]);

            Element currentElement = ElementUtils.getElementFromPoint(x, y);
            Element target = Element.as(event.getEventTarget());

            if (!ElementUtils.hasAncestor(currentElement, target)) {
                logger.finer("abandoned tooltip. canceling render."); //$NON-NLS-1$
                event.preventDefault();
            }
        }

        logger.finer("starting tooltip reaper"); //$NON-NLS-1$
        this.startHangingTooltipReaper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(final String id) {
        this.id = id;
        if (element != null) {
            element.setId(id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return (element == null) ? id : element.getId();
    }

    @Override
    public void setIsAnimated(final boolean isAnimated) {
        this.isAnimated = isAnimated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnimated() {
        return isAnimated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsHtml(final boolean isHTML) {
        this.isHTML = isHTML;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHtml() {
        return isHTML;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPlacement(final Placement placement) {
        this.placement = placement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Placement getPlacement() {
        return placement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTrigger(final Trigger trigger) {
        this.trigger = trigger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShowDelayMs(final int showDelayMs) {
        this.showDelayMs = showDelayMs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getShowDelayMs() {
        return showDelayMs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHideDelayMs(final int hideDelayMs) {
        this.hideDelayMs = hideDelayMs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHideDelayMs() {
        return hideDelayMs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContainer(final String container) {
        this.container = container;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContainer() {
        return container;
    }

    /**
     * Sets the tooltip's HTML content
     */
    public void setContent(final SafeHtml content) {
        this.isHTML = true;
        this.content = content != null ? content : SafeHtmlUtils.EMPTY_SAFE_HTML;
    }

    /**
     * Reconfigures the tooltip, must be called when altering any tooltip after it has already been shown
     */
    public void reconfigure() {
        // First destroy the old tooltip
        destroy();

        // Setup the new tooltip
        if (container != null && selector != null) {
            tooltip(element, isAnimated, isHTML, placement.getCssName(), selector, content.asString(),
                    trigger.getCssName(), showDelayMs, hideDelayMs, container);
        } else if (container != null) {
            tooltip(element, isAnimated, isHTML, placement.getCssName(), content.asString(),
                    trigger.getCssName(), showDelayMs, hideDelayMs, container);
        } else if (selector != null) {
            tooltip(element, isAnimated, isHTML, placement.getCssName(), selector, content.asString(),
                    trigger.getCssName(), showDelayMs, hideDelayMs);
        } else {
            tooltip(element, isAnimated, isHTML, placement.getCssName(), content.asString(),
                    trigger.getCssName(), showDelayMs, hideDelayMs);
        }
    }

    /**
     * Toggle the Tooltip to either show/hide
     */
    public void toggle() {
        call(element, TOGGLE);
    }

    /**
     * <p>
     * Force show the Tooltip. If you must, you should probably wrap this call in a Timer delay
     * of getShowDelayMs() ms.
     * </p>
     * <p>
     * This is generally flaky though. Better to fire a mouseover (or mouseenter) event at the tooltip's element.
     * </p>
     */
    public void show() {
        logger.finer("tooltip show on element id " + element.getId()); //$NON-NLS-1$
        call(element, SHOW);
    }

    /**
     * Force hide the Tooltip
     */
    public void hide() {
        call(element, HIDE);
    }

    /**
     * Force the Tooltip to be destroyed
     */
    public void destroy() {
        call(element, DESTROY);
    }

    private native void call(final Element e, final String arg) /*-{
        $wnd.jQuery(e).tooltip(arg);
    }-*/;

    // @formatter:off
    private native void bindJavaScriptEvents(final Element e) /*-{
        var target = this;
        var $tooltip = $wnd.jQuery(e);

        $tooltip.on('show.bs.tooltip', function (evt) {
            target.@org.ovirt.engine.ui.common.widget.tooltip.ElementTooltip::onShow(Lcom/google/gwt/user/client/Event;)(evt);
        });
    }-*/;

    private native void tooltip(Element e, boolean animation, boolean html, String placement, String selector,
                                String content, String trigger, int showDelay, int hideDelay, String container) /*-{
        $wnd.jQuery(e).tooltip({
            animation: animation,
            html: html,
            placement: placement,
            selector: selector,
            title: content,
            trigger: trigger,
            delay: {
                show: showDelay,
                hide: hideDelay
            },
            container: container
        });
    }-*/;

    private native void tooltip(Element e, boolean animation, boolean html, String placement,
                                String content, String trigger, int showDelay, int hideDelay, String container) /*-{
        $wnd.jQuery(e).tooltip({
            animation: animation,
            html: html,
            placement: placement,
            title: content,
            trigger: trigger,
            delay: {
                show: showDelay,
                hide: hideDelay
            },
            container: container
        });
    }-*/;

    private native void tooltip(Element e, boolean animation, boolean html, String placement, String selector,
                                String content, String trigger, int showDelay, int hideDelay) /*-{
        $wnd.jQuery(e).tooltip({
            animation: animation,
            html: html,
            placement: placement,
            selector: selector,
            title: content,
            trigger: trigger,
            delay: {
                show: showDelay,
                hide: hideDelay
            }
        });
    }-*/;

    private native void tooltip(Element e, boolean animation, boolean html, String placement,
                                String content, String trigger, int showDelay, int hideDelay) /*-{
        $wnd.jQuery(e).tooltip({
            animation: animation,
            html: html,
            placement: placement,
            title: content,
            trigger: trigger,
            delay: {
                show: showDelay,
                hide: hideDelay
            }
        });
    }-*/;

}
