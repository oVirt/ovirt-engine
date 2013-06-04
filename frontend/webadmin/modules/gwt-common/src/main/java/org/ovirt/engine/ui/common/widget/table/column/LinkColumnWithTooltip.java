package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.FieldUpdater;

/**
 * Column for displaying links using {@link LinkCellWithTooltip}.
 *
 * @param <T>
 *            the row type.
 */
public abstract class LinkColumnWithTooltip<T> extends TextColumnWithTooltip<T> {

    public LinkColumnWithTooltip() {
        this(null);
    }

    public LinkColumnWithTooltip(FieldUpdater<T, String> fieldUpdater) {
        this(TextCellWithTooltip.UNLIMITED_LENGTH, fieldUpdater);
    }

    public LinkColumnWithTooltip(int maxTextLength, FieldUpdater<T, String> fieldUpdater) {
        super(new LinkCellWithTooltip(maxTextLength));
        setFieldUpdater(fieldUpdater);
    }

}
