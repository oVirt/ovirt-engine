package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSizeInfo;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.webadmin.widget.table.column.GlusterCapacityCell;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class VolumeCapacityCell<P extends GlusterVolumeEntity> extends GlusterCapacityCell<P> {

    @Override
    public void render(Context context, GlusterVolumeEntity object, SafeHtmlBuilder sb, String id) {
        GlusterVolumeAdvancedDetails advancedDetails = object.getAdvancedDetails();
        GlusterVolumeSizeInfo sizeInfo = null;
        if (advancedDetails != null) {
            sizeInfo = advancedDetails.getCapacityInfo();
            if (sizeInfo != null) {
                setFreeSize(sizeInfo.getFreeSize().doubleValue());
                setTotalSize(sizeInfo.getTotalSize().doubleValue());
                setUsedSize(sizeInfo.getUsedSize().doubleValue());
                setInUnit(SizeUnit.BYTES);
            }
        }
        super.render(context, advancedDetails == null ? null : sizeInfo == null ? null : sizeInfo, sb, id);
    }
}
