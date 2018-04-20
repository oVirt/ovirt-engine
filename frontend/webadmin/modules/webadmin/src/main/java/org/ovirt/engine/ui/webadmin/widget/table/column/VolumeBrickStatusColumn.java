package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeBrickStatusCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class VolumeBrickStatusColumn extends AbstractColumn<GlusterVolumeEntity, GlusterVolumeEntity> {
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public VolumeBrickStatusColumn() {
        super(new VolumeBrickStatusCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

    @Override
    public SafeHtml getTooltip(GlusterVolumeEntity object) {
        String SPACE = " "; //$NON-NLS-1$
     // Nothing to render if no volume is provided:
        if (object == null) {
            return null;
        }

        int upBricks = 0;
        int downBricks = 0;
        for (GlusterBrickEntity brick : object.getBricks()) {
            if (brick.isOnline()) {
                upBricks++;
            } else {
                downBricks++;
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(upBricks)
                .append(SPACE)
                .append(constants.up())
                .append(SPACE)
                .append(downBricks)
                .append(SPACE)
                .append(constants.down());
        return SafeHtmlUtils.fromString(sb.toString());
    }

}
