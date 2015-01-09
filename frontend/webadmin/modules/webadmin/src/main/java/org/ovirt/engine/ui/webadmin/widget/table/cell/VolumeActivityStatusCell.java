package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeActivityStatusCell<T extends GlusterTaskSupport> extends AbstractCell<T> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        // Nothing to render if no task is provided, or if task status is empty:
        if (value == null || value.getAsyncTask() == null || value.getAsyncTask().getType() == null
                ||value.getAsyncTask().getStatus() == null) {
            return;
        }

        // Find the image corresponding to the task
        GlusterTaskType taskType = value.getAsyncTask().getType();
        ImageResource taskImage = null;
        String tooltip = ""; //$NON-NLS-1$

        if (taskType == GlusterTaskType.REBALANCE) {
            switch (value.getAsyncTask().getStatus()) {
            case STARTED:
                taskImage = resources.rebalanceRunning();
                tooltip = constants.rebalanceInProgress();
                break;
            case ABORTED:
                taskImage = resources.rebalanceStoppped();
                tooltip = constants.rebalanceStopped();
                break;
            case FAILED:
                taskImage = resources.rebalanceFailed();
                tooltip = constants.rebalanceFailed();
                break;
            case FINISHED:
                taskImage = resources.rebalanceCompleted();
                tooltip = constants.rebalanceCompleted();
                break;
            case UNKNOWN:
                taskImage = resources.questionMarkImage();
                tooltip = constants.rebalanceStatusUnknown();
                break;
            default:
                taskImage = null;
                tooltip = ""; //$NON-NLS-1$
            }
        }
        else if (taskType == GlusterTaskType.REMOVE_BRICK) {
            switch (value.getAsyncTask().getStatus()) {
            case STARTED:
                taskImage = resources.removeBrickRunning();
                tooltip = constants.removeBrickInProgress();
                break;
            case ABORTED:
                taskImage = resources.removeBrickStopped();
                tooltip = constants.removeBrickStopped();
                break;
            case FAILED:
                taskImage = resources.removeBrickFailed();
                tooltip = constants.removeBrickFailed();
                break;
            case FINISHED:
                taskImage = resources.removeBrickCommitRequired();
                tooltip = constants.removeBrickCommitRequired();
                break;
            case UNKNOWN:
                taskImage = resources.questionMarkImage();
                tooltip = constants.removeBrickStatusUnknown();
                break;
            default:
                taskImage = null;
                tooltip = ""; //$NON-NLS-1$
            }
        }

        if (taskImage != null) {
            // Generate the HTML for the image:
            SafeHtml activityImageHtml =
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(taskImage).getHTML());
            sb.append(applicationTemplates.image(activityImageHtml, tooltip));
        }
    }

}
