package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.GlusterVolumeSnapshotStatusCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class GlusterVolumeSnapshotStatusColumn extends AbstractColumn<GlusterVolumeSnapshotEntity, GlusterVolumeSnapshotEntity> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public GlusterVolumeSnapshotStatusColumn() {
        super(new GlusterVolumeSnapshotStatusCell());
    }

    @Override
    public GlusterVolumeSnapshotEntity getValue(GlusterVolumeSnapshotEntity object) {
        return object;
    }

    public void makeSortable() {
        makeSortable(Comparator.comparingInt(g -> g.getStatus().ordinal()));
    }

    @Override
    public SafeHtml getTooltip(GlusterVolumeSnapshotEntity snapshot) {
        // Nothing to render if no snapshot is provided:
        if (snapshot == null) {
            return null;
        }

        // Find the image corresponding to the status of the brick:
        GlusterSnapshotStatus status = snapshot.getStatus();
        String tooltip;

        switch (status) {
        case ACTIVATED:
            tooltip = constants.up();
            break;
        case DEACTIVATED:
            tooltip = constants.down();
            break;
        case UNKNOWN:
            tooltip = constants.unknown();
            break;
        default:
            tooltip = constants.down();
        }

        return SafeHtmlUtils.fromSafeConstant(tooltip);
    }
}
