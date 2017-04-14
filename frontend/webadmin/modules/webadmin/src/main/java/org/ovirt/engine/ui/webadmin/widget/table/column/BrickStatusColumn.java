package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.BrickStatusCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class BrickStatusColumn extends AbstractColumn<GlusterBrickEntity, GlusterBrickEntity> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    public BrickStatusColumn() {
        super(new BrickStatusCell());
    }

    @Override
    public GlusterBrickEntity getValue(GlusterBrickEntity object) {
        return object;
    }

    public void makeSortable() {
        makeSortable(Comparator.comparingInt(g -> g.getStatus().ordinal()));
    }

    @Override
    public SafeHtml getTooltip(GlusterBrickEntity brick) {
        GlusterStatus status = brick.getStatus();
        String tooltip = null;

        switch (status) {
        case DOWN:
            tooltip = constants.down();
            break;
        case UP:
            tooltip = constants.up();
            break;
        case UNKNOWN:
            tooltip = constants.unknown();
            break;
        default:
            tooltip = constants.down();
        }

        if (brick.getStatus() != GlusterStatus.DOWN && brick.getUnSyncedEntries() != null
                && brick.getUnSyncedEntries() > 0) {
            tooltip = messages.brickStatusWithUnSyncedEntriesPresent(tooltip, brick.getUnSyncedEntries());
        }

        return SafeHtmlUtils.fromSafeConstant(tooltip);

    }
}
