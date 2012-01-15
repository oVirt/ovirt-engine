package org.ovirt.engine.core.bll.job;

import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext.ExecutionMethod;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * Provides methods for managing the flow objects the of the command, by the given execution context o the command.
 * <ul>
 * <li>Creates an {@code ExecutionContext} instance for {@code CommandBase}.
 * <li>Creates {@Job} entities.
 * <li>Add {@code Step} to a {@code Job}
 * <li>Add {@code Step} to a {@code Step} as a sub step.
 * <li>End step.
 * <li>End job.
 * <ul>
 */
public class ExecutionHandler {

    private static Log log = LogFactory.getLog(ExecutionHandler.class);

    /**
     * Creates and returns an instance of {@link Job} entity.
     *
     * @param actionType
     *            The action type the job entity represents.
     * @param command
     *            The {@code CommandBase} instance which the job entity describes.
     * @return An initialized {@code Job} instance.
     */
    public static Job createJob(VdcActionType actionType, CommandBase<?> command) {
        Job job = new Job();

        job.setId(Guid.NewGuid());
        job.setActionType(actionType);

        // TODO: create job description by resource bundle
        job.setDescription(actionType.name());

        job.setJobSubjectEntities(command.getPermissionCheckSubjects());
        job.setOwnerId(command.getUserId());
        job.setStatus(JobExecutionStatus.STARTED);
        job.setStartTime(new Date());

        // TODO: set actual value of correlation-ID
        job.setCorrelationId(Guid.NewGuid().toString());

        return job;
    }

    /**
     * Finalizes a {@code Step} execution by a given context in which the step was performed and by the exit status of
     * the step.
     *
     * @param context
     *            The context in which the {@code Step} was executed.
     * @param step
     *            The step to finalize.
     * @param exitStatus
     *            Indicates if the execution described by the step ended successfully or not.
     */
    public static void endStep(ExecutionContext context, Step step, boolean exitStatus) {

        if (context.isMonitored()) {
            Job job = context.getJob();
            try {
                if (context.getExecutionMethod() == ExecutionMethod.AsJob && job != null) {

                    if (exitStatus) {
                        if (step != null) {
                            step.markStepEnded(exitStatus);
                            JobRepositoryFactory.getJobRepository().updateStep(step);
                        }
                    } else {
                        // step failure will cause the job to be marked as failed
                        job.markJobEnded(false);
                        JobRepositoryFactory.getJobRepository().updateCompletedJobAndSteps(job);
                    }
                } else {
                    Step parentStep = context.getStep();
                    if (context.getExecutionMethod() == ExecutionMethod.AsStep && parentStep != null) {
                        if (exitStatus) {
                            if (step != null) {
                                step.markStepEnded(exitStatus);
                                JobRepositoryFactory.getJobRepository().updateStep(step);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * Finalizes a {@code Step} execution which represents a VDSM task. In case of a failure status, the job will not be
     * marked as failed at this stage, but via executing the {@code CommandBase.endAction} with the proper status by
     * {@code the AsyncTaskManager}.
     *
     * @param stepId
     *            A unique identifier of the step to finalize.
     * @param exitStatus
     *            The status which the step should be ended with.
     */
    public static void endTaskStep(NGuid stepId, JobExecutionStatus exitStatus) {
        try {
            if (stepId != null) {
                Step step = JobRepositoryFactory.getJobRepository().getStep(stepId.getValue());

                if (step != null) {
                    step.markStepEnded(exitStatus);
                    JobRepositoryFactory.getJobRepository().updateStep(step);
                }
            }
        } catch (Exception e) {
            log.errorFormat("Failed to terminate step {0} with status {1}", stepId, exitStatus, e);
        }
    }

    /**
     * Prepares the monitoring objects for the command by the default behavior:
     * <ul>
     * <li> {@link ExecutionContext} determines how the command should be monitored. By default, non-internal commands
     * will be associated with {@code Job} to represent the command execution. Internal commands will not be monitored
     * by default, therefore the {@code ExecutionContext} is created as non-monitored context.
     * <li> {@link Job} is created for monitored actions
     * </ul>
     *
     * @param command
     *            The created instance of the command (can't be <code>null</code>).
     * @param actionType
     *            The action type of the command
     * @param runAsInternal
     *            Indicates if the command should be run as internal action or not
     */
    public static void prepareCommandForMonitoring(CommandBase<?> command,
            VdcActionType actionType,
            boolean runAsInternal) {

        ExecutionContext context = command.getExecutionContext();
        if (context == null) {
            context = new ExecutionContext();
        }

        try {
            boolean isMonitored = shouldMonitorCommand(actionType, runAsInternal);
            context.setMonitored(isMonitored);

            // A monitored job is created for monitored external flows
            if (isMonitored) {
                Job job = createJob(actionType, command);
                JobRepositoryFactory.getJobRepository().saveJob(job);
                context.setExecutionMethod(ExecutionMethod.AsJob);
                context.setJob(job);
                command.setExecutionContext(context);
            }
        } catch (Exception e) {
            log.errorFormat("Failed to prepare command of type {0} for monitoring due to error {1}",
                    actionType.name(),
                    ExceptionUtils.getMessage(e),
                    e);
        }
    }

    /**
     * Determines if a specific action should be monitored by the following criteria: <li>
     * {@code VdcActionType.isMonitored} - defined for a specific action type</li> <li>{@code isInternal} - By default,
     * only non-internal commands are monitored</li>
     *
     * @param actionType
     *            The action type
     * @param isInternal
     *            Indicator of action invocation method
     * @return true if the command should be monitored, else false.
     */
    private static boolean shouldMonitorCommand(VdcActionType actionType, boolean isInternal) {

        return actionType.isActionMonitored() && !isInternal;
    }

    /**
     * Adds a {@link Step} entity by the provided context. A {@link Step} will not be created if
     * {@code ExecutionContext.isMonitored()} returns false.
     *
     * @param context
     *            The context of the execution which defines visibility and execution method.
     * @param stepName
     *            The name of the step.
     * @param description
     *            A presentation name for the step. If not provided, the presentation name is resolved by the
     *            {@code stepName}.
     * @return The created instance of the step or {@code null}.
     */
    public static Step addStep(ExecutionContext context, StepEnum stepName, String description) {
        Step step = null;

        if (context.isMonitored()) {
            if (description == null) {
                description = ExecutionMessageDirector.getInstance().getStepMessage(stepName);
            }

            try {
                Job job = context.getJob();
                if (context.getExecutionMethod() == ExecutionMethod.AsJob && job != null) {
                    step = job.addStep(stepName, description);
                    try {
                        JobRepositoryFactory.getJobRepository().saveStep(step);
                    } catch (Exception e) {
                        log.errorFormat("Failed to save new step {0} for job {1}, {2}.", stepName.name(),
                                job.getId(), job.getActionType().name(), e);
                        job.getSteps().remove(step);
                        step = null;
                    }
                } else {
                    Step contextStep = context.getStep();

                    if (context.getExecutionMethod() == ExecutionMethod.AsStep && contextStep != null) {
                        step = addSubStep(contextStep, stepName, description);
                    }
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        return step;
    }

    /**
     * Adds a {@link Step} entity which describes a VDSM task by the provided context. A {@link Step} will not be
     * created if {@code ExecutionContext.isTasksMonitored()} returns false.
     *
     * @param context
     *            The context of the execution which defines visibility of tasks.
     * @param stepName
     *            The name of the step.
     * @param description
     *            A presentation name for the step. If not provided, the presentation name is resolved by the
     *            {@code stepName}.
     * @return The created instance of the step or {@code null}.
     */
    public static Step addTaskStep(ExecutionContext context, StepEnum stepName, String description) {
        Step step = null;

        if (context != null && context.isTasksMonitored()) {
            Step parentTaskStep = context.getParentTasksStep();
            if (parentTaskStep != null) {
                step = addSubStep(parentTaskStep, stepName, description);
            }
        }

        return step;
    }

    private static Step addSubStep(Step parentStep, StepEnum stepName, String description) {
        Step step = null;
        if (parentStep != null) {
            if (description == null) {
                description = ExecutionMessageDirector.getInstance().getStepMessage(stepName);
            }
            step = parentStep.addStep(stepName, description);

            try {
                JobRepositoryFactory.getJobRepository().saveStep(step);
            } catch (Exception e) {
                log.errorFormat("Failed to save new step {0} for step {1}, {2}.", stepName.name(),
                        parentStep.getId(), parentStep.getStepType().name(), e);
                parentStep.getSteps().remove(step);
                step = null;
            }
        }
        return step;
    }

    /**
     * Adds a {@link Step} entity by the provided context as a child step of a given parent step. A {@link Step} will
     * not be created if {@code ExecutionContext.isMonitored()} returns false.
     *
     * @param context
     *            The context of the execution which defines visibility and execution method.
     * @param parentStep
     *            The parent step which the new step will be added as its child.
     * @param newStepName
     *            The name of the step.
     * @param description
     *            A presentation name for the step. If not provided, the presentation name is resolved by the
     *            {@code stepName}.
     * @return The created instance of the step or {@code null}.
     */
    public static Step addSubStep(ExecutionContext context, Step parentStep, StepEnum newStepName, String description) {
        Step step = null;

        if (parentStep == null) {
            return null;
        }

        try {
            if (context.isMonitored()) {
                if (context.getExecutionMethod() == ExecutionMethod.AsJob) {
                    if (DbFacade.getInstance().getStepDao().exists(parentStep.getId())) {
                        if (parentStep.getJobId().equals(context.getJob().getId())) {
                            step = parentStep.addStep(newStepName, description);
                        }
                    }
                } else if (parentStep != null && context.getExecutionMethod() == ExecutionMethod.AsStep) {
                    step = parentStep.addStep(newStepName, description);
                }
            }
            if (step != null) {
                JobRepositoryFactory.getJobRepository().saveStep(step);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return step;
    }

    /**
     * Finalizes a {@code Job} execution by a given context in which the job was performed and by the exit status of
     * the step. If the {@code Job} execution continues beyond the scope of the command, the {@code Job.isAsyncJob()}
     * should be set to {@code true}. If {@code ExecutionMethod.AsStep} is defined, the current active step can end the
     * running {@code Job} by setting the {@ExecutionContext.shouldEndJob()} to
     * {@code true}.
     *
     * @param executionContext
     *            The context of the execution which defines how the job should be ended
     * @param exitStatus
     *            Indicates if the execution described by the job ended successfully or not.
     */
    public static void endJob(ExecutionContext context, boolean exitStatus) {

        Job job = context.getJob();

        try {
            if (context.isMonitored()) {
                if (context.getExecutionMethod() == ExecutionMethod.AsJob && job != null) {
                    if (!(job.isAsyncJob() && exitStatus)) {
                        endJob(exitStatus, job);
                    }
                } else {
                    Step step = context.getStep();
                    if (context.getExecutionMethod() == ExecutionMethod.AsStep && step != null) {
                        if (context.shouldEndJob()) {
                            if (job == null) {
                                job = JobRepositoryFactory.getJobRepository().getJob(step.getJobId());
                            }

                            if (job != null) {
                                endJob(exitStatus, job);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    private static void endJob(boolean exitStatus, Job job) {
        job.markJobEnded(exitStatus);
        try {
            JobRepositoryFactory.getJobRepository().updateCompletedJobAndSteps(job);
        } catch (Exception e) {
            log.errorFormat("Failed to end Job {0}, {1}", job.getId(), job.getActionType().name(), e);
        }
    }

    /**
     * Creates a default execution context for an inner command which creates VDSM tasks so the tasks will be monitored
     * under the parent {@code StepEnum.EXECUTING} step. If the parent command is an internal command, its parent task
     * step is passed to its internal command.
     *
     * @param parentContext
     *            The context of the parent command
     * @return A context by which the internal command should be monitored.
     */
    public static CommandContext createDefaultContexForTasks(ExecutionContext parentContext) {
        ExecutionContext executionContext = new ExecutionContext();

        if (parentContext != null) {
            if (parentContext.getJob() != null) {
                Step parentStep = parentContext.getParentTasksStep();
                if (parentStep != null) {
                    executionContext.setParentTasksStep(parentStep);
                }
            } else {
                executionContext.setParentTasksStep(parentContext.getParentTasksStep());
            }
        }
        return new CommandContext(executionContext);
    }

    /**
     * Creates {@code ExecutionContext} which defines the context for executing the finalizing step of the job. If the
     * step exists, it must be part of a job, therefore the {@code Job} entity is being set as part of the context.
     *
     * @param stepId
     *            The unique identifier of the step or {@code null} if no such step.
     * @return The context for monitoring the finalizing step of the job, or {@code null} if no such step.
     */
    public static ExecutionContext createJobFinlalizingContext(NGuid stepId) {
        ExecutionContext context = null;
        try {
            if (stepId != null) {
                Step step = JobRepositoryFactory.getJobRepository().getStep(stepId.getValue());
                if (step != null) {
                    context = new ExecutionContext();
                    context.setJob(JobRepositoryFactory.getJobRepository().getJobWithSteps(step.getJobId()));
                    context.setExecutionMethod(ExecutionMethod.AsJob);
                    context.setMonitored(true);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return context;
    }

    /**
     * Method should be called when finalizing the command. The execution step is being ended with success and the
     * finalization step is started.
     *
     * @param executionContext
     *            The context of the job
     * @return A created instance of the Finalizing step
     */
    public static Step startFinalizingStep(ExecutionContext executionContext) {
        Step step = null;
        Job job = executionContext.getJob();
        try {
            if (executionContext != null && job != null) {
                Step executingStep = job.getStep(StepEnum.EXECUTING);
                Step finalizingStep = job.addStep(StepEnum.FINALIZING, null);

                if (executingStep != null) {
                    executingStep.markStepEnded(true);
                    JobRepositoryFactory.getJobRepository().updateExistingStepAndSaveNewStep(executingStep,
                            finalizingStep);
                } else {
                    JobRepositoryFactory.getJobRepository().saveStep(finalizingStep);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        return step;
    }

    /**
     * Updates the step with the id in the external system in which the describe task runs.
     *
     * @param step
     *            The step which represents the external task
     * @param externalId
     *            The id of the task in the external system
     * @param systemType
     *            The type of the system
     */
    public static void updateStepExternalId(Step step, Guid externalId, ExternalSystemType systemType) {
        if (step != null) {
            step.getExternalSystem().setId(externalId);
            step.getExternalSystem().setType(systemType);
            try {
                JobRepositoryFactory.getJobRepository().updateStep(step);
            } catch (Exception e) {
                log.errorFormat("Failed to save step {0}, {1} for system-type {2} with id {3}",
                        step.getId(),
                        step.getStepType().name(),
                        systemType.name(),
                        externalId,
                        e);

            }
        }
    }
}
