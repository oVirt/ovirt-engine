package org.ovirt.engine.core.bll.job;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.HasCorrelationId;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.utils.ExecutionMethod;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.PreRun;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.log.LoggedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for managing the flow objects the of the command, by the given execution context o the command.
 * <ul>
 * <li>Creates an {@code ExecutionContext} instance for {@code CommandBase}.</li>
 * <li>Creates {@code Job} entities.</li>
 * <li>Add {@code Step} to a {@code Job}.</li>
 * <li>Add {@code Step} to a {@code Step} as a sub step.</li>
 * <li>End step.</li>
 * <li>End job.</li>
 * </ul>
 */
@Singleton
public class ExecutionHandler {

    private final Logger log = LoggerFactory.getLogger(ExecutionHandler.class);

    private static final List<Class<?>> validationGroups = Arrays.asList(new Class<?>[] { PreRun.class });

    private static ExecutionHandler instance;

    @Deprecated
    public static ExecutionHandler getInstance() {
        return instance;
    }

    @Inject
    private JobRepository jobRepository;

    @Inject
    private JobDao jobDao;

    @Inject
    private StepDao stepDao;

    @PostConstruct
    private void init() {
        instance = this;
    }

    /**
     * Creates and returns an instance of {@link Job} entity.
     *
     * @param actionType
     *            The action type the job entity represents.
     * @param command
     *            The {@code CommandBase} instance which the job entity describes.
     * @return An initialized {@code Job} instance.
     */
    public static Job createJob(ActionType actionType, CommandBase<?> command) {
        Job job = new Job();

        job.setId(Guid.newGuid());
        job.setActionType(actionType);
        job.setDescription(ExecutionMessageDirector.resolveJobMessage(actionType, command.getJobMessageProperties()));
        job.setJobSubjectEntities(getSubjectEntities(command.getPermissionCheckSubjects()));
        job.setOwnerId(command.getUserId());
        job.setEngineSessionSeqId(command.getSessionSeqId());
        job.setStatus(JobExecutionStatus.STARTED);
        job.setStartTime(new Date());
        job.setCorrelationId(command.getCorrelationId());

        return job;
    }

    private static Map<Guid, VdcObjectType> getSubjectEntities(List<PermissionSubject> permSubjectList) {
        Map<Guid, VdcObjectType> entities = new HashMap<>();
        for (PermissionSubject permSubj : permSubjectList) {
            if (permSubj.getObjectId() != null && permSubj.getObjectType() != null) {
                entities.put(permSubj.getObjectId(), permSubj.getObjectType());
            }
        }
        return entities;
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
    public void endStep(ExecutionContext context, Step step,
            boolean exitStatus) {
        if (context == null) {
            return;
        }
        if (context.isMonitored()) {
            Job job = context.getJob();
            try {
                if (step != null) {
                    step.markStepEnded(exitStatus);
                    jobRepository.updateStep(step);
                }

                if (context.getExecutionMethod() == ExecutionMethod.AsJob
                        && job != null && !exitStatus) {
                    // step failure will cause the job to be marked as failed
                    context.setCompleted(true);
                    job.markJobEnded(false);
                    jobRepository.updateCompletedJobAndSteps(job);
                } else {
                    Step parentStep = context.getStep();
                    if (context.getExecutionMethod() == ExecutionMethod.AsStep
                            && parentStep != null) {
                        context.setCompleted(true);
                        if (job != null && !exitStatus) {
                            job.markJobEnded(false);
                            jobRepository.updateCompletedJobAndSteps(job);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
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
    public void endTaskStep(Guid stepId, JobExecutionStatus exitStatus) {
        try {
            if (stepId != null) {
                Step step = jobRepository.getStep(stepId, false);

                if (step != null) {
                    step.markStepEnded(exitStatus);
                    jobRepository.updateStep(step);
                }
            }
        } catch (Exception e) {
            log.error("Failed to terminate step '{}' with status '{}': {}",
                    stepId,
                    exitStatus,
                    e.getMessage());
            log.debug("Exception", e);
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
    public void prepareCommandForMonitoring(CommandBase<?> command,
            ActionType actionType,
            boolean runAsInternal) {

        ExecutionContext context = command.getExecutionContext();
        if (context == null) {
            context = new ExecutionContext();
        }

        try {
            boolean isMonitored = shouldMonitorCommand(actionType, runAsInternal);

            // A monitored job is created for monitored external flows
            if (isMonitored || context.isJobRequired()) {
                Job job = getJob(command, actionType);
                context.setExecutionMethod(ExecutionMethod.AsJob);
                context.setJob(job);
                command.setExecutionContext(context);
                command.setJobId(job.getId());
                context.setMonitored(true);
            }
        } catch (Exception e) {
            log.error("Failed to prepare command of type '{}' for monitoring due to error '{}'",
                    actionType.name(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    private Job getJob(CommandBase<?> command, ActionType actionType) {
        ActionParametersBase params = command.getParameters();
        Job job;
        // if Job is external, we had already created the Job by AddExternalJobCommand, so just get it from DB
        if (params.getJobId() != null) {
            job = jobDao.get(params.getJobId());
        } else {
            job = createJob(actionType, command);
            jobRepository.saveJob(job);
        }
        return job;
    }

    /**
     * Determines if a specific action should be monitored by the following criteria:
     * <ul>
     * <li>{@code ActionType.isMonitored} - defined for a specific action type</li>
     * <li>{@code isInternal} - By default, only non-internal commands are monitored</li>
     * </ul>
     *
     * @param actionType
     *            The action type
     * @param isInternal
     *            Indicator of action invocation method
     * @return true if the command should be monitored, else false.
     */
    private static boolean shouldMonitorCommand(ActionType actionType, boolean isInternal) {

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
    public Step addStep(ExecutionContext context, StepEnum stepName, String description) {
        return addStep(context, stepName, description, false);

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
     * @param isExternal
     *        Indicates if the step is invoked by a plug-in
     */
    public Step addStep(ExecutionContext context, StepEnum stepName, String description, boolean isExternal) {
        if (context == null) {
            return null;
        }
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
                        step.setExternal(isExternal);
                        jobRepository.saveStep(step);
                    } catch (Exception e) {
                        log.error("Failed to save new step '{}' for job '{}', '{}': {}",
                                stepName.name(),
                                job.getId(),
                                job.getActionType().name(),
                                e.getMessage());
                        log.debug("Exception", e);
                        job.getSteps().remove(step);
                        step = null;
                    }
                } else {
                    Step contextStep = context.getStep();
                    if (context.getExecutionMethod() == ExecutionMethod.AsStep && contextStep != null) {
                        step = addSubStep(contextStep, stepName, description, Collections.emptyList());
                        step.setExternal(isExternal);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
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
    public Step addTaskStep(ExecutionContext context, StepEnum stepName,
                            String description,
                            Collection<StepSubjectEntity> stepSubjectEntities) {
        if (context == null) {
            return null;
        }
        Step step = null;

        if (context.isTasksMonitored()) {
            Step parentTaskStep = context.getParentTasksStep();
            if (parentTaskStep != null) {
                step = addSubStep(parentTaskStep, stepName, description, stepSubjectEntities);
            }
        }

        return step;
    }

    private Step addSubStep(Step parentStep,
                            StepEnum stepName,
                            String description,
                            Collection<StepSubjectEntity> stepSubjectEntities) {
        Step step = null;

        if (parentStep != null) {
            if (description == null) {
                description = ExecutionMessageDirector.getInstance().getStepMessage(stepName);
            }
            step = parentStep.addStep(stepName, description);

            try {
                jobRepository.saveStep(step, stepSubjectEntities);
            } catch (Exception e) {
                log.error("Failed to save new step '{}' for step '{}', '{}': {}",
                        stepName.name(),
                        parentStep.getId(),
                        parentStep.getStepType().name(),
                        e.getMessage());
                log.debug("Exception", e);
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
    public Step addSubStep(ExecutionContext context, Step parentStep, StepEnum newStepName, String description) {
        return addSubStep(context, parentStep, newStepName, description, false);
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
     * @param isExternal
     *        Indicates if the step is invoked by a plug-in
     */
    public Step addSubStep(ExecutionContext context, Step parentStep, StepEnum newStepName, String description, boolean isExternal) {
        Step step = null;

        if (context == null || parentStep == null) {
            return null;
        }

        try {
            if (context.isMonitored()) {
                if (description == null) {
                    description = ExecutionMessageDirector.getInstance().getStepMessage(newStepName);
                }

                if (context.getExecutionMethod() == ExecutionMethod.AsJob) {
                    if (stepDao.exists(parentStep.getId())) {
                        if (parentStep.getJobId().equals(context.getJob().getId())) {
                            step = parentStep.addStep(newStepName, description);
                        }
                    }
                } else if (context.getExecutionMethod() == ExecutionMethod.AsStep) {
                    step = parentStep.addStep(newStepName, description);
                }
            }
            if (step != null) {
                step.setExternal(isExternal);
                jobRepository.saveStep(step);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return step;
    }

    /**
     * Finalizes a {@code Job} execution by a given context in which the job was performed and by the exit status of
     * the step. If the {@code Job} execution continues beyond the scope of the command, the {@code Job.isAsyncJob()}
     * should be set to {@code true}. If {@code ExecutionMethod.AsStep} is defined, the current active step can end the
     * running {@code Job} by setting the {@link ExecutionContext#shouldEndJob()} to
     * {@code true}.
     *
     * @param context
     *            The context of the execution which defines how the job should be ended
     * @param exitStatus
     *            Indicates if the execution described by the job ended successfully or not.
     */
    public void endJob(ExecutionContext context, boolean exitStatus) {
        if (context == null) {
            return;
        }

        Job job = context.getJob();

        try {
            if (context.isMonitored()) {
                if (context.getExecutionMethod() == ExecutionMethod.AsJob && job != null) {
                    if (context.shouldEndJob() || !(job.isAsyncJob() && exitStatus)) {
                        context.setCompleted(true);
                        endJob(exitStatus, job);
                    }
                } else {
                    Step step = context.getStep();
                    if (context.getExecutionMethod() == ExecutionMethod.AsStep && step != null) {
                        if (context.shouldEndJob()) {
                            if (job == null) {
                                job = jobRepository.getJob(step.getJobId());
                            }

                            if (job != null) {
                                context.setCompleted(true);
                                endJob(exitStatus, job);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void endJob(boolean exitStatus, Job job) {
        job.markJobEnded(exitStatus);
        try {
            jobRepository.updateCompletedJobAndSteps(job);
        } catch (Exception e) {
            log.error("Failed to end Job '{}', '{}': {}",
                    job.getId(),
                    job.getActionType().name(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    /**
     * Creates a context for execution of internal command as a monitored Job
     *
     * @return an execution context as a Job
     */
    public static CommandContext createInternalJobContext() {
        return createInternalJobContext((EngineLock) null);
    }

    public static CommandContext createInternalJobContext(CommandContext commandContext) {
        return createInternalJobContext(commandContext, null);
    }

    /**
     * Creates a context for execution of internal command as a monitored Job,
     * the command will release the given lock when it is finished.
     *
     * @param lock
     *            The lock which should be released at child command (can be null)
     * @return an execution context as a Job
     */
    public static CommandContext createInternalJobContext(EngineLock lock) {
        return modifyContextForInternalJob(new CommandContext(new EngineContext()), lock);
    }

    public static CommandContext createInternalJobContext(CommandContext commandContext, EngineLock lock) {
        return modifyContextForInternalJob(commandContext.clone(), lock);
    }

    private static CommandContext modifyContextForInternalJob(CommandContext returnedContext, EngineLock lock) {
        return returnedContext
                .withExecutionContext(createMonitoredExecutionContext())
                .withLock(lock)
                .withoutCompensationContext();
    }

    private static ExecutionContext createMonitoredExecutionContext() {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.setJobRequired(true);
        executionContext.setMonitored(true);
        return executionContext;
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
    public static CommandContext createDefaultContextForTasks(CommandContext parentContext) {
        return createDefaultContextForTasks(parentContext, null);
    }

    /**
     * Creates a default execution context for an inner command which creates VDSM tasks so the tasks will be monitored
     * under the parent {@code StepEnum.EXECUTING} step. If the parent command is an internal command, its parent task
     * step is passed to its internal command.
     *
     * @param commandContext
     *            The context of the parent command
     * @param lock
     *            The lock which should be released at child command
     * @return A context by which the internal command should be monitored.
     */
    public static CommandContext createDefaultContextForTasks(CommandContext commandContext, EngineLock lock) {
        CommandContext result = commandContext.clone().withLock(lock).withoutCompensationContext();
        return result.withExecutionContext(createDefaultContextForTasksImpl(result.getExecutionContext()));
    }

    public static void setExecutionContextForTasks(CommandContext commandContext, ExecutionContext executionContext, EngineLock lock) {
        commandContext.withExecutionContext(createDefaultContextForTasksImpl(executionContext))
                .withLock(lock);
    }

    private static ExecutionContext createDefaultContextForTasksImpl(ExecutionContext parentExecutionContext) {
        ExecutionContext executionContext = new ExecutionContext();
        if (parentExecutionContext != null) {
            if (parentExecutionContext.getJob() != null) {
                Step parentStep = parentExecutionContext.getParentTasksStep();
                if (parentStep != null) {
                    executionContext.setParentTasksStep(parentStep);
                }
            } else {
                executionContext.setParentTasksStep(parentExecutionContext.getParentTasksStep());
            }
            executionContext.setStep(parentExecutionContext.getStep());
            executionContext.setStepsList(parentExecutionContext.getStepsList());
            executionContext.setJob(parentExecutionContext.getJob());
        }
        return executionContext;
    }

    /**
     * Creates {@code ExecutionContext} which defines the context for executing the finalizing step of the job. If the
     * step exists, it must be part of a job, therefore the {@code Job} entity is being set as part of the context.
     *
     * @param stepId
     *            The unique identifier of the step. Must not be {@code null}.
     * @return The context for monitoring the finalizing step of the job, or {@code null} if no such step.
     */
    public ExecutionContext createFinalizingContext(Guid stepId) {
        ExecutionContext context = null;
        try {
            Step step = jobRepository.getStep(stepId, false);
            if (step != null && step.getParentStepId() != null) {
                context = new ExecutionContext();
                Step executionStep = jobRepository.getStep(step.getParentStepId(), false);

                // indicates if a step is monitored at Job level or as an inner step
                Guid parentStepId = executionStep.getParentStepId();
                if (parentStepId == null) {
                    context.setExecutionMethod(ExecutionMethod.AsJob);
                    context.setJob(jobRepository.getJobWithSteps(step.getJobId()));
                } else {
                    context.setExecutionMethod(ExecutionMethod.AsStep);
                    Step parentStep = jobRepository.getStep(parentStepId, false);
                    parentStep.setSteps(stepDao.getStepsByParentStepId(parentStep.getId()));
                    context.setStep(parentStep);
                }
                context.setMonitored(true);
            }
        } catch (Exception e) {
            log.error("Exception", e);
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
    public Step startFinalizingStep(ExecutionContext executionContext) {
        if (executionContext == null) {
            return null;
        }
        Step step = null;

        try {
            if (executionContext.getExecutionMethod() == ExecutionMethod.AsJob) {
                Job job = executionContext.getJob();
                if (job != null) {
                    Step executingStep = job.getStep(StepEnum.EXECUTING);
                    Step finalizingStep =
                            job.addStep(StepEnum.FINALIZING,
                                    ExecutionMessageDirector.getInstance().getStepMessage(StepEnum.FINALIZING));

                    if (executingStep != null) {
                        executingStep.markStepEnded(true);
                        jobRepository.updateExistingStepAndSaveNewStep(executingStep,
                                finalizingStep);
                    } else {
                        jobRepository.saveStep(finalizingStep);
                    }
                }
            } else if (executionContext.getExecutionMethod() == ExecutionMethod.AsStep) {
                Step parentStep = executionContext.getStep();
                if (parentStep != null) {
                    Step executingStep = parentStep.getStep(StepEnum.EXECUTING);
                    Step finalizingStep =
                            parentStep.addStep(StepEnum.FINALIZING, ExecutionMessageDirector.getInstance()
                                    .getStepMessage(StepEnum.FINALIZING));
                    if (executingStep != null) {
                        executingStep.markStepEnded(true);
                        jobRepository.updateExistingStepAndSaveNewStep(executingStep,
                                finalizingStep);
                    } else {
                        jobRepository.saveStep(finalizingStep);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
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
    public void updateStepExternalId(Step step, Guid externalId, ExternalSystemType systemType) {
        if (step != null) {
            step.getExternalSystem().setId(externalId);
            step.getExternalSystem().setType(systemType);
            try {
                jobRepository.updateStep(step);
            } catch (Exception e) {
                log.error("Failed to save step '{}', '{}' for system-type '{}' with id '{}': {}",
                        step.getId(),
                        step.getStepType().name(),
                        systemType.name(),
                        externalId,
                        e.getMessage());
                log.debug("Exception", e);

            }
        }
    }

    /**
     * Mark the Job as an Async Job which should be terminated by external process to the current command scope.
     *
     * @param executionContext
     *            The context which describe the running job.
     * @param isAsync
     *            indicates if the job should be ended by current action
     */
    public static void setAsyncJob(ExecutionContext executionContext, boolean isAsync) {
        if (executionContext == null) {
            return;
        }
        Job job = executionContext.getJob();
        if (executionContext.getExecutionMethod() == ExecutionMethod.AsJob && job != null) {
            job.setIsAsyncJob(isAsync);
        }
    }

    /**
     * Evaluates if a given correlation-ID as part of the parameters is set correctly. If the correlation-ID is null or
     * empty, a valid correlation-ID will be set. If the correlation-ID exceeds its permitted length, an error return
     * value will be created and returned.
     *
     * @param parameters
     *            The parameters input of the command
     * @return A {@code null} object emphasis correlation-ID is valid or {@code ActionReturnValue} contains the
     *         correlation-ID violation message
     */
    public static ActionReturnValue evaluateCorrelationId(HasCorrelationId parameters) {
        ActionReturnValue returnValue = null;
        String correlationId = parameters.getCorrelationId();
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = CorrelationIdTracker.getCorrelationId();
            if (StringUtils.isEmpty(correlationId)) {
                correlationId = LoggedUtils.getObjectId(parameters);
            }
            parameters.setCorrelationId(correlationId);
        } else {
            List<String> messages = ValidationUtils.validateInputs(validationGroups, parameters);
            if (!messages.isEmpty()) {
                ActionReturnValue returnErrorValue = new ActionReturnValue();
                returnErrorValue.setValid(false);
                returnErrorValue.getValidationMessages().addAll(messages);
                return returnErrorValue;
            }
        }
        return returnValue;
    }

    /**
     * If the provided {@link ExecutionContext} execution method is as Step the current step will
     * be ended with the appropriate exitStatus and so is the FINALIZING {@link Step} if present.
     *
     * @param context
     *            The context of the execution
     * @param exitStatus
     *            Indicates if the execution ended successfully or not.
     */
    public void endFinalizingStepAndCurrentStep(ExecutionContext context, boolean exitStatus) {
        if (context == null) {
            return;
        }

        try {
            Step parentStep = context.getStep();
            if (context.getExecutionMethod() == ExecutionMethod.AsStep && parentStep != null) {
                Step finalizingStep = parentStep.getStep(StepEnum.FINALIZING);
                if (finalizingStep != null) {
                    finalizingStep.markStepEnded(exitStatus);
                    jobRepository.updateStep(finalizingStep);
                }
                parentStep.markStepEnded(exitStatus);
                jobRepository.updateStep(parentStep);
            }
        } catch (RuntimeException e) {
            log.error("Exception", e);
        }
    }

    /**
     * Finalizes Job with VDSM tasks, as this case requires verification that no other steps are running in order to
     * close the entire Job
     *
     * @param context
     *            The context of the execution which defines how the job should be ended
     * @param exitStatus
     *            Indicates if the execution described by the job ended successfully or not.
     */
    public void endTaskJobIfNeeded(ExecutionContext context, boolean exitStatus) {
        if (context == null) {
            return;
        }

        if (context.getExecutionMethod() == ExecutionMethod.AsJob && context.getJob() != null) {
            endJob(context, exitStatus);
        } else {
            Step parentStep = context.getStep();
            if (context.getExecutionMethod() == ExecutionMethod.AsStep && parentStep != null) {
                List<Step> steps = stepDao.getStepsByJobId(parentStep.getJobId());
                boolean hasChildStepsRunning = false;
                for (Step step : steps) {
                    if (step.getStatus() == JobExecutionStatus.STARTED && step.getParentStepId() != null) {
                        hasChildStepsRunning = true;
                        break;
                    }
                }
                if (!hasChildStepsRunning) {
                    endJob(exitStatus, jobRepository.getJob(parentStep.getJobId()));
                }
            }
        }
    }

    /**
     * If the provided {@link ExecutionContext} execution method is as Step the current step will
     * be ended with the appropriate exitStatus and so is the FINALIZING {@link Step} if present.
     * If needed the job will be ended as well.
     *
     * @param context
     *            The context of the execution which defines how the job should be ended
     * @param exitStatus
     *            Indicates if the execution described by the job ended successfully or not.
     */
    public void endTaskStepAndJob(ExecutionContext context, boolean exitStatus) {
        endFinalizingStepAndCurrentStep(context, exitStatus);
        endTaskJobIfNeeded(context, exitStatus);
    }

    /**
     * Checks if a Job has any Step associated with VDSM task
     *
     * @param context
     *            The context of the execution stores the Job
     * @return true if Job has any Step for VDSM Task, else false.
     */
    public boolean checkIfJobHasTasks(ExecutionContext context) {
        if (context == null || !context.isMonitored()) {
            return false;
        }

        try {
            Guid jobId = null;
            if (context.getExecutionMethod() == ExecutionMethod.AsJob && context.getJob() != null) {
                jobId = context.getJob().getId();
            } else if (context.getExecutionMethod() == ExecutionMethod.AsStep && context.getStep() != null) {
                jobId = context.getStep().getId();
            }

            if (jobId != null) {
                return jobDao.checkIfJobHasTasks(jobId);
            }
        } catch (RuntimeException e) {
            log.error("Exception", e);
        }

        return false;
    }

    /**
     * Updates Job for the same entity for a specific action as completed with a given exit status.
     *
     * @param entityId
     *            The entity to search for its jobs
     * @param actionType
     *            The action type to search for
     * @param status
     *            The exist status to be set for the job
     */
    public void updateSpecificActionJobCompleted(Guid entityId, ActionType actionType, boolean status) {
        try {
            List<Job> jobs = jobRepository.getJobsByEntityAndAction(entityId, actionType);
            for (Job job : jobs) {
                if (job.getStatus() == JobExecutionStatus.STARTED) {
                    job.markJobEnded(status);
                }
                jobRepository.updateCompletedJobAndSteps(job);
            }
        } catch (RuntimeException e) {
            log.error("Exception", e);
        }
    }
}
