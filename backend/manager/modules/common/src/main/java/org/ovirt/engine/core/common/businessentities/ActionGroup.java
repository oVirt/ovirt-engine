package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.mode.ApplicationMode;

public enum ActionGroup {
    // vm actions groups
    CREATE_VM(1, RoleType.USER, false, ApplicationMode.VirtOnly),
    DELETE_VM(2, RoleType.USER, true, ApplicationMode.VirtOnly),
    EDIT_VM_PROPERTIES(3, RoleType.USER, true, ApplicationMode.VirtOnly),
    REBOOT_VM(17, RoleType.USER, true, ApplicationMode.VirtOnly),
    RESET_VM(23, RoleType.USER, true, ApplicationMode.VirtOnly),
    STOP_VM(18, RoleType.USER, true, ApplicationMode.VirtOnly),
    SHUT_DOWN_VM(19, RoleType.USER, true, ApplicationMode.VirtOnly),
    HIBERNATE_VM(21, RoleType.USER, true, ApplicationMode.VirtOnly),
    RUN_VM(22, RoleType.USER, true, ApplicationMode.VirtOnly),
    CHANGE_VM_CD(5, RoleType.USER, true, ApplicationMode.VirtOnly),
    MIGRATE_VM(6, RoleType.USER, true, ApplicationMode.VirtOnly),
    CONNECT_TO_SERIAL_CONSOLE(1664, RoleType.USER, true, ApplicationMode.VirtOnly),
    /**
     * Connect to the console of a virtual machine, but only if no user
     * has connected before:
     */
    CONNECT_TO_VM(7, RoleType.USER, true, ApplicationMode.VirtOnly),

    IMPORT_EXPORT_VM(8, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    CONFIGURE_VM_NETWORK(9, RoleType.USER, true, ApplicationMode.VirtOnly),
    CONFIGURE_VM_STORAGE(10, RoleType.USER, true, ApplicationMode.VirtOnly),
    MOVE_VM(11, RoleType.USER, true, ApplicationMode.VirtOnly),
    MANIPULATE_VM_SNAPSHOTS(12, RoleType.USER, true, ApplicationMode.VirtOnly),

    /**
     * Connect to the console of a virtual machine even if a different
     * user was connected before:
     */
    RECONNECT_TO_VM(13, RoleType.USER, true, ApplicationMode.VirtOnly),

    CHANGE_VM_CUSTOM_PROPERTIES(14, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    /**
     * Admin role can specify destinationVdsId to override default target host.
     */
    EDIT_ADMIN_VM_PROPERTIES(15, RoleType.ADMIN, true, ApplicationMode.VirtOnly),

    CREATE_INSTANCE(16, RoleType.USER, false, ApplicationMode.VirtOnly),

    // host (vds) actions groups
    CREATE_HOST(100, RoleType.ADMIN, true),
    EDIT_HOST_CONFIGURATION(101, RoleType.ADMIN, true),
    DELETE_HOST(102, RoleType.ADMIN, true),
    MANIPULATE_HOST(103, RoleType.ADMIN, true),
    CONFIGURE_HOST_NETWORK(104, RoleType.ADMIN, true),
    // templates actions groups
    CREATE_TEMPLATE(200, RoleType.USER, false, ApplicationMode.VirtOnly),
    EDIT_TEMPLATE_PROPERTIES(201, RoleType.USER, true, ApplicationMode.VirtOnly),
    DELETE_TEMPLATE(202, RoleType.USER, true, ApplicationMode.VirtOnly),
    COPY_TEMPLATE(203, RoleType.USER, true, ApplicationMode.VirtOnly),
    CONFIGURE_TEMPLATE_NETWORK(204, RoleType.USER, true, ApplicationMode.VirtOnly),
    EDIT_ADMIN_TEMPLATE_PROPERTIES(205, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    // vm pools actions groups
    CREATE_VM_POOL(300, RoleType.USER, false, ApplicationMode.VirtOnly),
    EDIT_VM_POOL_CONFIGURATION(301, RoleType.USER, true, ApplicationMode.VirtOnly),
    DELETE_VM_POOL(302, RoleType.USER, true, ApplicationMode.VirtOnly),
    VM_POOL_BASIC_OPERATIONS(303, RoleType.USER, true, ApplicationMode.VirtOnly),
    // clusters actions groups
    CREATE_CLUSTER(400, RoleType.ADMIN, true),
    EDIT_CLUSTER_CONFIGURATION(401, RoleType.ADMIN, true),
    DELETE_CLUSTER(402, RoleType.ADMIN, true),
    CONFIGURE_CLUSTER_NETWORK(403, RoleType.ADMIN, true),
    ASSIGN_CLUSTER_NETWORK(404, RoleType.ADMIN, true),

    // users and MLA actions groups
    MANIPULATE_USERS(500, RoleType.ADMIN, true),
    MANIPULATE_ROLES(501, RoleType.ADMIN, true),
    MANIPULATE_PERMISSIONS(502, RoleType.USER, true),
    ADD_USERS_AND_GROUPS_FROM_DIRECTORY(503, RoleType.USER, true),
    EDIT_PROFILE(504, RoleType.USER, true),

    // storage domains actions groups
    CREATE_STORAGE_DOMAIN(600, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    EDIT_STORAGE_DOMAIN_CONFIGURATION(601, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    DELETE_STORAGE_DOMAIN(602, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    MANIPULATE_STORAGE_DOMAIN(603, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    // storage pool actions groups
    CREATE_STORAGE_POOL(700, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    DELETE_STORAGE_POOL(701, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    EDIT_STORAGE_POOL_CONFIGURATION(702, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    CONFIGURE_STORAGE_POOL_NETWORK(703, RoleType.ADMIN, true),
    CREATE_STORAGE_POOL_NETWORK(704, RoleType.ADMIN, true),
    DELETE_STORAGE_POOL_NETWORK(705, RoleType.ADMIN, true),

    // engine generic
    CONFIGURE_ENGINE(800, RoleType.ADMIN, true),

    // Quota
    CONFIGURE_QUOTA(900, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    CONSUME_QUOTA(901, RoleType.USER, true, ApplicationMode.VirtOnly),

    // Gluster
    CREATE_GLUSTER_VOLUME(1000, RoleType.ADMIN, true, ApplicationMode.GlusterOnly),
    MANIPULATE_GLUSTER_VOLUME(1001, RoleType.ADMIN, true, ApplicationMode.GlusterOnly),
    DELETE_GLUSTER_VOLUME(1002, RoleType.ADMIN, true, ApplicationMode.GlusterOnly),
    MANIPULATE_GLUSTER_HOOK(1003, RoleType.ADMIN, true, ApplicationMode.GlusterOnly),
    MANIPULATE_GLUSTER_SERVICE(1004, RoleType.ADMIN, true, ApplicationMode.GlusterOnly),

    // Disks action groups
    CREATE_DISK(1100, RoleType.USER, false, ApplicationMode.VirtOnly),
    ATTACH_DISK(1101, RoleType.USER, true, ApplicationMode.VirtOnly),
    EDIT_DISK_PROPERTIES(1102, RoleType.USER, true, ApplicationMode.VirtOnly),
    CONFIGURE_DISK_STORAGE(1103, RoleType.USER, true, ApplicationMode.VirtOnly),
    DELETE_DISK(1104, RoleType.USER, true, ApplicationMode.VirtOnly),
    CONFIGURE_SCSI_GENERIC_IO(1105, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    ACCESS_IMAGE_STORAGE(1106, RoleType.USER, false, ApplicationMode.VirtOnly),
    DISK_LIVE_STORAGE_MIGRATION(1107, RoleType.USER, true, ApplicationMode.VirtOnly),
    SPARSIFY_DISK(1108, RoleType.USER, true, ApplicationMode.VirtOnly),
    REDUCE_DISK(1109, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    BACKUP_DISK(1110, RoleType.ADMIN, true, ApplicationMode.VirtOnly),

    // VNIC Profiles
    CONFIGURE_NETWORK_VNIC_PROFILE(1203, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    CREATE_NETWORK_VNIC_PROFILE(1204, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    DELETE_NETWORK_VNIC_PROFILE(1205, RoleType.ADMIN, true, ApplicationMode.VirtOnly),

    // Login action group
    LOGIN(1300, RoleType.USER, false),

    // Inject external events action group
    INJECT_EXTERNAL_EVENTS(1400, RoleType.ADMIN, false),

    // Inject external tasks action group
    INJECT_EXTERNAL_TASKS(1500, RoleType.ADMIN, false),

    // Tag management action group
    TAG_MANAGEMENT(1301, RoleType.ADMIN, false),

    // Bookmark management action group
    BOOKMARK_MANAGEMENT(1302, RoleType.ADMIN, false),

    // Event notification management action group
    EVENT_NOTIFICATION_MANAGEMENT(1303, RoleType.ADMIN, false),

    // audit log management action group
    AUDIT_LOG_MANAGEMENT(1304, RoleType.ADMIN, false),

    // affinity group CRUD commands
    MANIPULATE_AFFINITY_GROUPS(1550, RoleType.ADMIN, true, ApplicationMode.VirtOnly),

    // disk profiles
    CONFIGURE_STORAGE_DISK_PROFILE(1560, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    CREATE_STORAGE_DISK_PROFILE(1561, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    DELETE_STORAGE_DISK_PROFILE(1562, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    ATTACH_DISK_PROFILE(1563, RoleType.USER, true, ApplicationMode.VirtOnly),

    // MAC pool actions groups
    CREATE_MAC_POOL(1660, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    EDIT_MAC_POOL(1661, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    DELETE_MAC_POOL(1662, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    CONFIGURE_MAC_POOL(1663, RoleType.ADMIN, true, ApplicationMode.VirtOnly),

    // cpu profiles
    DELETE_CPU_PROFILE(1665, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    UPDATE_CPU_PROFILE(1666, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    CREATE_CPU_PROFILE(1667, RoleType.ADMIN, true, ApplicationMode.VirtOnly),
    ASSIGN_CPU_PROFILE(1668, RoleType.ADMIN, true, ApplicationMode.VirtOnly);

    private int id;
    private RoleType roleType;
    private boolean allowsViewingChildren;
    private int applicationMode;
    private static final Map<Integer, ActionGroup> map = new HashMap<>(ActionGroup.values().length);

    static {
        for (ActionGroup t : ActionGroup.values()) {
            map.put(t.id, t);
        }
    }

    private ActionGroup(int value, RoleType type, boolean allowsViewingChildren) {
        this(value, type, allowsViewingChildren, ApplicationMode.AllModes);
    }

    private ActionGroup(int value,
                        RoleType type,
                        boolean allowsViewingChildren,
                        ApplicationMode applicationMode) {
        id = value;
        roleType = type;
        this.allowsViewingChildren = allowsViewingChildren;
        this.applicationMode = applicationMode.getValue();
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

    public int getAvailableInModes() {
        return applicationMode;
    }
}
