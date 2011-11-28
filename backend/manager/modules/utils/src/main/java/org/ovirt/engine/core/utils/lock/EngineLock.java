package org.ovirt.engine.core.utils.lock;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;

/**
 *The following class is represent a lock which is used in the system
 */
public class EngineLock {

    private Map<String, Guid> exclusiveLocks;
    private Map<String, Guid> sharedLocks;

    public Map<String, Guid> getExclusiveLocks() {
        return exclusiveLocks;
    }

    public void setExclusiveLocks(Map<String, Guid> exclusiveLocks) {
        this.exclusiveLocks = exclusiveLocks;
    }

    public Map<String, Guid> getSharedLocks() {
        return sharedLocks;
    }

    public void setSharedLocks(Map<String, Guid> sharedLocks) {
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

    private void buildEntryStrings(Map<String, Guid> locks, StringBuilder message) {
        if(locks != null) {
            for(Map.Entry<String, Guid> entry : locks.entrySet()) {
                message.append("key: ").append(entry.getKey()).append(" value: ").append(entry.getValue()).append('\n');
            }
        }
    }

}
