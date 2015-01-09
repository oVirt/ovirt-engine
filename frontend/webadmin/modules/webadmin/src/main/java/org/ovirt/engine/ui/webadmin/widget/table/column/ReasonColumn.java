package org.ovirt.engine.ui.webadmin.widget.table.column;

import com.google.gwt.resources.client.ImageResource;
import org.ovirt.engine.core.common.businessentities.Reasoned;

public class ReasonColumn<T extends Reasoned> extends AbstractWebAdminImageResourceColumn<T> {

    @Override
    public ImageResource getValue(T value) {
        setTitle(value.getStopReason());
        if (value.getStopReason() != null && !value.getStopReason().trim().isEmpty()) {
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
        return CONSTANTS.reasonLabel();
    }
}
