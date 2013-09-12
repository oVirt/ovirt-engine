package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;
import java.util.ArrayList;

public class EndedTasksInfo implements Serializable {
    private static final long serialVersionUID = 2511229303527096741L;
    private ArrayList<EndedTaskInfo> privateTasksInfo;

    public ArrayList<EndedTaskInfo> getTasksInfo() {
        return privateTasksInfo;
    }

    public void setTasksInfo(ArrayList<EndedTaskInfo> value) {
        privateTasksInfo = value;
    }
}
