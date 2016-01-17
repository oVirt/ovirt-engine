package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.job.JobRepositoryFactory;
import org.ovirt.engine.core.common.action.AddJobParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.JobDao;

public abstract class AddJobCommand<T extends AddJobParameters> extends CommandBase<T> {

    protected AddJobCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }


    @Override
    protected boolean validate() {
        boolean retValue = true;
        if (StringUtils.isBlank(getParameters().getDescription())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_EMPTY_DESCRIPTION);
            retValue = false;
        }
        return retValue;
    }

    public JobDao getJobDao() {
        return DbFacade.getInstance().getJobDao();
    }

    protected void createJob(VdcActionType actionType, boolean isExternal) {
        Job job = ExecutionHandler.createJob(actionType, this);
        job.setDescription(getParameters().getDescription());
        job.setAutoCleared(getParameters().isAutoCleared());
        Guid id = job.getId();
        job.setExternal(isExternal);
        JobRepositoryFactory.getJobRepository().saveJob(job);
        if (getJobDao().get(id) != null) {
            setActionReturnValue(id);
            setSucceeded(true);
        }
        else {
            setSucceeded(false);
        }
    }

}
