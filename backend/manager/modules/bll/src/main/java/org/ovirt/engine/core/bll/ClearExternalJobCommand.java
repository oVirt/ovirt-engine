package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.JobDao;

public class ClearExternalJobCommand <T extends VdcActionParametersBase> extends CommandBase<T>{

    private Job job;
    public ClearExternalJobCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getParameters().getJobId() != null) {
            job = getJobDao().get((Guid) getParameters().getJobId());
            if (job == null) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_NO_JOB);
            }
            else if (! job.isExternal()) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_NOT_EXTERNAL);
            }
        }
        else {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_NO_JOB);
        }
        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CLEAR);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__EXTERNAL_JOB);
        }
        return retValue;
    }

    @Override
    protected void executeCommand() {
        job = getJobDao().get((Guid) getParameters().getJobId());
        job.setAutoCleared(true);
        getJobDao().update(job);
        setSucceeded(true);
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
