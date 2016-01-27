package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;

class CallbackTiming {
    // Total delay between callback executions
    private int initialDelay;

    // Remaining delay to next callback execution
    private int remainingDelay;
    private CommandCallback callback;

    // the end-time, where the callback shouldn't wait for the event any longer and change to polling mode
    private long waitOnEventEndTime;

    public CallbackTiming(CommandCallback callback, int executionDelay) {
        this.callback = callback;
        this.initialDelay = executionDelay;
        this.remainingDelay = executionDelay;
    }

    public int getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }

    public int getRemainingDelay() {
        return remainingDelay;
    }

    public void setWaitOnEventEndTime(long waitOnEventEndTime) {
        this.waitOnEventEndTime = waitOnEventEndTime;
    }

    public long getWaitOnEventEndTime() {
        return waitOnEventEndTime;
    }

    public void setRemainingDelay(int remainingDelay) {
        this.remainingDelay = remainingDelay;
    }

    public CommandCallback getCallback() {
        return callback;
    }

    public void setCallback(CommandCallback callback) {
        this.callback = callback;
    }
}
