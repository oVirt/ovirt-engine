package org.ovirt.engine.ui.webadmin.widget.table.cell;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
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

public class VolumeTaskWaitingCell<T extends GlusterTaskSupport> extends AbstractCell<T> {

    ApplicationResources resources = ClientGinjectorProvider.getApplicationResources();

    ApplicationConstants constants = ClientGinjectorProvider.getApplicationConstants();

    ApplicationTemplates applicationTemplates = ClientGinjectorProvider.getApplicationTemplates();

    @Override
    public void render(Context context, T value, SafeHtmlBuilder sb) {
        // Waiting icon need to be rendered only if job is started and task ref is empty
        if (value.getAsyncTask() == null || value.getAsyncTask().getJobId() == null
                || value.getAsyncTask().getType() != null
                || value.getAsyncTask().getJobStatus() != JobExecutionStatus.STARTED) {
            return;
        }

        ImageResource taskImage = resources.waitImage();
        String tooltip = constants.waitForGlusterTask();

        // Generate the HTML for the image:
        SafeHtml activityImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(taskImage).getHTML());
        sb.append(applicationTemplates.image(activityImageHtml, tooltip));
    }

}
