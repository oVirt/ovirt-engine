package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionContext.ExecutionMethod;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.EndExternalStepParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.StepDao;

public class EndExternalStepCommand <T extends EndExternalStepParameters> extends CommandBase<T> {
    /**
    *
    */
    private static final long serialVersionUID = 1L;
    private Job job;
    private Step step;

    public EndExternalStepCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean retValue = true;
        step = getStepDao().get(getParameters().getId());
        if (step == null) {
            retValue = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_NO_STEP);
        }
        else if (!step.isExternal()) {
            retValue = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_NOT_EXTERNAL);
        }
        else {
            job = getJobDao().get(step.getJobId());
            if (job == null) {
                retValue = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_NO_JOB);
            }
            if (!retValue) {
                addValidationMessage(EngineMessage.VAR__ACTION__END);
                addValidationMessage(EngineMessage.VAR__TYPE__EXTERNAL_STEP);
            }
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        ExecutionContext context = new ExecutionContext();
        context.setJob(job);
        context.setStep(step);
        context.setMonitored(true);
        context.setExecutionMethod(ExecutionMethod.AsStep);
        ExecutionHandler.endStep(context, step, getParameters().getStatus());
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject>  permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                ActionGroup.INJECT_EXTERNAL_TASKS));
        return permissionList;
    }

    public JobDao getJobDao() {
        return DbFacade.getInstance().getJobDao();
    }

    @Override
    public StepDao getStepDao() {
        return DbFacade.getInstance().getStepDao();
    }
}
