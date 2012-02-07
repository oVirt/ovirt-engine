package org.ovirt.engine.core.bll.context;

import java.io.Serializable;

import org.ovirt.engine.core.bll.job.ExecutionContext;

/**
 * Holds the context for execution of the command.
 */
public class CommandContext implements Serializable{

    /**
     * Automatic generated serial version ID
     */
    private static final long serialVersionUID = 5108503477534376493L;

    /**
     * The compensation context holds the required information for compensating the failed command.
     */
    private CompensationContext compensationContext;

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
}
