package org.ovirt.engine.core.bll.lock;

import java.util.List;

/**
 * The following interface is used as interface for JMX bean
 */
public interface LockManagerMonitorMXBean {

    /**
     * The following method will return all locks currently kept in the system
     */
    List<String> showAllLocks();

    /**
     * The following method will allow to clear all locks via JMX console
     */
    void clear();

    /**
     * The following method will release a lock with provided lockId
     */
    boolean releaseLock(String lockId);
}
