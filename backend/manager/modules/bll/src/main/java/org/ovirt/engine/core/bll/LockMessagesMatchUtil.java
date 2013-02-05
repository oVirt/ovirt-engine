package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class LockMessagesMatchUtil {

    public static final Pair<String, String> POOL = new Pair<String, String>(LockingGroup.POOL.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> VDS = new Pair<String, String>(LockingGroup.VDS.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> VDS_FENCE = new Pair<String, String>(LockingGroup.VDS_FENCE.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> VM = new Pair<String, String>(LockingGroup.VM.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> TEMPLATE = new Pair<String, String>(LockingGroup.TEMPLATE.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> DISK = new Pair<String, String>(LockingGroup.DISK.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> VM_DISK_BOOT = new Pair<String, String>(LockingGroup.VM_DISK_BOOT.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> VM_NAME = new Pair<String, String>(LockingGroup.VM_NAME.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> STORAGE = new Pair<String, String>(LockingGroup.STORAGE.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> REGISTER_VDS = new Pair<String, String>(LockingGroup.REGISTER_VDS.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> VM_SNAPSHOTS = new Pair<String, String>(LockingGroup.VM_SNAPSHOTS.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> GLUSTER = new Pair<String, String>(LockingGroup.GLUSTER.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
    public static final Pair<String, String> USER_VM_POOL = new Pair<String, String>(LockingGroup.USER_VM_POOL.name(),
            VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());

}
