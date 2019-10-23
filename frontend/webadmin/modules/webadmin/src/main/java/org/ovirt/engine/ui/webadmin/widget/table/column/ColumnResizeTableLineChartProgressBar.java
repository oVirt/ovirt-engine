package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.widget.table.AbstractActionTable;

public abstract class ColumnResizeTableLineChartProgressBar<E, T> extends AbstractLineChartProgressBarColumn<T>{

    private AbstractActionTable<E, T> table;

    protected ColumnResizeTableLineChartProgressBar(AbstractActionTable<E, T> table, String sortBy) {
        this(table);
        makeSortable(sortBy);
    }

    protected ColumnResizeTableLineChartProgressBar(AbstractActionTable<E, T> table, Comparator<? super T> comparator) {
        this(table);
        makeSortable(comparator);
    }

    protected ColumnResizeTableLineChartProgressBar(AbstractActionTable<E, T> table) {
        this.table = table;
    }

    @Override
    protected String getActualWidth() {
        return table.getColumnWidth(this);
    }
}
