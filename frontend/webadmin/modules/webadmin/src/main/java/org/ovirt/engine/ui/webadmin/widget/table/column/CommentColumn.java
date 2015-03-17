package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Commented;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;

/**
 * Column that renders getComment() of a Commented.
 *
 * @param <T> row type, must implement Commented
 */
public class CommentColumn<T extends Commented> extends AbstractTextColumn<T> {

    /**
     * Using some row value of type T extends Commented, simply get the comment.
     *
     * @see com.google.gwt.user.cellview.client.Column#getValue(java.lang.Object)
     */
    @Override
    public String getValue(T value) {
        if (value != null && value.getComment() != null && !value.getComment().isEmpty()) {
            return value.getComment();
        }
        return null;
    }

}
