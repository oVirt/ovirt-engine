package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class LockMessagesMatchUtil {
    public static final Pair<String, String> POOL         = makeLockingPair(LockingGroup.POOL);
    public static final Pair<String, String> VDS          = makeLockingPair(LockingGroup.VDS);
    public static final Pair<String, String> VDS_FENCE    = makeLockingPair(LockingGroup.VDS_FENCE);
    public static final Pair<String, String> VM           = makeLockingPair(LockingGroup.VM);
    public static final Pair<String, String> TEMPLATE     = makeLockingPair(LockingGroup.TEMPLATE);
    public static final Pair<String, String> DISK         = makeLockingPair(LockingGroup.DISK);
    public static final Pair<String, String> VM_DISK_BOOT = makeLockingPair(LockingGroup.VM_DISK_BOOT);
    public static final Pair<String, String> VM_NAME      = makeLockingPair(LockingGroup.VM_NAME);
    public static final Pair<String, String> STORAGE      = makeLockingPair(LockingGroup.STORAGE);
    public static final Pair<String, String> STORAGE_CONNECTION = makeLockingPair(LockingGroup.STORAGE_CONNECTION);
    public static final Pair<String, String> REGISTER_VDS = makeLockingPair(LockingGroup.REGISTER_VDS);
    public static final Pair<String, String> VM_SNAPSHOTS = makeLockingPair(LockingGroup.VM_SNAPSHOTS);
    public static final Pair<String, String> GLUSTER      = makeLockingPair(LockingGroup.GLUSTER);
    public static final Pair<String, String> USER_VM_POOL = makeLockingPair(LockingGroup.USER_VM_POOL);

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
        return new Pair<String, String>(group.name(), message);
    }

    /**
     * Factory method that returns a pair of the given locking group and the default
     * error message to display when the lock cannot be acquired
     *
     * @param group locking group
     * @return {@link Pair} of the given locking group and default cannot-acquire-lock can-do message
     * @see {@link LockMessagesMatchUtil#makeLockingPair(LockingGroup, VdcBllMessages)}
     */
    private static Pair<String, String> makeLockingPair(LockingGroup group) {
        return makeLockingPair(group, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED);
    }
}
