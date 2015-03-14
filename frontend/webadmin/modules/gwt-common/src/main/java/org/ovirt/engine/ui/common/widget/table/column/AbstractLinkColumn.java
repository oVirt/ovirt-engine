package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.LinkCell;

import com.google.gwt.cell.client.FieldUpdater;

/**
 * Column for displaying links using {@link LinkCell}.
 *
 * @param <T>
 *            the row type.
 */
public abstract class AbstractLinkColumn<T> extends AbstractTextColumn<T> {

    public AbstractLinkColumn() {
        this(null);
    }

    public AbstractLinkColumn(FieldUpdater<T, String> fieldUpdater) {
        super(new LinkCell());
        setFieldUpdater(fieldUpdater);
    }

}
