package org.ovirt.engine.core.bll.network.macpool;

import java.util.List;

import org.ovirt.engine.core.utils.lock.AcquireReadLock;
import org.ovirt.engine.core.utils.lock.AcquireWriteLock;


public interface MacPool {

    /**
     * @return free MAC from pool.
     * @throws org.ovirt.engine.core.common.errors.EngineException if mac address cannot be allocated.
     */
    @AcquireWriteLock
    String allocateNewMac();

    /**
     * @return number of available MACs in pool.
     */
    @AcquireReadLock
    int getAvailableMacsCount();

    /**
     * Returns MAC back to pool.
     * @param mac mac to return to pool.
     */
    @AcquireWriteLock
    void freeMac(String mac);

    /**
     * take specified mac from pool. May be unsuccessful depending on system setting.
     * @param mac mac to get from pool.
     * @return true if MAC was added successfully, and false if the MAC is in use and
     * {@link org.ovirt.engine.core.common.businessentities.MacPool#isAllowDuplicateMacAddresses()} is set to false
     */
    @AcquireWriteLock
    boolean addMac(String mac);

    /**
     * Add given MAC address, regardless of it being in use.
     * @param mac MAC to add.
     */
    @AcquireWriteLock
    void forceAddMac(String mac);

    /**
     * @param mac MAC to check.
     * @return true if mac is used.
     */
    @AcquireReadLock
    boolean isMacInUse(String mac);

    /**
     *
     * @param macs macs to return to pool
     */
    @AcquireWriteLock
    void freeMacs(List<String> macs);

    /**
     * @param numberOfAddresses The number of MAC addresses to allocate
     * @return The list of MAC addresses, sorted in ascending order
     * @throws org.ovirt.engine.core.common.errors.EngineException if mac address cannot be allocated.
     */
    @AcquireWriteLock
    List<String> allocateMacAddresses(int numberOfAddresses);

    boolean isDuplicateMacAddressesAllowed();

    boolean isMacInRange(String mac);
}
