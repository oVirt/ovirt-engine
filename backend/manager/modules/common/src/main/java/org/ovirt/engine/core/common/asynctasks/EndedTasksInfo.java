package org.ovirt.engine.core.common.asynctasks;

public class EndedTasksInfo implements java.io.Serializable {
    private static final long serialVersionUID = 2511229303527096741L;
    private java.util.ArrayList<EndedTaskInfo> privateTasksInfo;

    public java.util.ArrayList<EndedTaskInfo> getTasksInfo() {
        return privateTasksInfo;
    }

    public void setTasksInfo(java.util.ArrayList<EndedTaskInfo> value) {
        privateTasksInfo = value;
    }

    private boolean _commandShouldBeLogged;

    public boolean getCommandShouldBeLogged() {
        return _commandShouldBeLogged;
    }

    public void setCommandShouldBeLogged(boolean value) {
        _commandShouldBeLogged = value;
    }

    public EndedTasksInfo() {
        _commandShouldBeLogged = true;
    }
}
