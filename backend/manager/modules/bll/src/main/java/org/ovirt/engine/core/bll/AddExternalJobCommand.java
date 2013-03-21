package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddExternalJobParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.JobDao;


public class AddExternalJobCommand<T extends AddExternalJobParameters> extends CommandBase<T> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AddExternalJobCommand(T parameters) {
        super(parameters);
    }


    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (StringUtils.isBlank(getParameters().getDescription())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_EMPTY_DESCRIPTION);
            retValue = false;
        }
        return retValue;
    }


    @Override
    protected void executeCommand() {
        Job job = ExecutionHandler.createJob(VdcActionType.AddExternalJob, this);
        job.setDescription(getParameters().getDescription());
        job.setAutoCleared(getParameters().isAutoCleared());
        Guid id = job.getId();
        job.setExternal(true);
        JobRepositoryFactory.getJobRepository().saveJob(job);
        if (getJobDao().get(id) != null) {
            setActionReturnValue(id);
            setSucceeded(true);
        }
        else {
            setSucceeded(false);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_EXTERNAL_JOB : AuditLogType.USER_ADD_EXTERNAL_JOB_FAILED;
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
}
