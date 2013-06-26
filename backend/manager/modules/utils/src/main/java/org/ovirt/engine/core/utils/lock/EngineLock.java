package org.ovirt.engine.core.utils.lock;

import java.util.Map;

import org.ovirt.engine.core.common.utils.Pair;

/**
 *The following class is represent a lock which is used in the system
 */
public class EngineLock implements AutoCloseable {

    private Map<String, Pair<String, String>> exclusiveLocks;
    private Map<String, Pair<String, String>> sharedLocks;

    public EngineLock() {

    }

    public EngineLock(Map<String, Pair<String, String>> exclusiveLocks, Map<String, Pair<String, String>> sharedLocks) {
        this.exclusiveLocks = exclusiveLocks;
        this.sharedLocks = sharedLocks;
    }

    public Map<String, Pair<String, String>> getExclusiveLocks() {
        return exclusiveLocks;
    }

    public void setExclusiveLocks(Map<String, Pair<String, String>> exclusiveLocks) {
        this.exclusiveLocks = exclusiveLocks;
    }

    public Map<String, Pair<String, String>> getSharedLocks() {
        return sharedLocks;
    }

    public void setSharedLocks(Map<String, Pair<String, String>> sharedLocks) {
        this.sharedLocks = sharedLocks;
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder("EngineLock [exclusiveLocks= ");
        buildEntryStrings(exclusiveLocks, message);
        message.append(", sharedLocks= ");
        buildEntryStrings(sharedLocks, message);
        message.append("]");
        return message.toString();
    }

    private void buildEntryStrings(Map<String, Pair<String, String>> locks, StringBuilder message) {
        if (locks != null) {
            for (Map.Entry<String, Pair<String, String>> entry : locks.entrySet()) {
                message.append("key: ").append(entry.getKey()).append(" value: ").append(entry.getValue().getFirst()).append('\n');
            }
        }
    }

    @Override
    public void close() {
        LockManagerFactory.getLockManager().releaseLock(this);
    }

}
