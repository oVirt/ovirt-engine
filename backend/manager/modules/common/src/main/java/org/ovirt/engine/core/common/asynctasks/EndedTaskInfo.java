package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;

public class EndedTaskInfo implements Serializable {
    private static final long serialVersionUID = 5791056980536223685L;
    private AsyncTaskStatus privateTaskStatus;

    public AsyncTaskStatus getTaskStatus() {
        return privateTaskStatus;
    }

    public void setTaskStatus(AsyncTaskStatus value) {
        privateTaskStatus = value;
    }

    private AsyncTaskParameters privateTaskParameters;

    public AsyncTaskParameters getTaskParameters() {
        return privateTaskParameters;
    }

    public void setTaskParameters(AsyncTaskParameters value) {
        privateTaskParameters = value;
    }

    public EndedTaskInfo() {
    }
}
