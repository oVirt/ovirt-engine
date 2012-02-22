package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.column.BaseImageResourceColumn;

import com.google.gwt.cell.client.ImageResourceCell;

/**
 * Column for displaying {@link ImageResourceCell} instances.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class ImageResourceColumn<T> extends BaseImageResourceColumn<T> {

    public ImageResourceColumn() {
        super(new ImageResourceCell());
    }
}
