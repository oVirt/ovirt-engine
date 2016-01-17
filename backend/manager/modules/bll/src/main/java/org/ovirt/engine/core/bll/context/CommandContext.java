package org.ovirt.engine.core.bll.context;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.utils.lock.EngineLock;

/**
 * Holds the context for execution of the command.
 */
public final class CommandContext implements Cloneable {
    /**
     * The compensation context holds the required information for compensating the failed command.
     */
    private CompensationContext compensationContext;

    /**
     * Holds common attributes to all engine flows
     */
    private final EngineContext engineContext;

    /**
     * The Lock in the command context is used by the parent command to pass to the child command locks the child needs
     * to acquire.
     */
    private EngineLock lock;

    /**
     * The execution context describe how the command should be monitored
     */
    private ExecutionContext executionContext;

    public CommandContext(CommandContext ctx) {
        this.compensationContext = ctx.compensationContext;
        this.lock = ctx.lock;
        this.executionContext = ctx.executionContext;
        this.engineContext = ctx.engineContext;
    }

    public CommandContext(EngineContext engineContext) {
        this.engineContext = engineContext;
    }

    public static CommandContext createContext(String sessionId) {
        return new CommandContext(new EngineContext().withSessionId(sessionId)).withoutExecutionContext();
    }

    public EngineContext getEngineContext() {
        return engineContext;
    }

    public CommandContext withCompensationContext(CompensationContext compensationContext) {
        this.compensationContext = compensationContext;
        return this;
    }

    public CommandContext withoutCompensationContext() {
        return withCompensationContext(null);
    }

    public CompensationContext getCompensationContext() {
        return compensationContext;
    }

    public CommandContext withExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
        return this;
    }

    public CommandContext withoutExecutionContext() {
        return withExecutionContext(new ExecutionContext());
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public CommandContext withLock(EngineLock lock) {
        this.lock = lock;
        return this;
    }

    public CommandContext withoutLock() {
        return withLock(null);
    }

    public EngineLock getLock() {
        return lock;
    }

    @Override
    public CommandContext clone() {
        return new CommandContext(this);
    }

}
