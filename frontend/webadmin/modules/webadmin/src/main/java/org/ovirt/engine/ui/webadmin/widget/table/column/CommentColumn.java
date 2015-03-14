package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Commented;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Column that render a comment image (yellow paper icon) that, when hovered, shows the
 * actual comment in a tooltip.
 *
 * @param <T> row type
 */
public class CommentColumn<T extends Commented> extends AbstractImageResourceColumn<T> {

    private final static ApplicationResources resources = AssetProvider.getResources();

    /**
     * Using some row value of type T, build an ImageResource to render in this column.
     *
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public ImageResource getValue(T value) {
        if (value != null && value.getComment() != null && !value.getComment().isEmpty()) {
            return resources.commentImage();
        }
        return null;
    }

    /**
     * Use the comment icon.
     */
    public ImageResource getDefaultImage() {
        return resources.commentImage();
    }

    /**
     * Using some row value of type T, build a SafeHtml tooltip to render when this column is moused over.
     *
     * @see org.ovirt.engine.ui.common.widget.table.column.AbstractColumn#getTooltip(java.lang.Object)
     */
    @Override
    public SafeHtml getTooltip(T value) {
        if (value == null || value.getComment() == null) {
            return null;
        }
        return SafeHtmlUtils.fromString(value.getComment());
    }
}
