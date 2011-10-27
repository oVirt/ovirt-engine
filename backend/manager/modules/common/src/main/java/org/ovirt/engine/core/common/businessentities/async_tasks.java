package org.ovirt.engine.core.common.businessentities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "async_tasks")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class async_tasks implements Serializable {
    private static final long serialVersionUID = 5913365704117183118L;

    public async_tasks() {
    }

    public async_tasks(VdcActionType action_type, AsyncTaskResultEnum result, AsyncTaskStatusEnum status, Guid task_id,
            VdcActionParametersBase action_parameters) {
        this.actionType = action_type;
        this.result = result;
        this.status = status;
        this.taskId = task_id;
        this.setaction_parameters(action_parameters);
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

    // used to be toSerializedForm
    public byte[] getSerializedForm() {
        ByteArrayOutputStream baos = null;
        ObjectOutput out = null;
        try {
            baos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(baos);
            out.writeObject(getaction_parameters());
            out.close();
            baos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Unable to serialize task", e);
        } finally {
            try {
                if (baos != null)
                    baos.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                // nothing to do here
            }
        }
    }

    // used to be fromSerializedForm
    public void setSerializedForm(InputStream stream) {
        ObjectInput in = null;
        try {
            if (stream == null || stream.available() == 0)
                return;

            in = new ObjectInputStream(stream);
            setaction_parameters((VdcActionParametersBase) in.readObject());
        } catch (Exception e) {
            throw new RuntimeException("Unable to load task", e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                // nothing to do here
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int results = 1;
        results = prime * results + ((actionParameters == null) ? 0 : actionParameters.hashCode());
        results = prime * results + ((actionType == null) ? 0 : actionType.hashCode());
        results = prime * results + ((result == null) ? 0 : result.hashCode());
        results = prime * results + ((status == null) ? 0 : status.hashCode());
        results = prime * results + ((taskId == null) ? 0 : taskId.hashCode());
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
        async_tasks other = (async_tasks) obj;
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
        return true;
    }
}
