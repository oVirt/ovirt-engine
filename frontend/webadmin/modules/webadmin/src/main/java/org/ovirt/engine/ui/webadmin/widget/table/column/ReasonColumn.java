package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.resources.client.ImageResource;

public abstract class ReasonColumn<T> extends AbstractWebAdminImageResourceColumn<T> {

    @Override
    public ImageResource getValue(T value) {
        setTitle(getReason(value));
        if (getReason(value) != null && !getReason(value).trim().isEmpty()) {
            return getApplicationResources().commentImage();
        }
        return null;
    }

    protected abstract String getReason(T value);

    @Override
    public ImageResource getDefaultImage() {
        return getApplicationResources().commentImage();
    }

    @Override
    public String getDefaultTitle() {
        return CONSTANTS.reasonLabel();
    }
}
