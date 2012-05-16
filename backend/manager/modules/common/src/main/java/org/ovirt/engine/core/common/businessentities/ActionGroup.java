package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;

public enum ActionGroup {
    // vm actions groups
    CREATE_VM(1, RoleType.USER, VdcObjectType.VM, false),
    DELETE_VM(2, RoleType.USER, VdcObjectType.VM, true),
    EDIT_VM_PROPERTIES(3, RoleType.USER, VdcObjectType.VM, true),
    VM_BASIC_OPERATIONS(4, RoleType.USER, VdcObjectType.VM, true),
    CHANGE_VM_CD(5, RoleType.USER, VdcObjectType.VM, true),
    MIGRATE_VM(6, RoleType.USER, VdcObjectType.VM, true),

    /**
     * Connect to the console of a virtual machine, but only if no user
     * has connected before:
     */
    CONNECT_TO_VM(7, RoleType.USER, VdcObjectType.VM, true),

    IMPORT_EXPORT_VM(8, RoleType.ADMIN, VdcObjectType.VM, true),
    CONFIGURE_VM_NETWORK(9, RoleType.USER, VdcObjectType.VM, true),
    CONFIGURE_VM_STORAGE(10, RoleType.USER, VdcObjectType.VM, true),
    MOVE_VM(11, RoleType.USER, VdcObjectType.VM, true),
    MANIPULATE_VM_SNAPSHOTS(12, RoleType.USER, VdcObjectType.VM, true),

    /**
     * Connect to the console of a virtual machine even if a different
     * user was connected before:
     */
    RECONNECT_TO_VM(13, RoleType.USER, VdcObjectType.VM, true),

    // host (vds) actions groups
    CREATE_HOST(100, RoleType.ADMIN, VdcObjectType.VDS, true),
    EDIT_HOST_CONFIGURATION(101, RoleType.ADMIN, VdcObjectType.VDS, true),
    DELETE_HOST(102, RoleType.ADMIN, VdcObjectType.VDS, true),
    MANIPUTLATE_HOST(103, RoleType.ADMIN, VdcObjectType.VDS, true),
    CONFIGURE_HOST_NETWORK(104, RoleType.ADMIN, VdcObjectType.VDS, true),
    // templates actions groups
    CREATE_TEMPLATE(200, RoleType.USER, VdcObjectType.VmTemplate, false),
    EDIT_TEMPLATE_PROPERTIES(201, RoleType.USER, VdcObjectType.VmTemplate, true),
    DELETE_TEMPLATE(202, RoleType.USER, VdcObjectType.VmTemplate, true),
    COPY_TEMPLATE(203, RoleType.USER, VdcObjectType.VmTemplate, true),
    CONFIGURE_TEMPLATE_NETWORK(204, RoleType.USER, VdcObjectType.VmTemplate, true),
    // vm pools actions groups
    CREATE_VM_POOL(300, RoleType.USER, VdcObjectType.VmPool, false),
    EDIT_VM_POOL_CONFIGURATION(301, RoleType.USER, VdcObjectType.VmPool, true),
    DELETE_VM_POOL(302, RoleType.USER, VdcObjectType.VmPool, true),
    VM_POOL_BASIC_OPERATIONS(303, RoleType.USER, VdcObjectType.VmPool, true),
    // clusters actions groups
    CREATE_CLUSTER(400, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    EDIT_CLUSTER_CONFIGURATION(401, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    DELETE_CLUSTER(402, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    CONFIGURE_CLUSTER_NETWORK(403, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    // users and MLA actions groups
    MANIPULATE_USERS(500, RoleType.ADMIN, VdcObjectType.User, true),
    MANIPULATE_ROLES(501, RoleType.ADMIN, VdcObjectType.User, true),
    MANIPULATE_PERMISSIONS(502, RoleType.USER, VdcObjectType.User, true),
    // storage domains actions groups
    CREATE_STORAGE_DOMAIN(600, RoleType.ADMIN, VdcObjectType.Storage, true),
    EDIT_STORAGE_DOMAIN_CONFIGURATION(601, RoleType.ADMIN, VdcObjectType.Storage, true),
    DELETE_STORAGE_DOMAIN(602, RoleType.ADMIN, VdcObjectType.Storage, true),
    MANIPULATE_STORAGE_DOMAIN(603, RoleType.ADMIN, VdcObjectType.Storage, true),
    // storage pool actions groups
    CREATE_STORAGE_POOL(700, RoleType.ADMIN, VdcObjectType.StoragePool, true),
    DELETE_STORAGE_POOL(701, RoleType.ADMIN, VdcObjectType.StoragePool, true),
    EDIT_STORAGE_POOL_CONFIGURATION(702, RoleType.ADMIN, VdcObjectType.StoragePool, true),
    CONFIGURE_STORAGE_POOL_NETWORK(703, RoleType.ADMIN, VdcObjectType.StoragePool, true),

    // engine generic
    CONFIGURE_ENGINE(800, RoleType.ADMIN, VdcObjectType.System, true),

    // Quota
    CONFIGURE_QUOTA(900, RoleType.ADMIN, VdcObjectType.Quota, true),
    CONSUME_QUOTA(901, RoleType.USER, VdcObjectType.Quota, true),

    // Gluster
    CREATE_GLUSTER_VOLUME(1000, RoleType.ADMIN, VdcObjectType.GlusterVolume, true),
    MANIPULATE_GLUSTER_VOLUME(1001, RoleType.ADMIN, VdcObjectType.GlusterVolume, true),
    DELETE_GLUSTER_VOLUME(1002, RoleType.ADMIN, VdcObjectType.GlusterVolume, true),

    // Disks action groups
    CREATE_DISK(1100, RoleType.USER, VdcObjectType.Disk, false),
    ATTACH_DISK(1101, RoleType.USER, VdcObjectType.Disk, true),
    EDIT_DISK_PROPERTIES(1102, RoleType.USER, VdcObjectType.Disk, true),
    CONFIGURE_DISK_STORAGE(1103, RoleType.USER, VdcObjectType.Disk, true),
    DELETE_DISK(1104, RoleType.USER, VdcObjectType.Disk, true);

    private int id;
    private RoleType roleType;
    private VdcObjectType vdcObjectType;
    private boolean isInheritable;
    private static HashMap<Integer, ActionGroup> map = new HashMap<Integer, ActionGroup>(ActionGroup.values().length);
    private static HashMap<VdcObjectType, ArrayList<ActionGroup>> entityToActionGroupsMapping =
            new HashMap<VdcObjectType, ArrayList<ActionGroup>>();
    private static List<ActionGroup> userActionsGroup = new ArrayList<ActionGroup>();

    static {
        initEntitiesMap();
        for (ActionGroup t : ActionGroup.values()) {
            map.put(t.id, t);
            entityToActionGroupsMapping.get(t.vdcObjectType).add(t);
            if (RoleType.USER == t.roleType) {
                userActionsGroup.add(t);
            }
        }
    }

    private ActionGroup(int value, RoleType type, VdcObjectType objectType, boolean isInheritable) {
        id = value;
        roleType = type;
        vdcObjectType = objectType;
        this.isInheritable = isInheritable;
    }

    private static void initEntitiesMap() {
        for (VdcObjectType obj : VdcObjectType.values())
            entityToActionGroupsMapping.put(obj, new ArrayList<ActionGroup>());
    }

    public int getId() {
        return id;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public boolean isInheritable() {
        return isInheritable;
    }

    public static ActionGroup forValue(int value) {
        return map.get(value);
    }

    public static List<ActionGroup> getAllUserActionGroups() {
        return userActionsGroup;
    }

    public static HashMap<VdcObjectType, ArrayList<ActionGroup>> getEntityToActionGroupsMapping() {
        return entityToActionGroupsMapping;
    }
}
