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
        this(null, compensationContext, null);
    }

    /**
     * Creates instance which holds the execution context
     *
     * @param executionContext
     *            the execution context
     */
    public CommandContext(ExecutionContext executionContext) {
        this(executionContext, null, null);
    }

    /**
     * Creates instance which holds the execution and compensation contexts, and the lock passed by parent command
     *
     * @param executionContext
     *            the execution context
     * @param compensationContext
     *            the compensation context
     * @param lock
     *            the lock passed by the parent command
     */
    public CommandContext(ExecutionContext executionContext, CompensationContext compensationContext, EngineLock lock) {
        setCompensationContext(compensationContext);
        setExecutionContext(executionContext);
        this.lock = lock;
    }

    /**
     * Creates instance which holds the execution and compensation contexts
     * @param executionContext
     *            the execution context
     * @param compensationContext
     *            the compensation context
     */
    public CommandContext(ExecutionContext executionContext, CompensationContext compensationContext) {
        this(executionContext, compensationContext, null);
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
