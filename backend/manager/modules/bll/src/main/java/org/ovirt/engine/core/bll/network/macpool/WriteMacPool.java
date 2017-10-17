package org.ovirt.engine.core.bll.network.macpool;

import java.util.List;

import org.ovirt.engine.core.utils.lock.AcquireWriteLock;

/**
 * The interface defines all operations that change a MAC-pool state.
 */
public interface WriteMacPool {
    /**
     * @return true if there are enough free MACs in the pool.
     */
    @AcquireWriteLock
    boolean canAllocateMacAddresses(int macs);

    /**
     * @return free MAC from pool.
     * @throws org.ovirt.engine.core.common.errors.EngineException if mac address cannot be allocated.
     */
    @AcquireWriteLock
    String allocateNewMac();

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
     * @param macs macs to be added.
     * @return list of macs, which failed to be added, because of existence of duplicate.
     */
    @AcquireWriteLock
    List<String> addMacs(List<String> macs);

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
}
