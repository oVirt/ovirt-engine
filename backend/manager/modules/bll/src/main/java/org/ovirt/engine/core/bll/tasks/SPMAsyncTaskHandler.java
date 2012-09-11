package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;

/**
 * This interface describes an object that handles async task execution.
 * It contains callbacks that can be fired before/after a task's state changes
 * (e.g., the task is created, finished, receives an error, etc.)
 */
public interface SPMAsyncTaskHandler {
    /** The execution carried out on the engine side before firing an async task. */
    public void execute();

    /** A callback for the task completes successfully */
    public void endSuccessfully();

    /** A callback for the task fails to complete */
    public void endWithFailure();

    /** A callback for undoing the work of a task that has previously succeeded */
    public void compensate();

    /** @return The type of the task this handler fires */
    public AsyncTaskType getTaskType();

    /** @return The type of the task this handler fires when reverting */
    public AsyncTaskType getRevertTaskType();
}
