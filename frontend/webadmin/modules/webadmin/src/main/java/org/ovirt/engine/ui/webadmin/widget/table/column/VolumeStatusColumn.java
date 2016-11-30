package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils.VolumeStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeStatusCell;

public class VolumeStatusColumn extends AbstractColumn<GlusterVolumeEntity, GlusterVolumeEntity> {

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    public VolumeStatusColumn() {
        super(new VolumeStatusCell());
    }

    public VolumeStatusColumn(UICommand onClickCommand) {
        super(new VolumeStatusCell(onClickCommand));
    }

    @Override
    public GlusterVolumeEntity getValue(GlusterVolumeEntity object) {
        return object;
    }

    @Override
    public String getTooltip(GlusterVolumeEntity volume) {

        if (volume == null) {
            return null;
        }

        VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);
        String tooltip = null;
        switch (status) {
        case DOWN:
            tooltip = constants.down();
            break;
        case UP:
            tooltip = constants.up();
            break;
        case SOME_BRICKS_DOWN:
            tooltip = constants.volumeBricksDown();
            break;
        case ALL_BRICKS_DOWN:
            tooltip = constants.volumeAllBricksDown();
            break;
        default:
            tooltip = constants.down();
        }

        return tooltip;

    }
}
