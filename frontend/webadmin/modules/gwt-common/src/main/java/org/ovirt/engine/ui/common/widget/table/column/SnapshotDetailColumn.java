package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotDetailModel;

import com.google.gwt.resources.client.ImageResource;


public class SnapshotDetailColumn extends AbstractImageResourceColumn<SnapshotDetailModel> {

    @Override
    public ImageResource getValue(SnapshotDetailModel snapshotDetailModel) {
        String name = snapshotDetailModel.getName();

        if (name.equals(getCommonConstants().generalLabel())) {
            return getCommonResources().generalImage();
        }
        else if (name.equals(getCommonConstants().disksLabel())) {
            return getCommonResources().diskImage();
        }
        else if (name.equals(getCommonConstants().nicsLabel())) {
            return getCommonResources().nicImage();
        }
        else if (name.equals(getCommonConstants().applicationsLabel())) {
            return getCommonResources().applicationsImage();
        }
        return null;
    }

}
