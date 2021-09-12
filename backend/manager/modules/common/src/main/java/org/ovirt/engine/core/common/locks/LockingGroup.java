package org.ovirt.engine.core.common.locks;

public enum LockingGroup {

    POOL,
    VDS,
    VDS_INIT,
    VDS_FENCE,
    VM,
    TEMPLATE,
    TEMPLATE_NAME,
    /** locked entity identifier: "deviceId", vmId is omitted */
    VM_DEVICE,
    DISK,
    VM_DISK_BOOT,
    VM_NAME,
    NETWORK,
    /** this groups is used to lock networks of an external provider */
    PROVIDER,
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
    /** this group is used to ensure there are no parallel runs of storage sync*/
    GLUSTER_STORAGE_DOMAIN_SYNC,
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
    /** This group is used for indication that an operation is executed using the specified host */
    VDS_EXECUTION,
    VDS_POOL_AND_STORAGE_CONNECTIONS,
    /** This group is used to lock host for operations that allocate host's devices to VMs */
    HOST_DEVICES,
    VM_POOL,
    VM_POOL_NAME,
    /** This group is used when examining whether the last VM is detached from the pool */
    VM_POOL_DETACH,
    LIVE_STORAGE_MIGRATION,
    VM_BACKUP;
}
