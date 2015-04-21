package org.ovirt.engine.core.bll;

/**
 * An interface for commands that are using the
 * {@link SerialChildCommandsExecutionCallback} callback.
 */
public interface SerialChildExecutingCommand {
    /**
     * method to perform the next operation
     *
     * @param completedChildCount
     *            - indicating how many commands were already executed.
     * @return true if there are more operations to be performed, otherwise false.
     */
    public boolean performNextOperation(int completedChildCount);

    /**
     * method to handle immediately (before endWithFailure() is called) a failure.
     */
    public void handleFailure();
}
