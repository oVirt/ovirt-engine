package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Commented;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.widget.table.cell.TooltipCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceCell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Column that render a comment image (yellow paper icon) that, when hovered, shows the
 * actual comment in a tooltip.
 *
 * @param <T> row type
 */
public class CommentColumn2<T extends Commented> extends AbstractColumn<T, ImageResource> {

    private static final CommonApplicationResources RESOURCES = GWT.create(CommonApplicationResources.class);

    public CommentColumn2() {
        super(new ImageResourceCell());
    }

    public CommentColumn2(TooltipCell<ImageResource> cell) {
        super(cell);
    }

    /**
     * Using some row value of type T, build an ImageResource to render in this column.
     *
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public ImageResource getValue(T value) {
        if (value.getComment() != null && !value.getComment().isEmpty()) {
            return RESOURCES.commentImage();
        }
        return null;
    }

    /**
     * Using some row value of type T, build a SafeHtml tooltip to render when this column is moused over.
     *
     * @see org.ovirt.engine.ui.common.widget.table.column.AbstractColumn#getTooltip(java.lang.Object)
     */
    @Override
    public SafeHtml getTooltip(T value) {
        return SafeHtmlUtils.fromString(value.getComment());
    }

}
