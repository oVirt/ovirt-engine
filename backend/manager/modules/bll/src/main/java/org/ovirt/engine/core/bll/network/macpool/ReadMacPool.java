package org.ovirt.engine.core.bll.network.macpool;

import org.ovirt.engine.core.compat.Guid;
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
     * @return number of MACs in pool.
     */
    @AcquireReadLock
    int getTotalMacsCount();

    /**
     * Please note, that what this method returns needs not to be accurate. In transactional execution after you
     * release mac, this will be kept used until end of transaction, and only after that it will be released. So if you
     * release mac and invoke this method before tx ends, you'll get 'unexpected' result.
     *
     * @param mac MAC to check.
     * @return true if mac is used.
     */
    @AcquireReadLock
    boolean isMacInUse(String mac);

    boolean isDuplicateMacAddressesAllowed();

    boolean isMacInRange(String mac);

    /**
     *
     * @return ID of this MacPool. Most often this should return ID of underlying DB record representing mac pool.
     */
    Guid getId();

    /**
     * @return true if this MacPool contains duplicates.
     */
    @AcquireReadLock
    boolean containsDuplicates();

    /**
     * @return the mac storage associated with this mac pool
     */
    MacsStorage getMacsStorage();

    /**
     * @return true if this mac pool has overlapping ranges with the specified mac pool
     */
    @AcquireReadLock
    boolean overlaps(MacPool other);

}
