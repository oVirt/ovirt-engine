package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;

public class LockMessagesMatchUtil {

    /**
     * Helper factory method that creates a pair of locking group and error message
     * that would be displayed when lock cannot be acquired
     *
     * @param group locking group
     * @param message message to be shown when the lock cannot be acquired
     * @return {@link Pair} of the given locking group and error message as Strings
     */
    public static Pair<String, String> makeLockingPair(LockingGroup group, VdcBllMessages message) {
        return makeLockingPair(group, message.name());
    }

    /**
     * Helper factory method that creates a pair of locking group and error message
     * that would be displayed when lock cannot be acquired
     *
     * @param group locking group
     * @param message message to be shown when the lock cannot be acquired
     * @return {@link Pair} of the given locking group and error message as Strings
     */
    public static Pair<String, String> makeLockingPair(LockingGroup group, String message) {
        return new Pair<>(group.name(), message);
    }
}
