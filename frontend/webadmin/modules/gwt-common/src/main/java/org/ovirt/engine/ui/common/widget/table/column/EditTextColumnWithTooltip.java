package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.EditTextCellWithTooltip;
import org.ovirt.engine.ui.common.widget.table.cell.TextCellWithTooltip;

import com.google.gwt.cell.client.FieldUpdater;

/**
 * Column for displaying editable text using {@link EditTextCellWithTooltip}.
 *
 * @param <T>
 *            the row type.
 */
public abstract class EditTextColumnWithTooltip<T> extends TextColumnWithTooltip<T> {

    public EditTextColumnWithTooltip() {
        this(null);
    }

    public EditTextColumnWithTooltip(FieldUpdater<T, String> fieldUpdater) {
        this(TextCellWithTooltip.UNLIMITED_LENGTH, fieldUpdater);
    }

    public EditTextColumnWithTooltip(int maxTextLength, FieldUpdater<T, String> fieldUpdater) {
        super(new EditTextCellWithTooltip(maxTextLength));
        setFieldUpdater(fieldUpdater);
    }

}
