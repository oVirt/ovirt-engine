package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils.VolumeStatus;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeStatusCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class VolumeStatusColumn extends AbstractColumn<GlusterVolumeEntity, GlusterVolumeEntity> {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    public VolumeStatusColumn() {
        super(new VolumeStatusCell());
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

    @Override
    public SafeHtml getTooltip(GlusterVolumeEntity volume) {

        if (volume == null) {
            return null;
        }

        VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);
        String tooltip = null;
        switch (status) {
        case DOWN:
            tooltip = constants.down();
        case UP:
            tooltip = constants.up();
        case SOME_BRICKS_DOWN:
            tooltip = constants.volumeBricksDown();
        case ALL_BRICKS_DOWN:
            tooltip = constants.volumeAllBricksDown();
        default:
            tooltip = constants.down();
        }

        return SafeHtmlUtils.fromSafeConstant(tooltip);

    }
}
