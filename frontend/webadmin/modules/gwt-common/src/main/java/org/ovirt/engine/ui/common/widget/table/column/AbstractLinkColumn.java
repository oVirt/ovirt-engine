package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.LinkCell;
import org.ovirt.engine.ui.common.widget.table.cell.TextCell;

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
        this(TextCell.UNLIMITED_LENGTH, fieldUpdater);
    }

    public AbstractLinkColumn(int maxTextLength, FieldUpdater<T, String> fieldUpdater) {
        super(new LinkCell(maxTextLength));
        setFieldUpdater(fieldUpdater);
    }

}
