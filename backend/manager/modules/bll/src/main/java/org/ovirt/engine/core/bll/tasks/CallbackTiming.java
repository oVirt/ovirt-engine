package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;

class CallbackTiming {
    // Total delay between callback executions
    private long initialDelay;

    // Remaining delay to next callback execution
    private long remainingDelay;
    private CommandCallback callback;

    // the end-time, where the callback shouldn't wait for the event any longer and change to polling mode
    private long waitOnEventEndTime;

    public CallbackTiming(CommandCallback callback, long executionDelay) {
        this.callback = callback;
        this.initialDelay = executionDelay;
        this.remainingDelay = executionDelay;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public long getRemainingDelay() {
        return remainingDelay;
    }

    public void setWaitOnEventEndTime(long waitOnEventEndTime) {
        this.waitOnEventEndTime = waitOnEventEndTime;
    }

    public long getWaitOnEventEndTime() {
        return waitOnEventEndTime;
    }

    public void setRemainingDelay(long remainingDelay) {
        this.remainingDelay = remainingDelay;
    }

    public CommandCallback getCallback() {
        return callback;
    }

    public void setCallback(CommandCallback callback) {
        this.callback = callback;
    }
}
