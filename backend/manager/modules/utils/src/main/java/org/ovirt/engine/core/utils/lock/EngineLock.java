package org.ovirt.engine.core.utils.lock;

import java.util.Map;

/**
 *The following class is represent a lock which is used in the system
 */
public class EngineLock {

    private Map<String, String> exclusiveLocks;
    private Map<String, String> sharedLocks;

    public EngineLock() {

    }

    public EngineLock(Map<String, String> exclusiveLocks, Map<String, String> sharedLocks) {
        setExclusiveLocks(exclusiveLocks);
        setSharedLocks(sharedLocks);
    }

    public Map<String, String> getExclusiveLocks() {
        return exclusiveLocks;
    }

    public void setExclusiveLocks(Map<String, String> exclusiveLocks) {
        this.exclusiveLocks = exclusiveLocks;
    }

    public Map<String, String> getSharedLocks() {
        return sharedLocks;
    }

    public void setSharedLocks(Map<String, String> sharedLocks) {
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

    private void buildEntryStrings(Map<String, String> locks, StringBuilder message) {
        if (locks != null) {
            for (Map.Entry<String, String> entry : locks.entrySet()) {
                message.append("key: ").append(entry.getKey()).append(" value: ").append(entry.getValue()).append('\n');
            }
        }
    }

}
