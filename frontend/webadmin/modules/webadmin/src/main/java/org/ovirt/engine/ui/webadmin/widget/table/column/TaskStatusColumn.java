package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.resources.client.ImageResource;

/**
 * Image column that corresponds to XAML {@code HistorySeverityTemplate}.
 */
public class TaskStatusColumn extends AbstractImageResourceColumn<EntityModel> {

    private final static ApplicationResources resources = AssetProvider.getResources();

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
            return resources.waitImage();
        case FINISHED:
            return resources.logNormalImage();
        case FAILED:
            return resources.logErrorImage();
        case ABORTED:
            return resources.alertImage();
        case UNKNOWN:
            return resources.questionMarkImage();
        default:
            return null;
        }
    }

}
