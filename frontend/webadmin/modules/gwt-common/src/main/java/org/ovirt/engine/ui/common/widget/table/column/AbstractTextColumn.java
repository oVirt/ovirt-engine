package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.widget.table.cell.TextCell;

/**
 * A Column used to render text. Supports tooltips. Supports wrapping with a css style. Supports truncation.
 *
 * If truncation is enabled, and if the text doesn't fit in the parent element, it is truncated.
 *
 * There are two types of truncation. You can specify a length in characters, or if you don't, overflow
 * will be detected and truncated via CSS 'text-overflow: ellipse'.
 *
 * Truncation can also be disabled.
 *
 * When text is truncated, the full text will be rendered in a tooltip, unless a different tooltip is manually
 * configured. Configure a manual tooltip by overriding getTooltip().
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractTextColumn<T> extends AbstractColumn<T, String> {

    public AbstractTextColumn() {
        super(new TextCell());
    }

    public AbstractTextColumn(boolean useOverflowTruncation) {
        super(new TextCell(TextCell.UNLIMITED_LENGTH, useOverflowTruncation));
    }

    public AbstractTextColumn(int maxTextLength) {
        super(new TextCell(maxTextLength, false));
    }

    public AbstractTextColumn(TextCell cell) {
        super(cell);
    }

    @Override
    public TextCell getCell() {
        return (TextCell) super.getCell();
    }

    /**
     * Enables default <em>client-side</em> sorting for this column, by the String ordering of the displayed text.
     */
    public void makeSortable() {
        makeSortable(Comparator.comparing(this::getValue, new LexoNumericComparator()));
    }

}
