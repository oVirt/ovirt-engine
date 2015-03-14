package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Commented;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

public class CommentColumn<T extends Commented> extends AbstractImageResourceColumn<T> {

    private final static ApplicationResources resources = AssetProvider.getResources();
    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageResource getValue(T value) {
        setTitle(value.getComment());
        if (value.getComment() != null && !value.getComment().isEmpty()) {
            return resources.commentImage();
        }
        return null;
    }

    @Override
    public ImageResource getDefaultImage() {
        return resources.commentImage();
    }

    @Override
    public String getDefaultTitle() {
        return constants.commentLabel();
    }
}
