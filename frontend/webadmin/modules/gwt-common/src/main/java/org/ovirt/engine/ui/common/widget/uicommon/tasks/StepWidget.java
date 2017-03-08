package org.ovirt.engine.ui.common.widget.uicommon.tasks;

import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.constants.ProgressType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;

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

public class StepWidget extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, StepWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

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

    public StepWidget(Step step) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        if (JobExecutionStatus.STARTED.equals(step.getStatus())) {
            markStarted();
        }
        label.getElement().setInnerSafeHtml(SafeHtmlUtils.fromString(step.getDescription()));

        jobProgressBar.setType(TaskWidget.getProgressBarType(step.getStatus()));
        if (JobExecutionStatus.FINISHED.equals(step.getStatus())) {
            markFinished(step);
        } else {
            markInProgress(step);
        }
    }

    private void markInProgress(Step step) {
        jobProgress.setActive(true);
        jobProgress.setType(ProgressType.STRIPED);
        jobProgressBar.setPercent(100);
        jobProgressBar.setText(constants.startedTask() + ": " // $NON-NLS-1$
                + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(step.getStartTime()));
        if (step.getProgress() != null) {
            jobProgressBar.setPercent(step.getProgress());
        }
    }

    private void markFinished(Step step) {
        jobProgressBar.setPercent(100);
        statusIcon.addStyleName(PatternflyIconType.PF_BASE.getCssName());
        jobProgressBar.setText(constants.completedTask() + ": " // $NON-NLS-1$
                + DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(step.getEndTime()));
        jobProgressBar.setPercent(100);
        if (JobExecutionStatus.FINISHED.equals(step.getStatus())) {
            statusIcon.addStyleName(PatternflyConstants.PFICON_OK);
        } else {
            statusIcon.addStyleName(PatternflyConstants.PFICON_ERROR);
        }
    }

    private void markStarted() {
        statusIcon.addStyleName(PatternflyConstants.PF_SPINNER);
        statusIcon.addStyleName(PatternflyConstants.PF_SPINNER_XS);
        statusIcon.addStyleName(PatternflyConstants.PF_SPINNER_INLINE);
    }
}
