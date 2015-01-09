package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Commented;

import com.google.gwt.resources.client.ImageResource;

public class CommentColumn<T extends Commented> extends AbstractWebAdminImageResourceColumn<T> {

    @Override
    public ImageResource getValue(T value) {
        setTitle(value.getComment());
        if (value.getComment() != null && !value.getComment().isEmpty()) {
            return getApplicationResources().commentImage();
        }
        return null;
    }

    @Override
    public ImageResource getDefaultImage() {
        return getApplicationResources().commentImage();
    }

    @Override
    public String getDefaultTitle() {
        return CONSTANTS.commentLabel();
    }
}
