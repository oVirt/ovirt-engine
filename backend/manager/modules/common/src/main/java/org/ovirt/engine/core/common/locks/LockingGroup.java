package org.ovirt.engine.core.common.locks;

public enum LockingGroup {

    POOL,
    VDS,
    VDS_INIT,
    VDS_FENCE,
    VM,
    TEMPLATE,
    DISK,
    VM_DISK_BOOT,
    VM_NAME,
    NETWORK,
    STORAGE,
    STORAGE_CONNECTION,
    STORAGE_CONNECTION_EXTENSION,
    REGISTER_VDS,
    VM_SNAPSHOTS,
    GLUSTER,
    /** this group is used to lock geo-replication session */
    GLUSTER_GEOREP,
    /** this group is used for gluster volume snapshot purpose */
    GLUSTER_SNAPSHOT,
    /** this group is used to lock Network Manipulations in a host */
    HOST_NETWORK,
    /** this group is used to lock Storage Devices in the host */
    HOST_STORAGE_DEVICES,
    USER_VM_POOL,
    /** This group is used to lock template which is in export domain */
    REMOTE_TEMPLATE,
    /** This group is used to lock VM which is in export domain */
    REMOTE_VM,
    OVF_UPDATE,
    SYNC_LUNS,
    /** This group is used for indication that an operation is executed using the specified host */
    VDS_EXECUTION,
    VDS_POOL_AND_STORAGE_CONNECTIONS,
    /** This group is used for indication that a storage manipulating operation is executed on a domain */
    DOMAIN_EXECUTION,
    /** This group is used to lock host for operations that allocate host's devices to VMs */
    HOST_DEVICES,
    VM_DISK_HOT_PLUG,
    VM_POOL,
    VM_POOL_NAME;

}
