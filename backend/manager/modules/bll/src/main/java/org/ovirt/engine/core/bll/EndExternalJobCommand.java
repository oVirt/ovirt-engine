package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionContext.ExecutionMethod;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.EndExternalJobParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.JobDao;

public class EndExternalJobCommand <T extends EndExternalJobParameters> extends CommandBase<T>{

    private static final long serialVersionUID = 1L;
    private Job job;

    public EndExternalJobCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getParameters().getJobId() != null) {
            job = getJobDao().get((Guid) getParameters().getJobId());
            if (job == null) {
                retValue = false;
                addCanDoActionMessage(EngineMessage.ACTION_TYPE_NO_JOB);
            }
            else if (! job.isExternal()) {
                retValue = false;
                addCanDoActionMessage(EngineMessage.ACTION_TYPE_NOT_EXTERNAL);
            }
        }
        else {
            retValue = false;
            addCanDoActionMessage(EngineMessage.ACTION_TYPE_NO_JOB);
        }
        if (!retValue) {
            addCanDoActionMessage(EngineMessage.VAR__ACTION__CLEAR);
            addCanDoActionMessage(EngineMessage.VAR__TYPE__EXTERNAL_JOB);
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        ExecutionContext context = new ExecutionContext();
        context.setMonitored(true);
        context.setExecutionMethod(ExecutionMethod.AsJob);
        context.setJob(job);
        ExecutionHandler.endJob(context, JobExecutionStatus.FINISHED == getParameters().getStatus());
        if (getParameters().isForce()) {
            // mark job as auto-cleared
            job.setAutoCleared(true);
            getJobDao().update(job);
            // Set all job steps and sub steps that were not completed as ABORTED
            JobRepositoryFactory.getJobRepository().closeCompletedJobSteps(job.getId(), JobExecutionStatus.ABORTED);
        }
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
}
