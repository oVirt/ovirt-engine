package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotDetailModel;
import com.google.gwt.resources.client.ImageResource;


public class SnapshotDetailColumn extends AbstractImageResourceColumn<SnapshotDetailModel> {

    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public ImageResource getValue(SnapshotDetailModel snapshotDetailModel) {
        String name = snapshotDetailModel.getName();

        if (name.equals(constants.generalLabel())) {
            return resources.generalImage();
        }
        else if (name.equals(constants.disksLabel())) {
            return resources.diskImage();
        }
        else if (name.equals(constants.nicsLabel())) {
            return resources.nicImage();
        }
        else if (name.equals(constants.applicationsLabel())) {
            return resources.applicationsImage();
        }
        return null;
    }

}
