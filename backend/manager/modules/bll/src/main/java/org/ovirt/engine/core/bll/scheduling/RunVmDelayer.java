package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.compat.Guid;

/**
 * Some commands e.g RunVm may run as a bulk and performs logic to <br>
 * count and reserve memory and CPU to assure there are both enough resources to complete them<br>
 * (i.e run a VM on a selected host) and that we don't exceed those for the subsequent executions.<br>
 * Moreover bulk operations may cause a pick in VDSM resources utilization and the engine can regulate <br>
 * the pace to enable <br>
 * Successful end of the operations.
 */
public interface RunVmDelayer {

    public void delay(Guid vdsId);
}
