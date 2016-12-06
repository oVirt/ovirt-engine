package org.ovirt.engine.core.bll.job;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.ExecutionMethod;

/**
 * Determines the behavior of the Step/Job execution.
 * Class should be used when provided to internal command invocation to determine monitoring of Job/Step.
 * If isMonitored is null, the default behavior will be applied:
 * <ul>
 * <li>Internal commands are not monitored, except Steps which describe Tasks creation.</li>
 * <li>If isMonitored set explicitly - it determines the entirely monitoring level of Job/Step.</li>
 * <li>stepsList will contain a selective list of Steps to be printed. Should be used with isMonitored = null.</li>
 * </ul>
 */
public class ExecutionContext implements Serializable{

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = -2712230330731851853L;

    /**
     * A default list of steps
     */
    static final List<StepEnum> DEFAULT_STEPS_LIST = Arrays.asList(StepEnum.VALIDATING, StepEnum.EXECUTING);

    /**
     * The Job which the internal command participate in. If set, emphasis the control of the Job is passed to the
     * internal command.
     */
    private Job job = null;

    /**
     * A step which represents the internal command
     */
    private Step step = null;

    /**
     * A list of steps to be monitored as part of the internal command execution
     */
    private List<StepEnum> stepsList;

    /**
     * Indicates if the executed command should be monitored
     */
    private boolean isMonitored;

    /**
     * Defines how the executed command should be treated
     */
    private ExecutionMethod executionMethod = ExecutionMethod.AsStep;

    /**
     * Determines if current context should end the Job. Should be used moderately.
     */
    private boolean shouldEndJob;

    /**
     * The step which the VDSM tasks should be added under. The default step will be the {@code StepEnum.EXECUTING}.
     */
    private Step parentTasksStep = null;

    /**
     * VSDM tasks are being monitored even if the internal command isn't being monitored for other steps.
     * By overriding the value of {@code isTasksMonitored} to false, the VDSM tasks will not be monitored as steps.
     */
    private boolean isTasksMonitored = true;

    /**
     * A flag used to identified completed Job or Step. Is turned on by {@code IVdsAsyncCommand.RunningSucceded} or by
     * any asynchronous step of the command.
     */
    private boolean isCompleted;

    /**
     * Specifies if a Job should be created for a context with {@code ExecutionMethod.AsJob}
     */
    private boolean isJobRequired;

    /**
     * Default constructor which defines the default monitored steps of the internal command
     */
    public ExecutionContext() {
        stepsList = DEFAULT_STEPS_LIST;
    }

    public ExecutionContext(ExecutionContext otherContext) {
        job = otherContext.job; // shallow clone, might need to change.
        step = otherContext.step; // shallow clone, might need to change.
        parentTasksStep = otherContext.parentTasksStep; // shallow clone, might need to change.
        isCompleted = otherContext.isCompleted;
        executionMethod = otherContext.executionMethod;
        isMonitored = otherContext.isMonitored;
        shouldEndJob = otherContext.shouldEndJob;
        stepsList = otherContext.stepsList;
        isTasksMonitored = otherContext.isTasksMonitored;
        isJobRequired = otherContext.isJobRequired;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Job getJob() {
        return job;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Step getStep() {
        return step;
    }

    public void setStepsList(List<StepEnum> stepsList) {
        this.stepsList = stepsList;
    }

    public List<StepEnum> getStepsList() {
        return stepsList;
    }

    public void setMonitored(boolean isMonitored) {
        this.isMonitored = isMonitored;
    }

    public boolean isMonitored() {
        return isMonitored;
    }

    public boolean shouldEndJob() {
        return shouldEndJob;
    }

    public void setShouldEndJob(boolean shouldEndJob) {
        this.shouldEndJob = shouldEndJob;
    }

    public void setExecutionMethod(ExecutionMethod executionMethod) {
        this.executionMethod = executionMethod;
    }

    public ExecutionMethod getExecutionMethod() {
        return executionMethod;
    }

    /**
     * Returns the parent step for the steps which represents VDSM tasks.<br>
     * If no step is set, the {@code StepEnum.EXECUTING} will be selected.
     *
     * @return The parent step for the VDSM tasks
     */
    public Step getParentTasksStep() {
        if (parentTasksStep == null) {
            if (job != null) {
                parentTasksStep = job.getStep(StepEnum.EXECUTING);
            } else if (step != null) {
                parentTasksStep = step.getStep(StepEnum.EXECUTING);
            }
        }
        return parentTasksStep;
    }

    public void setParentTasksStep(Step parentTasksStep) {
        this.parentTasksStep = parentTasksStep;
    }

    public boolean isTasksMonitored() {
        return isTasksMonitored;
    }

    public void setTasksMonitored(boolean isTasksMonitored) {
        this.isTasksMonitored = isTasksMonitored;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public void setJobRequired(boolean isJobRequired) {
        this.isJobRequired = isJobRequired;
    }

    public boolean isJobRequired() {
        return isJobRequired;
    }

}
