package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionContext.ExecutionMethod;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.common.action.AddStepParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;

public abstract class AddStepCommand<T extends AddStepParameters> extends CommandBase<T> {

    protected Job job;
    protected Step parentStep;

    @Inject
    private JobRepository jobRepository;

    @Inject
    private JobDao jobDao;

    @Inject
    private StepDao stepDao;

    protected AddStepCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean retValue=true;
        job = getJobDao().get(getParameters().getParentId());
        if (job == null) {
            parentStep = getStepDao().get(getParameters().getParentId());
        }
        if (job == null && parentStep == null) {
            retValue=false;
            addValidationMessage(EngineMessage.ACTION_TYPE_NO_PARENT);
        }
        if (StringUtils.isBlank(getParameters().getDescription())) {
            retValue=false;
            addValidationMessage(EngineMessage.ACTION_TYPE_EMPTY_DESCRIPTION);
        }

        return retValue;
    }

    @Override
    protected void executeCommand() {
        ExecutionContext context = new ExecutionContext();
        context.setMonitored(true);
        if (parentStep == null) { // A step that is directly under a job
                context.setJob(job);
                context.setExecutionMethod(ExecutionMethod.AsJob);
                jobRepository.loadJobSteps(job);
                Step step = ExecutionHandler.addStep(context, getParameters().getStepType(), getParameters().getDescription(), true);
                setActionReturnValue(step.getId());
                setSucceeded(true);
        }
        else {// this is a sub-step
                context.setStep(parentStep);
                context.setExecutionMethod(ExecutionMethod.AsStep);
                jobRepository.loadParentStepSteps(parentStep);
                Step step = ExecutionHandler.addSubStep(context, parentStep, getParameters().getStepType(), getParameters().getDescription(), true);
                setActionReturnValue(step.getId());
                setSucceeded(true);
        }
    }

    public JobDao getJobDao() {
        return jobDao;
    }

    @Override
    public StepDao getStepDao() {
        return stepDao;
    }

}
