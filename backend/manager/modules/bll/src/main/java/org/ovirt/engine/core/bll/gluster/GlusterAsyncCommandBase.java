package org.ovirt.engine.core.bll.gluster;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;

public abstract class GlusterAsyncCommandBase<T extends GlusterVolumeParameters> extends GlusterVolumeCommandBase<T> {

    private static final long serialVersionUID = -7411023079841571332L;
    private Step asyncTaskStep;


    public GlusterAsyncCommandBase(T params) {
        super(params);
    }

    protected Map<String, String> getStepMessageMap() {
        Map<String, String> values = new HashMap<String, String>();
        values.put(GlusterConstants.CLUSTER, getVdsGroupName());
        values.put(GlusterConstants.VOLUME, getGlusterVolumeName());
        //TODO: Define constants
        values.put("status", JobExecutionStatus.STARTED.toString() );
        values.put("info", " ");
        return values;
    }

    /**
     *
     * @return the StepEnum associated with this command. This is used to start a sub step for the executing step.
     */
    protected abstract StepEnum getStepType();

    /**
     * Run the corresponding VDS command. The return value of the command should be GlusterAsyncTask.
     * @return GlusterAsyncTask object that contains the started gluster task id which is used to populate
     *          the external id of {@link Step}
     */
    protected abstract GlusterAsyncTask executeAndReturnTask();

    @Override
    protected void executeCommand() {
        asyncTaskStep = ExecutionHandler.addSubStep(this.getExecutionContext(), getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                getStepType(),
                ExecutionMessageDirector.resolveStepMessage(getStepType(),getStepMessageMap()));

        handleTaskReturn(executeAndReturnTask());
    }

    protected void handleTaskReturn(GlusterAsyncTask asyncTask) {
        Guid externalTaskId = asyncTask.getTaskId();
        asyncTaskStep.setStatus(JobExecutionStatus.STARTED);
        ExecutionHandler.updateStepExternalId(asyncTaskStep,
                externalTaskId,
                ExternalSystemType.GLUSTER);
        getExecutionContext().getJob().setStatus(JobExecutionStatus.STARTED);
     }


}
