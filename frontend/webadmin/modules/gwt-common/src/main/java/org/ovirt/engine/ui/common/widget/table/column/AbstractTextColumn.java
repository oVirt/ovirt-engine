package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.ui.common.widget.table.cell.TextCell;


/**
 * Column for displaying text using {@link TextCell}.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractTextColumn<T> extends AbstractSortableColumn<T, String> implements ColumnWithElementId {

    public AbstractTextColumn() {
        this(TextCell.UNLIMITED_LENGTH);
    }

    public AbstractTextColumn(int maxTextLength) {
        this(new TextCell(maxTextLength));
    }

    public AbstractTextColumn(TextCell cell) {
        super(cell);
    }

    @Override
    public void configureElementId(String elementIdPrefix, String columnId) {
        getCell().setElementIdPrefix(elementIdPrefix);
        getCell().setColumnId(columnId);
    }

    @Override
    public TextCell getCell() {
        return (TextCell) super.getCell();
    }

    public void setTitle(String tooltipText) {
        getCell().setTitle(tooltipText);
    }

    /**
     * Enables default <em>client-side</em> sorting for this column, by the String ordering of the displayed text.
     */
    public void makeSortable() {
        makeSortable(new Comparator<T>() {

            private LexoNumericComparator lexoNumeric = new LexoNumericComparator();

            @Override
            public int compare(T arg0, T arg1) {
                return lexoNumeric.compare(getValue(arg0), getValue(arg1));
            }
        });
    }
}
