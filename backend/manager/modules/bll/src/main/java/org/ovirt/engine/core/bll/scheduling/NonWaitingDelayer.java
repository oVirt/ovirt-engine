package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.compat.Guid;

/**
 * This delayer should be used in cases when no throttling is desired e.g in the validation phase of commands.
 */
public class NonWaitingDelayer implements RunVmDelayer {

    @Override
    public void delay(Guid vdsId) {
    }

}
