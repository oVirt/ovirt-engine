package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.EditTextCell;

import com.google.gwt.cell.client.FieldUpdater;

/**
 * Column for displaying editable text using {@link EditTextCell}.
 *
 * @param <T>
 *            the row type.
 */
public abstract class AbstractEditTextColumn<T> extends AbstractTextColumn<T> {

    public AbstractEditTextColumn() {
        this(null);
    }

    public AbstractEditTextColumn(FieldUpdater<T, String> fieldUpdater) {
        super(new EditTextCell());
        setFieldUpdater(fieldUpdater);
    }

}
