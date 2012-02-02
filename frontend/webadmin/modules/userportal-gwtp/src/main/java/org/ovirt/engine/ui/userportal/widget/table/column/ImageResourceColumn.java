package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

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

    protected ApplicationResources getApplicationResources() {
        return ClientGinjectorProvider.instance().getApplicationResources();
    }

}
