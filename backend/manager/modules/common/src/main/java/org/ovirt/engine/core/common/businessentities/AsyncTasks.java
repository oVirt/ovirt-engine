package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

@Entity
@Table(name = "async_tasks")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class AsyncTasks implements Serializable {
    private static final long serialVersionUID = 5913365704117183118L;



    public AsyncTasks() {
    }

    public AsyncTasks(VdcActionType action_type, AsyncTaskResultEnum result, AsyncTaskStatusEnum status, Guid task_id,
            VdcActionParametersBase action_parameters, NGuid stepId, Guid commandId, Guid storagePoolId, AsyncTaskType taskType) {
        this.actionType = action_type;
        this.result = result;
        this.status = status;
        this.taskId = task_id;
        this.setaction_parameters(action_parameters);
        this.stepId = stepId;
        this.startTime = new Date();
        this.commandId = commandId;
        this.storagePoolId = storagePoolId;
        this.taskType = taskType;
    }

    @Column(name = "action_type", nullable = false)
    @Enumerated
    private VdcActionType actionType = VdcActionType.forValue(0);

    public VdcActionType getaction_type() {
        return this.actionType;
    }

    public void setaction_type(VdcActionType value) {
        this.actionType = value;
    }

    @Column(name = "result", nullable = false)
    @Enumerated
    private AsyncTaskResultEnum result = AsyncTaskResultEnum.forValue(0);


    private Date startTime;


    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public AsyncTaskResultEnum getresult() {
        return this.result;
    }

    public void setresult(AsyncTaskResultEnum value) {
        this.result = value;
    }

    @Column(name = "status", nullable = true)
    @Enumerated
    private AsyncTaskStatusEnum status = AsyncTaskStatusEnum.forValue(0);

    public AsyncTaskStatusEnum getstatus() {
        return this.status;
    }

    public void setstatus(AsyncTaskStatusEnum value) {
        this.status = value;
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "org.ovirt.engine.core.dao.GuidGenerator")
    @Column(name = "task_id")
    @Type(type = "guid")
    private Guid taskId = new Guid();

    public Guid gettask_id() {
        return this.taskId;
    }

    public void settask_id(Guid value) {
        this.taskId = value;
    }

    @Column(name = "action_parameters")
    private VdcActionParametersBase actionParameters;

    public VdcActionParametersBase getaction_parameters() {
        return this.actionParameters;
    }

    public void setaction_parameters(VdcActionParametersBase value) {
        this.actionParameters = value;
    }

    @Column(name = "step_id")
    private NGuid stepId;

    public NGuid getStepId() {
        return this.stepId;
    }

    public void setStepId(NGuid stepId) {
        this.stepId = stepId;
    }

    @Column(name = "command_id")
    private Guid commandId = Guid.Empty;

    public Guid getCommandId() {
        return commandId;
    }

    public void setCommandId(Guid commandId) {
        this.commandId = commandId;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    private Guid storagePoolId;

    public AsyncTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(AsyncTaskType taskType) {
        this.taskType = taskType;
    }

    private AsyncTaskType taskType;

    @Override
    public int hashCode() {
        final int prime = 31;
        int results = 1;
        results = prime * results + ((actionParameters == null) ? 0 : actionParameters.hashCode());
        results = prime * results + ((actionType == null) ? 0 : actionType.hashCode());
        results = prime * results + ((result == null) ? 0 : result.hashCode());
        results = prime * results + ((status == null) ? 0 : status.hashCode());
        results = prime * results + ((taskId == null) ? 0 : taskId.hashCode());
        results = prime * results + ((stepId == null) ? 0 : stepId.hashCode());
        results = prime * results + ((commandId == null) ? 0 : commandId.hashCode());
        results =  prime * results + ((startTime == null) ? 0 : startTime.hashCode());
        results = prime * results + ((storagePoolId == null) ? 0 : storagePoolId.hashCode());
        results = prime * results + ((taskType == null) ? 0 : taskType.hashCode());

        return results;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AsyncTasks other = (AsyncTasks) obj;
        if (actionParameters == null) {
            if (other.actionParameters != null)
                return false;
        } else if (!actionParameters.equals(other.actionParameters))
            return false;
        if (actionType != other.actionType)
            return false;
        if (result != other.result)
            return false;
        if (status != other.status)
            return false;
        if (taskId == null) {
            if (other.taskId != null)
                return false;
        } else if (!taskId.equals(other.taskId))
            return false;
        if (stepId == null) {
            if (other.stepId != null) {
                return false;
            }
        } else if (!stepId.equals(other.stepId)) {
            return false;
        }
        if (commandId == null) {
            if (other.commandId != null) {
                return false;
            }
        } else if (!commandId.equals(other.commandId)) {
            return false;
        }
        if (startTime == null) {
            if (other.startTime != null) {
                return false;
            }
        } else if (!startTime.equals(other.startTime)) {
            return false;
        }
        if (storagePoolId == null) {
            if (other.storagePoolId != null) {
                return false;
            }
        } else if (!storagePoolId.equals(other.storagePoolId)) {
            return false;
        }
        if (taskType == null) {
            if (other.taskType != null) {
                return false;
            }
        } else if (!taskType.equals(other.taskType)) {
            return false;
        }

        return true;
    }
}
