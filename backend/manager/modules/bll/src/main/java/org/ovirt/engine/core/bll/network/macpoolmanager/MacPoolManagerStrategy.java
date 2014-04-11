package org.ovirt.engine.core.bll.network.macpoolmanager;

import java.util.List;

public interface MacPoolManagerStrategy {

    /**
     * Initialize pool.
     */
    void initialize();

    /**
     * @return free MAC from pool.
     * @throws org.ovirt.engine.core.common.errors.VdcBLLException if mac address cannot be allocated.
     */
    String allocateNewMac();

    /**
     * @return number of available MACs in pool.
     */
    int getAvailableMacsCount();

    /**
     * Returns MAC back to pool.
     * @param mac mac to return to pool.
     */
    void freeMac(String mac);

    /**
     * take specified mac from pool. May be unsuccessful depending on system setting.
     * @param mac mac to get from pool.
     * @return true if MAC was added successfully, and false if the MAC is in use and
     * {@link org.ovirt.engine.core.common.config.ConfigValues#AllowDuplicateMacAddresses} is set to false
     */
    boolean addMac(String mac);

    /**
     * Add given MAC address, regardless of it being in use.
     * @param mac MAC to add.
     */
    void forceAddMac(String mac);

    /**
     * @param mac MAC to check.
     * @return true if mac is used.
     */
    boolean isMacInUse(String mac);

    /**
     *
     * @param macs macs to return to pool
     */
    void freeMacs(List<String> macs);

    /**
     * @param numberOfAddresses The number of MAC addresses to allocate
     * @return The list of MAC addresses, sorted in ascending order
     * @throws org.ovirt.engine.core.common.errors.VdcBLLException if mac address cannot be allocated.
     */
    List<String> allocateMacAddresses(int numberOfAddresses);
}
