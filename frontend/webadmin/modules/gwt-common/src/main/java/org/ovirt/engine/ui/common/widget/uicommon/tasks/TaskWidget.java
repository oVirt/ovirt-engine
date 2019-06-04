package org.ovirt.engine.ui.common.widget.uicommon.tasks;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.constants.ProgressBarType;
import org.gwtbootstrap3.client.ui.constants.ProgressType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class TaskWidget extends Composite {
    interface ViewUiBinder extends UiBinder<Widget, TaskWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    HTMLPanel statusIcon;

    @UiField
    Progress jobProgress;

    @UiField
    ProgressBar jobProgressBar;

    @UiField
    HTMLPanel label;

    @UiField
    FlowPanel container;

    final String correlationId;

    final TaskListModel model;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public TaskWidget(Job job, String correlationId, TaskListModel taskListModel) {
        this.correlationId = correlationId;
        this.model = taskListModel;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        jobProgressBar.setType(getProgressBarType(job.getStatus()));
        if (JobExecutionStatus.FINISHED.equals(job.getStatus()) || JobExecutionStatus.FAILED.equals(job.getStatus())) {
            markJobFinished(job);
        }
        if (JobExecutionStatus.STARTED.equals(job.getStatus())) {
            markJobStarted(job, correlationId);
        }
        label.getElement().setInnerSafeHtml(SafeHtmlUtils.fromString(job.getDescription()));
    }

    private void markJobStarted(Job job, String correlationId) {
        statusIcon.addStyleName(PatternflyConstants.PF_SPINNER);
        statusIcon.addStyleName(PatternflyConstants.PF_SPINNER_XS);
        statusIcon.addStyleName(PatternflyConstants.PF_SPINNER_INLINE);
        jobProgress.setActive(true);
        jobProgress.setType(ProgressType.STRIPED);
        jobProgressBar.setPercent(100);
        String startTime = formatDateToString(job.getStartTime());
        jobProgressBar.setText(constants.startedTask() + ": " + startTime); // $NON-NLS-1$
        if (job.getSteps().isEmpty()) {
            model.updateSingleTask(correlationId);
        } else {
            // Updated values, add the sub tasks
            job.getSteps().forEach(step -> {
                container.add(createStep(step));
            });
        }
    }

    private void markJobFinished(Job job) {
        statusIcon.addStyleName(PatternflyIconType.PF_BASE.getCssName());
        String endTime = formatDateToString(job.getEndTime());
        jobProgressBar.setText(constants.completedTask() + ": " + endTime); // $NON-NLS-1$
        jobProgressBar.setPercent(100);
        if (JobExecutionStatus.FINISHED.equals(job.getStatus())) {
            statusIcon.addStyleName(PatternflyConstants.PFICON_OK);
        } else {
            statusIcon.addStyleName(PatternflyConstants.PFICON_ERROR);
        }
    }

    private String formatDateToString(Date date) {
        return date == null ? constants.notAvailableLabel() :
                DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(date);
    }

    public static ProgressBarType getProgressBarType(JobExecutionStatus status) {
        switch (status) {
        case ABORTED:
            return ProgressBarType.WARNING;
        case FAILED:
            return ProgressBarType.DANGER;
        case FINISHED:
            return ProgressBarType.SUCCESS;
        case STARTED:
            return ProgressBarType.INFO;
        default:
            return ProgressBarType.DEFAULT;
        }
    }

    private Widget createStep(Step step) {
        return new StepWidget(step);
    }
}
