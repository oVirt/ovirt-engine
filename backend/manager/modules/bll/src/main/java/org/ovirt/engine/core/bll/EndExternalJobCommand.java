package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.EndExternalJobParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.utils.ExecutionMethod;
import org.ovirt.engine.core.dao.JobDao;

public class EndExternalJobCommand <T extends EndExternalJobParameters> extends CommandBase<T>{

    private Job job;

    @Inject
    private JobRepository jobRepository;
    @Inject
    private JobDao jobDao;

    public EndExternalJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        boolean retValue = true;
        if (getParameters().getJobId() != null) {
            job = jobDao.get(getParameters().getJobId());
            if (job == null) {
                retValue = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_NO_JOB);
            } else if (! job.isExternal()) {
                retValue = false;
                addValidationMessage(EngineMessage.ACTION_TYPE_NOT_EXTERNAL);
            }
        } else {
            retValue = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_NO_JOB);
        }
        if (!retValue) {
            addValidationMessage(EngineMessage.VAR__ACTION__CLEAR);
            addValidationMessage(EngineMessage.VAR__TYPE__EXTERNAL_JOB);
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        ExecutionContext context = new ExecutionContext();
        context.setMonitored(true);
        context.setExecutionMethod(ExecutionMethod.AsJob);
        context.setJob(job);
        executionHandler.endJob(context, getParameters().getStatus());
        if (getParameters().isForce()) {
            // mark job as auto-cleared
            job.setAutoCleared(true);
            jobDao.update(job);
            // Set all job steps and sub steps that were not completed as ABORTED
            jobRepository.closeCompletedJobSteps(job.getId(), JobExecutionStatus.ABORTED);
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
}
