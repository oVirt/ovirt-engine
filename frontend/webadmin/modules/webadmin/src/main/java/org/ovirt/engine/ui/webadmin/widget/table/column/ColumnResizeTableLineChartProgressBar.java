package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.widget.table.AbstractActionTable;

public abstract class ColumnResizeTableLineChartProgressBar<T> extends AbstractLineChartProgressBarColumn<T>{

    private AbstractActionTable<T> table;

    protected ColumnResizeTableLineChartProgressBar(AbstractActionTable<T> table, String sortBy) {
        this(table);
        makeSortable(sortBy);
    }

    protected ColumnResizeTableLineChartProgressBar(AbstractActionTable<T> table, Comparator<? super T> comparator) {
        this(table);
        makeSortable(comparator);
    }

    protected ColumnResizeTableLineChartProgressBar(AbstractActionTable<T> table) {
        this.table = table;
    }

    @Override
    protected String getActualWidth() {
        return table.getColumnWidth(this);
    }
}
