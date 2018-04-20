package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeActivityStatusCell<T extends GlusterTaskSupport> extends AbstractCell<T> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no task is provided, or if task status is empty:
        if (value == null || value.getAsyncTask() == null || value.getAsyncTask().getType() == null
                ||value.getAsyncTask().getStatus() == null) {
            return;
        }

        // Find the image corresponding to the task
        GlusterTaskType taskType = value.getAsyncTask().getType();
        ImageResource taskImage = null;

        if (taskType == GlusterTaskType.REBALANCE) {
            switch (value.getAsyncTask().getStatus()) {
            case STARTED:
                taskImage = resources.rebalanceRunning();
                break;
            case ABORTED:
                taskImage = resources.rebalanceStoppped();
                break;
            case FAILED:
                taskImage = resources.rebalanceFailed();
                break;
            case FINISHED:
                taskImage = resources.rebalanceCompleted();
                break;
            case UNKNOWN:
                taskImage = resources.questionMarkImage();
                break;
            default:
                taskImage = null;
            }
        } else if (taskType == GlusterTaskType.REMOVE_BRICK) {
            switch (value.getAsyncTask().getStatus()) {
            case STARTED:
                taskImage = resources.removeBrickRunning();
                break;
            case ABORTED:
                taskImage = resources.removeBrickStopped();
                break;
            case FAILED:
                taskImage = resources.removeBrickFailed();
                break;
            case FINISHED:
                taskImage = resources.removeBrickCommitRequired();
                break;
            case UNKNOWN:
                taskImage = resources.questionMarkImage();
                break;
            default:
                taskImage = null;
            }
        }

        if (taskImage != null) {
            // Generate the HTML for the image:
            SafeHtml activityImageHtml =
                    SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(taskImage).getHTML());
            sb.append(templates.imageWithId(activityImageHtml, id));
        }
    }

}
