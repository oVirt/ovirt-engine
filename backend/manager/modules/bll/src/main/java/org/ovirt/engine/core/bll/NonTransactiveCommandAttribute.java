package org.ovirt.engine.core.bll;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonTransactiveCommandAttribute {
    /**
     * Flag for determining if a compensation context will be created during command execution, although it will be run
     * without a wrapping transaction
     *
     * @return true if to create compensation context, false if not
     */
    boolean forceCompensation() default false;

    /**
     * Enum for determining if the compensation context will be used/cleanup at the execution part (EXECUTION), or,
     * the compensation will saved until the end of the callback executions (END_COMMAND).
     *
     * @return CommandCompensationPhase.END_COMMAND in order to handle the compensation by the callbacks,
     * CommandCompensationPhase.EXECUTION in order to handle the compensation at the execution phase of the command.
     */
    CommandCompensationPhase compensationPhase() default CommandCompensationPhase.EXECUTION;

    /**
     * Enum for determining when the command should use or clean the compensation
     */
    enum CommandCompensationPhase {
        /**
         * The command should use/clean the compensation at the execution part of the command only, means that if the
         * command will have child commands, the compensation will not be available when they done.
         */
        EXECUTION,
        /**
         * The command should use/clean the compensation when all the child commands done.
         */
        END_COMMAND
    }
}
