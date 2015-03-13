package org.ovirt.engine.ui.common.widget.table.cell;

import org.ovirt.engine.ui.common.utils.ElementUtils;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
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
        if (!BrowserEvents.MOUSEOVER.equals(event.getType())) {
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
        return parent != null && (ElementUtils.detectOverflowUsingScrollWidth(parent) || ElementUtils.detectOverflowUsingClientHeight(parent));
    }
}
