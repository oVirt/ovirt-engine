package org.ovirt.engine.core.utils.lock;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

/**
 *The following class is represent a lock which is used in the system
 */
public class EngineLock {

    private Map<Guid, String> exclusiveLocks;
    private Map<Guid, String> sharedLocks;

    public EngineLock() {

    }

    public EngineLock(Map<Guid, String> exclusiveLocks, Map<Guid, String> sharedLocks) {
        setExclusiveLocks(exclusiveLocks);
        setSharedLocks(sharedLocks);
    }

    public Map<Guid, String> getExclusiveLocks() {
        return exclusiveLocks;
    }

    public void setExclusiveLocks(Map<Guid, String> exclusiveLocks) {
        this.exclusiveLocks = exclusiveLocks;
    }

    public Map<Guid, String> getSharedLocks() {
        return sharedLocks;
    }

    public void setSharedLocks(Map<Guid, String> sharedLocks) {
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

    private void buildEntryStrings(Map<Guid, String> locks, StringBuilder message) {
        if (locks != null) {
            for (Map.Entry<Guid, String> entry : locks.entrySet()) {
                message.append("key: ").append(entry.getKey()).append(" value: ").append(entry.getValue()).append('\n');
            }
        }
    }

}
