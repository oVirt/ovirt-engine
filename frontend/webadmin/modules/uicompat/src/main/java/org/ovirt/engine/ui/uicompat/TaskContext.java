package org.ovirt.engine.ui.uicompat;

public class TaskContext {

    private Object privateState;
    public Object getState() {
        return privateState;
    }
    private void setState(Object value) {
        privateState = value;
    }

    public TaskContext(Object state) {
        setState(state);
    }

    public void invokeUIThread(ITaskTarget target, Object state) {
        setState(state);
        target.run(this);
    }
}
