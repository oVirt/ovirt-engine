package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.safehtml.shared.SafeHtml;

public interface TooltipColumn<T> {

    /**
     * <p>
     * Implement this to return tooltip content for T object. You'll likely use some member(s)
     * of T to build a tooltip. You could also use a constant if the tooltip is always the same
     * for this column.
     * </p>
     * <p>
     * The tooltip cell will then use this value when rendering the cell.
     * </p>
     *
     * @return tooltip content
     */
    public SafeHtml getTooltip(T object);
}
