package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

/**
 * A wrapper for {@link SimpleStatusImageComparator} that uses an {@link AbstractImageResourceColumn} to extract image values.
 */
public class SimpleStatusColumnComparator<T> implements Comparator<T> {

    private final AbstractImageResourceColumn<T> renderingColumn;
    private final SimpleStatusImageComparator imageComparator;

    public SimpleStatusColumnComparator(AbstractImageResourceColumn<T> renderingColumn) {
        this.renderingColumn = renderingColumn;
        imageComparator = new SimpleStatusImageComparator();
    }

    @Override
    public int compare(T o1, T o2) {
        return imageComparator.compare(renderingColumn.getValue(o1), renderingColumn.getValue(o2));
    }

}
