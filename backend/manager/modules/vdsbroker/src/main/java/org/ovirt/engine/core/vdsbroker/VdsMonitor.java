package org.ovirt.engine.core.vdsbroker;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VdsMonitor {

    private final Lock lock;
    private final Condition decreasedMemoryCondition;

    public VdsMonitor() {
        lock = new ReentrantLock();
        decreasedMemoryCondition = getLock().newCondition();
    }

    /**
     * A lock for the enclosing VDS
     *
     * @return
     */
    public Lock getLock() {
        return lock;
    }

    /**
     * a signal condition to communicate updates the decreased amount of memory this vds reserevs for scheduling a VM
     *
     * @return
     */
    public Condition getDecreasedMemoryCondition() {
        return decreasedMemoryCondition;
    }

}
