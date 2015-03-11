package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.EditTextCellWithTooltip;
import org.ovirt.engine.ui.common.widget.table.cell.TextCell;

import com.google.gwt.cell.client.FieldUpdater;

/**
 * Column for displaying editable text using {@link EditTextCellWithTooltip}.
 *
 * @param <T>
 *            the row type.
 */
public abstract class AbstractEditTextColumn<T> extends AbstractTextColumn<T> {

    public AbstractEditTextColumn() {
        this(null);
    }

    public AbstractEditTextColumn(FieldUpdater<T, String> fieldUpdater) {
        this(TextCell.UNLIMITED_LENGTH, fieldUpdater);
    }

    public AbstractEditTextColumn(int maxTextLength, FieldUpdater<T, String> fieldUpdater) {
        super(new EditTextCellWithTooltip(maxTextLength));
        setFieldUpdater(fieldUpdater);
    }

}
