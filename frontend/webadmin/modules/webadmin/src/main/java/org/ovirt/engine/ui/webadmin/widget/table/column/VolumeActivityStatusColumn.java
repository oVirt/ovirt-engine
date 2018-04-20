package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.VolumeActivityStatusCell;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class VolumeActivityStatusColumn<T extends GlusterTaskSupport> extends AbstractColumn<T, GlusterTaskSupport> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public VolumeActivityStatusColumn() {
        super(new VolumeActivityStatusCell<>());
    }

    @Override
    public GlusterTaskSupport getValue(T object) {
        return object;
    }

    @Override
    public SafeHtml getTooltip(T value) {

        // Nothing to render if no task is provided, or if task status is empty:
        if (value == null || value.getAsyncTask() == null || value.getAsyncTask().getType() == null
                ||value.getAsyncTask().getStatus() == null) {
            return null;
        }


        GlusterTaskType taskType = value.getAsyncTask().getType();
        String tooltip = null;

        if (taskType == GlusterTaskType.REBALANCE) {
            switch (value.getAsyncTask().getStatus()) {
            case STARTED:
                tooltip = constants.rebalanceInProgress();
                break;
            case ABORTED:
                tooltip = constants.rebalanceStopped();
                break;
            case FAILED:
                tooltip = constants.rebalanceFailed();
                break;
            case FINISHED:
                tooltip = constants.rebalanceCompleted();
                break;
            case UNKNOWN:
                tooltip = constants.rebalanceStatusUnknown();
                break;
            default:
                tooltip = ""; //$NON-NLS-1$
            }
        } else if (taskType == GlusterTaskType.REMOVE_BRICK) {
            switch (value.getAsyncTask().getStatus()) {
            case STARTED:
                tooltip = constants.removeBrickInProgress();
                break;
            case ABORTED:
                tooltip = constants.removeBrickStopped();
                break;
            case FAILED:
                tooltip = constants.removeBrickFailed();
                break;
            case FINISHED:
                tooltip = constants.removeBrickCommitRequired();
                break;
            case UNKNOWN:
                tooltip = constants.removeBrickStatusUnknown();
                break;
            default:
                tooltip = ""; //$NON-NLS-1$
            }
        }

        return SafeHtmlUtils.fromSafeConstant(tooltip);
    }

}
