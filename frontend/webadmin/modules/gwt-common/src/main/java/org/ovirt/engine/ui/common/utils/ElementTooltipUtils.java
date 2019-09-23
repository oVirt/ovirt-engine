package org.ovirt.engine.ui.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.widget.tooltip.TooltipConfig;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * jQuery/Bootstrap tooltip utility methods.
 *
 * @see org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip
 */
public final class ElementTooltipUtils {

    public static class CellWidgetTooltipReaper implements RepeatingCommand {

        private static final int REAPER_PERIOD = 10000; // ms

        public CellWidgetTooltipReaper() {
            Scheduler.get().scheduleFixedDelay(this, REAPER_PERIOD);
        }

        @Override
        public boolean execute() {
            ElementTooltipUtils.reapCellWidgetTooltips();
            return true;
        }

    }

    public static class TooltipHideOnRootPanelClick {

        public TooltipHideOnRootPanelClick() {
            RootPanel.get().addDomHandler(event -> ElementTooltipUtils.hideAllTooltips(), MouseDownEvent.getType());
        }
    }

    // Reaper lists to track elements on which the tooltip must be destroyed.
    // These reaper lists basically represent different categories of non-singleton
    // DOM elements that require manual tooltip destruction to prevent memory leaks.
    private static final List<Element> cellWidgetElementReapList = new ArrayList<>();
    private static final List<Element> popupContentElementReapList = new ArrayList<>();

    /**
     * Apply tooltip on the given element. Uses default config.
     */
    public static void setTooltipOnElement(Element e, SafeHtml tooltip) {
        setTooltipOnElement(e, tooltip, new TooltipConfig());
    }

    /**
     * Apply tooltip on the given element. Uses default config.
     */
    public static void setTooltipOnElement(Element e, SafeHtml tooltip, Placement placement) {
        setTooltipOnElement(e, tooltip, new TooltipConfig().setPlacement(Collections.singletonList(placement)));
    }

    /**
     * Apply tooltip on the given element or replace existing text if tooltip already exists
     */
    public static void setOrReplaceTooltipOnElement(Element e, SafeHtml tooltip, TooltipConfig config) {

        // Try not to set the same tooltip again.
        if (sameTooltipOnElement(e, tooltip)) {
            return;
        }

        // if tooltip already exists, just replace the text
        if (hasTooltip(e)) {
            replaceTooltipContent(e, getTooltipHtmlString(tooltip));
            return;
        }

        setTooltipOnElement(e, tooltip, config);
    }

    /**
     * Apply tooltip on the given element.
     */
    public static void setTooltipOnElement(Element e, SafeHtml tooltip, TooltipConfig config) {
        // Try not to set (destroy & create) the same tooltip again.
        if (sameTooltipOnElement(e, tooltip)) {
            return;
        }

        // Destroy existing tooltip first.
        destroyTooltip(e);

        // Create new tooltip.
        String tooltipHtmlString = getTooltipHtmlString(tooltip);
        if (!tooltipHtmlString.isEmpty()) {
            String placement =
                    config.getPlacement()
                            .stream()
                            .map(Placement::getCssName)
                            .collect(Collectors.joining(" ")); //$NON-NLS-1$
            createTooltipImpl(e, tooltipHtmlString,
                    placement,
                    config.getTooltipTemplate(),
                    config.isForceShow(),
                    config.isSanitizeContent());

            // Update reaper lists.
            if (config.isForCellWidgetElement()) {
                cellWidgetElementReapList.add(e);
            } else if (isPopupContentElement(e)) {
                popupContentElementReapList.add(e);
            }
        }
    }

    private static boolean sameTooltipOnElement(Element e, SafeHtml maybeNewTooltip) {
        String existingTooltipHtmlString = getExistingTooltipHtml(e);
        String maybeNewTooltipHtmlString = getTooltipHtmlString(maybeNewTooltip);
        return maybeNewTooltipHtmlString.equals(existingTooltipHtmlString);
    }

    private static String getTooltipHtmlString(SafeHtml tooltip) {
        return (tooltip != null) ? tooltip.asString() : "";
    }

    private static native void replaceTooltipContent(Element element, String html) /*-{
        var $e = $wnd.jQuery(element);
        $e.attr('data-tooltip-content', html);
        $e.attr('data-original-title', html);

    }-*/;

    private static native void replaceTooltipContent(String elementId, String html) /*-{
        var $e = $wnd.jQuery('#' + elementId);
        $e.attr('data-tooltip-content', html);
        $e.attr('data-original-title', html);
        $e.tooltip('show');

   }-*/;

    private static native boolean isPopupContentElement(Element e) /*-{
        var popupContentSelector = '.' + @org.ovirt.engine.ui.common.view.AbstractPopupView::POPUP_CONTENT_STYLE_NAME;
        return $wnd.jQuery(e).closest(popupContentSelector).length > 0;
    }-*/;

    /**
     * Returns tooltip HTML string or empty string if the element has no tooltip attached.
     */
    private static native String getExistingTooltipHtml(Element e) /*-{
        return $wnd.jQuery(e).attr('data-tooltip-content') || '';
    }-*/;

    /**
     * Returns {@code true} if the given element has tooltip attached.
     */
    private static native boolean hasTooltip(Element e) /*-{
        return $wnd.jQuery(e).attr('rel') === 'tooltip';
    }-*/;

    private static native void createTooltipImpl(Element e, String html, String placement, String template, boolean forceShow, boolean sanitize) /*-{
        var $e = $wnd.jQuery(e);

        // `rel=tooltip` identifies a tooltipped element.
        $e.attr('rel', 'tooltip');

        // `data-tooltip-content` contains tooltip's HTML string.
        $e.attr('data-tooltip-content', html);

        $e.tooltip({
            animation: true,
            container: 'body',
            trigger: 'hover',
            delay: {
                show: 500,
                hide: 0
            },
            title: html,
            html: true,
            placement: placement,
            template: template,
            sanitize: sanitize
        });

        if (forceShow) {
            $e.tooltip('show');
        }
    }-*/;

    /**
     * Destroy tooltip on the given element.
     */
    public static void destroyTooltip(Element e) {
        destroyTooltipImpl(e, true);
        removeElementFromReaperLists(e);
    }

    /**
     * @return {@code true} if the tooltip was successfully destroyed on the given
     * element. Always returns {@code true} if {@code forceDestroy} is {@code true}.
     */
    private static native boolean destroyTooltipImpl(Element e, boolean forceDestroy) /*-{
        // No tooltip means nothing to destroy.
        if (!@org.ovirt.engine.ui.common.utils.ElementTooltipUtils::hasTooltip(Lcom/google/gwt/dom/client/Element;)(e)) {
            return true;
        }

        var $e = $wnd.jQuery(e);

        // Don't destroy if the element is still part of live DOM.
        if (forceDestroy || !$wnd.jQuery.contains($doc, e)) {
            $e.removeAttr('rel');
            $e.removeAttr('data-tooltip-content');
            $e.tooltip('destroy');
            return true;
        }

        return false;
    }-*/;

    /**
     * Hide tooltip on the given element.
     */
    public static native void hideTooltip(Element e) /*-{
        $wnd.jQuery(e).tooltip('hide');
    }-*/;

    /**
     * Hide all tooltips that might be currently visible.
     */
    public static native void hideAllTooltips() /*-{
        $wnd.jQuery('[rel=tooltip]').tooltip('hide');

        // Take the opportunity to reap cell widget tooltips.
        @org.ovirt.engine.ui.common.utils.ElementTooltipUtils::reapCellWidgetTooltips()();
    }-*/;

    /**
     * Reap all tooltips attached to cell widgets.
     * <p>
     * Triggered automatically by {@linkplain CellWidgetTooltipReaper cell widget
     * tooltip reaper} and when calling {@link #hideAllTooltips} method manually.
     */
    public static void reapCellWidgetTooltips() {
        reapTooltips(cellWidgetElementReapList);
    }

    /**
     * Reap all tooltips attached to popup content.
     * <p>
     * Triggered automatically upon closing each popup, given there are no popups
     * currently active.
     */
    public static void reapPopupContentTooltips() {
        reapTooltips(popupContentElementReapList);
    }

    private static void reapTooltips(List<Element> elements) {
        Iterator<Element> it = elements.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            if (destroyTooltipImpl(e, false)) {
                it.remove();
            }
        }
    }

    private static void removeElementFromReaperLists(Element e) {
        List<Element> singletonList = Collections.singletonList(e);
        cellWidgetElementReapList.removeAll(singletonList);
        popupContentElementReapList.removeAll(singletonList);
    }

    // -- Cell widget utilities --

    /**
     * TODO-GWT things would be much easier if GWT supported mouseenter + mouseleave
     *
     * @see com.google.gwt.user.client.impl.DOMImpl#eventGetTypeInt(java.lang.String)
     */
    public static final Set<String> HANDLED_CELL_EVENTS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            BrowserEvents.MOUSEOVER,
            BrowserEvents.MOUSEDOWN
    )));

    private static native Element findParentTableCellElement(Element e) /*-{
        return $wnd.jQuery(e).closest('td, th')[0];
    }-*/;

    private static void hideAllCellWidgetTooltipsExcept(String elementId) {
        for (Element e : cellWidgetElementReapList) {
            if (!elementId.equals(e.getId())) {
                hideTooltip(e);
            }
        }
    }

    public static void handleCellEvent(NativeEvent event, Element e, SafeHtml tooltip) {
        String eventType = event.getType();

        if (BrowserEvents.MOUSEOVER.equals(eventType)) {
            Element parentTableCellElement = findParentTableCellElement(e);

            // Assign unique ID to the parent TD/TH element.
            String parentTableCellElementId = parentTableCellElement.getId();
            if (StringHelper.isNullOrEmpty(parentTableCellElementId)) {
                parentTableCellElementId = DOM.createUniqueId();
                parentTableCellElement.setId(parentTableCellElementId);
            }

            // Make sure the tooltip is set only once on the parent TD/TH element.
            if (!hasTooltip(parentTableCellElement)) {
                setTooltipOnElement(parentTableCellElement, tooltip,
                        new TooltipConfig().setForceShow().markAsCellWidgetTooltip());
            } else if (!sameTooltipOnElement(parentTableCellElement, tooltip)) {
                replaceTooltipContent(parentTableCellElement.getId(), getTooltipHtmlString(tooltip));
            }

            // Prevent other cell widget tooltips from hanging open.
            hideAllCellWidgetTooltipsExcept(parentTableCellElementId);
        } else if (BrowserEvents.MOUSEDOWN.equals(eventType)) {
            hideAllTooltips();
        }
    }

    // -- Menu widget utilities --

    public static void destroyMenuItemTooltips(DropDownMenu dropdownMenu) {
        for (int i = 0; i < dropdownMenu.getWidgetCount(); ++i) {
            destroyTooltip(dropdownMenu.getWidget(i).getElement());
        }
    }

}
