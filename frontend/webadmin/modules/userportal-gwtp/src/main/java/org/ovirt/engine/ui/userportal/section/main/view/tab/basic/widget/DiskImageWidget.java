package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

import org.ovirt.engine.core.common.businessentities.DiskImage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DiskImageWidget extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, DiskImageWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Label diskName;

    @UiField
    Label diskSize;

    public DiskImageWidget(DiskImage diskImage) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        diskSize.setText(diskImage.getSizeInGigabytes() + "GB");
        diskName.setText("Disk " + diskImage.getinternal_drive_mapping() + ':');
    }

}
