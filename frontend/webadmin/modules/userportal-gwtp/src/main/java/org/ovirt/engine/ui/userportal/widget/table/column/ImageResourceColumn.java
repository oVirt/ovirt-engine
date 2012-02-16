package org.ovirt.engine.ui.userportal.widget.table.column;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;

/**
 * Column for displaying {@link ImageResource} instances.
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class ImageResourceColumn<T> extends Column<T, ImageResource> {

    public ImageResourceColumn() {
        super(new ImageResourceCell());
    }
}
