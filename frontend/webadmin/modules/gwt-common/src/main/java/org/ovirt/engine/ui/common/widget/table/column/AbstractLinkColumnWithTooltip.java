package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.LinkCellWithTooltip;
import org.ovirt.engine.ui.common.widget.table.cell.TextCellWithTooltip;

import com.google.gwt.cell.client.FieldUpdater;

/**
 * Column for displaying links using {@link LinkCellWithTooltip}.
 *
 * @param <T>
 *            the row type.
 */
public abstract class AbstractLinkColumnWithTooltip<T> extends AbstractTextColumnWithTooltip<T> {

    public AbstractLinkColumnWithTooltip() {
        this(null);
    }

    public AbstractLinkColumnWithTooltip(FieldUpdater<T, String> fieldUpdater) {
        this(TextCellWithTooltip.UNLIMITED_LENGTH, fieldUpdater);
    }

    public AbstractLinkColumnWithTooltip(int maxTextLength, FieldUpdater<T, String> fieldUpdater) {
        super(new LinkCellWithTooltip(maxTextLength));
        setFieldUpdater(fieldUpdater);
    }

}
