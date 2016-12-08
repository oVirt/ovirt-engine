package org.ovirt.engine.core.bll.network.macpool;

import org.ovirt.engine.core.utils.lock.AcquireReadLock;

/**
 * The interface defines all operations that retrieve info from a MAC-pool but do not change the MAC-pool state.
 */
public interface ReadMacPool {
    /**
     * @return number of available MACs in pool.
     */
    @AcquireReadLock
    int getAvailableMacsCount();

    /**
     * @param mac MAC to check.
     * @return true if mac is used.
     */
    @AcquireReadLock
    boolean isMacInUse(String mac);

    boolean isDuplicateMacAddressesAllowed();

    boolean isMacInRange(String mac);
}
