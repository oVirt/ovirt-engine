package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code HistorySeverityTemplate}.
 */
public class TaskStatusColumn extends AbstractWebAdminImageResourceColumn<EntityModel> {

    @Override
    public ImageResource getValue(EntityModel jobOrStep) {
        JobExecutionStatus jobExecutionStatus = null;
        if (jobOrStep.getEntity() instanceof Job) {
            jobExecutionStatus = ((Job) jobOrStep.getEntity()).getStatus();
        } else if (jobOrStep.getEntity() instanceof Step) {
            jobExecutionStatus = ((Step) jobOrStep.getEntity()).getStatus();
        } else {
            return null;
        }

        setEnumTitle(jobExecutionStatus);
        switch (jobExecutionStatus) {
        case STARTED:
            return getApplicationResources().waitImage();
        case FINISHED:
            return getApplicationResources().logNormalImage();
        case FAILED:
            return getApplicationResources().logErrorImage();
        case ABORTED:
            return getApplicationResources().alertImage();
        case UNKNOWN:
            return getApplicationResources().questionMarkImage();
        default:
            return null;
        }
    }

}
