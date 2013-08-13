package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * An {@link AbstractCell} that provides tooltip in case the content doesn't fit the parent element.
 * <p>
 * Make sure to specify {@code mouseover} within cell consumed events, otherwise the tooltip feature will not work.
 *
 * @param <C>
 *            Cell data type.
 */
public abstract class AbstractCellWithTooltip<C> extends AbstractCell<C> {

    public AbstractCellWithTooltip(String... consumedEvents) {
        super(consumedEvents);
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, C value,
            NativeEvent event, ValueUpdater<C> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Skip events other than 'mouseover'
        if (!"mouseover".equals(event.getType())) { //$NON-NLS-1$
            return;
        }

        if (value != null && showTooltip(parent, value)) {
            parent.setTitle(getTooltip(value));
        } else {
            parent.setTitle(""); //$NON-NLS-1$
        }
    }

    /**
     * Returns tooltip to show for the given value.
     */
    protected abstract String getTooltip(C value);

    /**
     * Returns {@code true} if tooltip should be shown for the given {@code parent} element.
     */
    protected boolean showTooltip(Element parent, C value) {
        return contentOverflows(parent);
    }

    /**
     * Returns {@code true} when the content of the given {@code parent} element overflows its area.
     */
    protected boolean contentOverflows(Element parent) {
        return parent != null && (detectOverflowUsingScrollWidth(parent) || detectOverflowUsingClientHeight(parent));
    }

    /**
     * Uses scrollWidth with temporary CSS 'overflow:auto' to detect horizontal overflow.
     */
    boolean detectOverflowUsingScrollWidth(Element parent) {
        int scrollWidthBefore = parent.getScrollWidth();
        String overflowValue = parent.getStyle().getProperty("overflow"); //$NON-NLS-1$
        parent.getStyle().setProperty("overflow", "auto"); //$NON-NLS-1$ //$NON-NLS-2$

        int scrollWidthAfter = parent.getScrollWidth();
        int clientWidthAfter = parent.getClientWidth();
        parent.getStyle().setProperty("overflow", overflowValue); //$NON-NLS-1$

        return scrollWidthAfter > scrollWidthBefore || scrollWidthAfter > clientWidthAfter;
    }

    /**
     * Uses clientHeight with temporary CSS 'whiteSpace:normal' to detect vertical overflow.
     * <p>
     * This is necessary due to some browsers (Firefox) having issues with scrollWidth (e.g. elements with CSS 'display'
     * other than 'block' have incorrect scrollWidth value).
     */
    boolean detectOverflowUsingClientHeight(Element parent) {
        int clientHeightBefore = parent.getClientHeight();
        String whiteSpaceValue = parent.getStyle().getProperty("whiteSpace"); //$NON-NLS-1$
        parent.getStyle().setProperty("whiteSpace", "normal"); //$NON-NLS-1$ //$NON-NLS-2$
        int scrollHeightAfter = parent.getScrollHeight();
        int clientHeightAfter = parent.getClientHeight();
        parent.getStyle().setProperty("whiteSpace", whiteSpaceValue); //$NON-NLS-1$

        return clientHeightAfter > clientHeightBefore || clientHeightAfter < scrollHeightAfter;
    }

}
