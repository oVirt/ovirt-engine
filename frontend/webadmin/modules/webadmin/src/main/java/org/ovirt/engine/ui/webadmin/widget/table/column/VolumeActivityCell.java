package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
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

public class VolumeActivityCell extends AbstractCell<GlusterVolumeEntity> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    @Override
    public void render(Context context, GlusterVolumeEntity volume, SafeHtmlBuilder sb) {
        // Nothing to render if no volume is provided, or if volume has no task:
        if (volume == null || volume.getAsyncTask() == null) {
            return;
        }

        // Find the image corresponding to the task on the volume:
        GlusterTaskType taskType = volume.getAsyncTask().getType();
        ImageResource taskImage = null;
        String tooltip = ""; //$NON-NLS-1$

        if (taskType == GlusterTaskType.REBALANCE) {
            switch (volume.getAsyncTask().getStatus()) {
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
            default:
                taskImage = null;
                tooltip = ""; //$NON-NLS-1$
            }
        }

        if (taskImage != null) {
            // Generate the HTML for the image:
            SafeHtml activityImageHtml =
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(taskImage).getHTML());
            sb.append(applicationTemplates.statusTemplate(activityImageHtml, tooltip));
        }
    }

}
