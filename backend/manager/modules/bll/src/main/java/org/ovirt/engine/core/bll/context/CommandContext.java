package org.ovirt.engine.core.bll.context;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.utils.lock.EngineLock;

/**
 * Holds the context for execution of the command.
 */
public class CommandContext {

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = 5108503477534376493L;

    /**
     * The compensation context holds the required information for compensating the failed command.
     */
    private CompensationContext compensationContext;

    /**
     * The Lock in the command context is used by the parent command to pass to the child command locks the child needs
     * to acquire.
     */
    private EngineLock lock;

    /**
     * The execution context describe how the command should be monitored
     */
    private ExecutionContext executionContext;

    public CommandContext() {
    }

    /**
     * Create instance which holds the compensation context
     *
     * @param compensationContext
     *            the compensation context
     */
    public CommandContext(CompensationContext compensationContext) {
        setCompensationContext(compensationContext);
    }

    /**
     * Creates instance which holds the execution context
     *
     * @param executionContext
     *            the execution context
     */
    public CommandContext(ExecutionContext executionContext) {
        setExecutionContext(executionContext);
    }

    /**
     * Creates instance which holds the execution context
     * @param executionContext
     *            the execution context
     * @param lock
     *            the lock passed from parent, can be null
     */
    public CommandContext(ExecutionContext executionContext, EngineLock lock) {
        this(executionContext);
        this.lock = lock;
    }

    public CommandContext(CompensationContext compensationContext, EngineLock lock) {
        this(compensationContext);
        this.lock = lock;
    }

    public void setCompensationContext(CompensationContext compensationContext) {
        this.compensationContext = compensationContext;
    }

    public CompensationContext getCompensationContext() {
        return compensationContext;
    }

    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public EngineLock getLock() {
        return lock;
    }
}
