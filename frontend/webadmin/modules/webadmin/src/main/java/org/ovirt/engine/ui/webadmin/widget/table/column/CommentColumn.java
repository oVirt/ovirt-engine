package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.Commented;

import com.google.gwt.resources.client.ImageResource;

public class CommentColumn<T extends Commented> extends WebAdminImageResourceColumn<T> {

    @Override
    public ImageResource getValue(T value) {
        setTitle(value.getComment());
        if (value.getComment() != null && value.getComment() != "") {
            return getApplicationResources().commentImage();
        }
        return null;
    }
}
