package org.ovirt.engine.ui.common.widget.uicommon.tasks;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Progress;
import org.gwtbootstrap3.client.ui.ProgressBar;
import org.gwtbootstrap3.client.ui.constants.ColumnOffset;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
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

    private static final String OFFSET_PREFIX = "SM_"; //$NON-NLS-1$

    interface WidgetUiBinder extends UiBinder<Widget, StepWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    HTMLPanel statusIcon;

    @UiField
    Progress jobProgress;

    @UiField
    Column column;

    @UiField
    ProgressBar jobProgressBar;

    @UiField
    HTMLPanel label;

    @UiField
    FlowPanel container;

    public StepWidget(Step step) {
        this(step, 0);
    }

    public StepWidget(Step step, int indent) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        column.setSize(determineSize(indent));
        column.setOffset(determineOffset(indent));
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
        if (!step.getSteps().isEmpty()) {
            // Updated values, add the sub tasks
            step.getSteps().forEach(subStep ->
                container.add(createStep(subStep, indent + 1))
            );
        }
    }

    private ColumnOffset determineOffset(int indent) {
        int offset = 1 + indent;
        return ColumnOffset.valueOf(OFFSET_PREFIX + offset);
    }

    private ColumnSize determineSize(int indent) {
        int size = 11 - indent;
        return ColumnSize.valueOf(OFFSET_PREFIX + size);
    }

    private void markInProgress(Step step) {
        jobProgress.setActive(true);
        jobProgress.setType(ProgressType.STRIPED);
        jobProgressBar.setPercent(100);
        String startTime = step.getStartTime() == null ? constants.notAvailableLabel() :
            DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(step.getStartTime());
        jobProgressBar.setText(constants.startedTask() + ": " + startTime); // $NON-NLS-1$
        if (step.getProgress() != null) {
            jobProgressBar.setPercent(step.getProgress());
        }
    }

    private void markFinished(Step step) {
        jobProgressBar.setPercent(100);
        statusIcon.addStyleName(PatternflyIconType.PF_BASE.getCssName());
        String endTime = step.getEndTime() == null ? constants.notAvailableLabel() :
            DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(step.getEndTime());
        jobProgressBar.setText(constants.completedTask() + ": " + endTime); // $NON-NLS-1$
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

    private Widget createStep(Step step, int indent) {
        return new StepWidget(step, indent);
    }
}
