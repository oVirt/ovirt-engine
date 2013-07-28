package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionContext.ExecutionMethod;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddExternalStepParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;

public class AddExternalStepCommand <T extends AddExternalStepParameters> extends CommandBase<T>{

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    private Job job;
    private Step parentStep;

    public AddExternalStepCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue=true;
        job = getJobDao().get(getParameters().getParentId());
        if (job != null) {
            if (!job.isExternal()) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_NOT_EXTERNAL);
            }
        }
        else {
            parentStep = getStepDao().get(getParameters().getParentId());
        }
        if (job == null && parentStep == null) {
            retValue=false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_NO_PARENT);
        }
        if (StringUtils.isBlank(getParameters().getDescription())) {
            retValue=false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_EMPTY_DESCRIPTION);
        }
        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__EXTERNAL_JOB);
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
                Step step = ExecutionHandler.addStep(context, getParameters().getStepType(), getParameters().getDescription(), true);
                setActionReturnValue(step.getId());
                setSucceeded(true);
        }
        else {// this is a sub-step
                context.setStep(parentStep);
                context.setExecutionMethod(ExecutionMethod.AsStep);
                Step step = ExecutionHandler.addSubStep(context, parentStep, getParameters().getStepType(), getParameters().getDescription(), true);
                setActionReturnValue(step.getId());
                setSucceeded(true);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject>  permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.INJECT_EXTERNAL_TASKS));
        return permissionList;
    }

    public JobDao getJobDao() {
        return DbFacade.getInstance().getJobDao();
    }

    public StepDao getStepDao() {
        return DbFacade.getInstance().getStepDao();
    }

}
