package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DiskImageWidget extends Composite implements HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, DiskImageWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Label diskName;

    @UiField
    Label diskSize;

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    public DiskImageWidget(DiskImage diskImage) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        diskSize.setText(messages.gigabytes(String.valueOf(diskImage.getSizeInGigabytes())));
        diskName.setText(diskImage.getDiskAlias() + ':'); //$NON-NLS-1$
    }

    @Override
    public void setElementId(String elementId) {
        // Set disk name element ID
        diskName.getElement().setId(
                ElementIdUtils.createElementId(elementId, "diskName")); //$NON-NLS-1$

        // Set disk size element ID
        diskSize.getElement().setId(
                ElementIdUtils.createElementId(elementId, "diskSize")); //$NON-NLS-1$
    }

}
