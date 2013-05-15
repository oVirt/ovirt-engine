package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.mode.ApplicationMode;

public enum ActionGroup {
    // vm actions groups
    CREATE_VM(1, RoleType.USER, VdcObjectType.VM, false, ApplicationMode.VirtOnly),
    DELETE_VM(2, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    EDIT_VM_PROPERTIES(3, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    VM_BASIC_OPERATIONS(4, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    CHANGE_VM_CD(5, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    MIGRATE_VM(6, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),

    /**
     * Connect to the console of a virtual machine, but only if no user
     * has connected before:
     */
    CONNECT_TO_VM(7, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),

    IMPORT_EXPORT_VM(8, RoleType.ADMIN, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    CONFIGURE_VM_NETWORK(9, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    CONFIGURE_VM_STORAGE(10, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    MOVE_VM(11, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    MANIPULATE_VM_SNAPSHOTS(12, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),

    /**
     * Connect to the console of a virtual machine even if a different
     * user was connected before:
     */
    RECONNECT_TO_VM(13, RoleType.USER, VdcObjectType.VM, true, ApplicationMode.VirtOnly),

    CHANGE_VM_CUSTOM_PROPERTIES(14, RoleType.ADMIN, VdcObjectType.VM, true, ApplicationMode.VirtOnly),
    /**
     * Admin role can specify destinationVdsId to override default target host.
     */
    EDIT_ADMIN_VM_PROPERTIES(15, RoleType.ADMIN, VdcObjectType.VM, true, ApplicationMode.VirtOnly),

    // host (vds) actions groups
    CREATE_HOST(100, RoleType.ADMIN, VdcObjectType.VDS, true),
    EDIT_HOST_CONFIGURATION(101, RoleType.ADMIN, VdcObjectType.VDS, true),
    DELETE_HOST(102, RoleType.ADMIN, VdcObjectType.VDS, true),
    MANIPUTLATE_HOST(103, RoleType.ADMIN, VdcObjectType.VDS, true),
    CONFIGURE_HOST_NETWORK(104, RoleType.ADMIN, VdcObjectType.VDS, true),
    // templates actions groups
    CREATE_TEMPLATE(200, RoleType.USER, VdcObjectType.VmTemplate, false, ApplicationMode.VirtOnly),
    EDIT_TEMPLATE_PROPERTIES(201, RoleType.USER, VdcObjectType.VmTemplate, true, ApplicationMode.VirtOnly),
    DELETE_TEMPLATE(202, RoleType.USER, VdcObjectType.VmTemplate, true, ApplicationMode.VirtOnly),
    COPY_TEMPLATE(203, RoleType.USER, VdcObjectType.VmTemplate, true, ApplicationMode.VirtOnly),
    CONFIGURE_TEMPLATE_NETWORK(204, RoleType.USER, VdcObjectType.VmTemplate, true, ApplicationMode.VirtOnly),
    EDIT_ADMIN_TEMPLATE_PROPERTIES(205, RoleType.ADMIN, VdcObjectType.VmTemplate, true, ApplicationMode.VirtOnly),
    // vm pools actions groups
    CREATE_VM_POOL(300, RoleType.USER, VdcObjectType.VmPool, false, ApplicationMode.VirtOnly),
    EDIT_VM_POOL_CONFIGURATION(301, RoleType.USER, VdcObjectType.VmPool, true, ApplicationMode.VirtOnly),
    DELETE_VM_POOL(302, RoleType.USER, VdcObjectType.VmPool, true, ApplicationMode.VirtOnly),
    VM_POOL_BASIC_OPERATIONS(303, RoleType.USER, VdcObjectType.VmPool, true, ApplicationMode.VirtOnly),
    // clusters actions groups
    CREATE_CLUSTER(400, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    EDIT_CLUSTER_CONFIGURATION(401, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    DELETE_CLUSTER(402, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    CONFIGURE_CLUSTER_NETWORK(403, RoleType.ADMIN, VdcObjectType.VdsGroups, true),
    ASSIGN_CLUSTER_NETWORK(404, RoleType.ADMIN, VdcObjectType.Network, true),

    // users and MLA actions groups
    MANIPULATE_USERS(500, RoleType.ADMIN, VdcObjectType.User, true),
    MANIPULATE_ROLES(501, RoleType.ADMIN, VdcObjectType.User, true),
    MANIPULATE_PERMISSIONS(502, RoleType.USER, VdcObjectType.User, true),
    // storage domains actions groups
    CREATE_STORAGE_DOMAIN(600, RoleType.ADMIN, VdcObjectType.Storage, true, ApplicationMode.VirtOnly),
    EDIT_STORAGE_DOMAIN_CONFIGURATION(601, RoleType.ADMIN, VdcObjectType.Storage, true, ApplicationMode.VirtOnly),
    DELETE_STORAGE_DOMAIN(602, RoleType.ADMIN, VdcObjectType.Storage, true, ApplicationMode.VirtOnly),
    MANIPULATE_STORAGE_DOMAIN(603, RoleType.ADMIN, VdcObjectType.Storage, true, ApplicationMode.VirtOnly),
    // storage pool actions groups
    CREATE_STORAGE_POOL(700, RoleType.ADMIN, VdcObjectType.StoragePool, true, ApplicationMode.VirtOnly),
    DELETE_STORAGE_POOL(701, RoleType.ADMIN, VdcObjectType.StoragePool, true, ApplicationMode.VirtOnly),
    EDIT_STORAGE_POOL_CONFIGURATION(702, RoleType.ADMIN, VdcObjectType.StoragePool, true, ApplicationMode.VirtOnly),
    CONFIGURE_STORAGE_POOL_NETWORK(703, RoleType.ADMIN, VdcObjectType.Network, true),
    CREATE_STORAGE_POOL_NETWORK(704, RoleType.ADMIN, VdcObjectType.StoragePool, true),
    DELETE_STORAGE_POOL_NETWORK(705, RoleType.ADMIN, VdcObjectType.Network, true),

    // engine generic
    CONFIGURE_ENGINE(800, RoleType.ADMIN, VdcObjectType.System, true),

    // Quota
    CONFIGURE_QUOTA(900, RoleType.ADMIN, VdcObjectType.Quota, true, ApplicationMode.VirtOnly),
    CONSUME_QUOTA(901, RoleType.USER, VdcObjectType.Quota, true, ApplicationMode.VirtOnly),

    // Gluster
    CREATE_GLUSTER_VOLUME(1000, RoleType.ADMIN, VdcObjectType.GlusterVolume, true, ApplicationMode.GlusterOnly),
    MANIPULATE_GLUSTER_VOLUME(1001, RoleType.ADMIN, VdcObjectType.GlusterVolume, true, ApplicationMode.GlusterOnly),
    DELETE_GLUSTER_VOLUME(1002, RoleType.ADMIN, VdcObjectType.GlusterVolume, true, ApplicationMode.GlusterOnly),
    MANIPULATE_GLUSTER_HOOK(1003, RoleType.ADMIN, VdcObjectType.GlusterHook, true, ApplicationMode.GlusterOnly),
    MANIPULATE_GLUSTER_SERVICE(1004, RoleType.ADMIN, VdcObjectType.GlusterService, true, ApplicationMode.GlusterOnly),

    // Disks action groups
    CREATE_DISK(1100, RoleType.USER, VdcObjectType.Disk, false, ApplicationMode.VirtOnly),
    ATTACH_DISK(1101, RoleType.USER, VdcObjectType.Disk, true, ApplicationMode.VirtOnly),
    EDIT_DISK_PROPERTIES(1102, RoleType.USER, VdcObjectType.Disk, true, ApplicationMode.VirtOnly),
    CONFIGURE_DISK_STORAGE(1103, RoleType.USER, VdcObjectType.Disk, true, ApplicationMode.VirtOnly),
    DELETE_DISK(1104, RoleType.USER, VdcObjectType.Disk, true, ApplicationMode.VirtOnly),
    CONFIGURE_SCSI_GENERIC_IO(1105, RoleType.ADMIN, VdcObjectType.Disk, true, ApplicationMode.VirtOnly),

    // Network
    PORT_MIRRORING(1200, RoleType.ADMIN, VdcObjectType.Network, true, ApplicationMode.VirtOnly),

    // Login action group
    LOGIN(1300, RoleType.USER, VdcObjectType.Bottom, false),

    // Inject external events action group
    INJECT_EXTERNAL_EVENTS(1400, RoleType.ADMIN, VdcObjectType.Event, false);

    private int id;
    private RoleType roleType;
    private VdcObjectType vdcObjectType;
    private boolean allowsViewingChildren;
    private int applicationMode = ApplicationMode.AllModes.getValue();
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

    private ActionGroup(int value, RoleType type, VdcObjectType objectType, boolean allowsViewingChildren) {
        id = value;
        roleType = type;
        vdcObjectType = objectType;
        this.allowsViewingChildren = allowsViewingChildren;
    }

    private ActionGroup(int value,
            RoleType type,
            VdcObjectType objectType,
            boolean allowsViewingChildren,
            ApplicationMode applicationMode) {
        this(value, type, objectType, allowsViewingChildren);
        this.applicationMode = applicationMode.getValue();
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

    public boolean allowsViewingChildren() {
        return allowsViewingChildren;
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

    public int getAvailableInModes() {
        return applicationMode;
    }
}
