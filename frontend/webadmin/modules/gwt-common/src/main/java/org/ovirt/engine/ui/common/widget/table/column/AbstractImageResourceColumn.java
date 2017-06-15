package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.widget.table.cell.ImageResourceCell;

import com.google.gwt.resources.client.ImageResource;

/**
 * Column for rendering {@link ImageResource} instances using {@link ImageResourceCell}. Supports tooltips.
 *
 * If you need an ImageResource header for your column, use ImageResourceHeader (which also supports tooltips).
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractImageResourceColumn<T> extends AbstractColumn<T, ImageResource> {

    public AbstractImageResourceColumn() {
        super(new ImageResourceCell());
    }

    public AbstractImageResourceColumn(ImageResourceCell cell) {
        super(cell);
    }

    @Override
    public ImageResourceCell getCell() {
        return (ImageResourceCell) super.getCell();
    }

    public ImageResource getDefaultImage() {
        return null;
    }

}
