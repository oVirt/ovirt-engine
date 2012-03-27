package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.VdcObjectType;

@XmlType(name = "ActionGroup")
public enum ActionGroup {
    // vm actions groups
    CREATE_VM(1, RoleType.USER, VdcObjectType.VM),
    DELETE_VM(2, RoleType.USER, VdcObjectType.VM),
    EDIT_VM_PROPERTIES(3, RoleType.USER, VdcObjectType.VM),
    VM_BASIC_OPERATIONS(4, RoleType.USER, VdcObjectType.VM),
    CHANGE_VM_CD(5, RoleType.USER, VdcObjectType.VM),
    MIGRATE_VM(6, RoleType.USER, VdcObjectType.VM),
    CONNECT_TO_VM(7, RoleType.USER, VdcObjectType.VM),
    IMPORT_EXPORT_VM(8, RoleType.ADMIN, VdcObjectType.VM),
    CONFIGURE_VM_NETWORK(9, RoleType.USER, VdcObjectType.VM),
    CONFIGURE_VM_STORAGE(10, RoleType.USER, VdcObjectType.VM),
    MOVE_VM(11, RoleType.USER, VdcObjectType.VM),
    MANIPULATE_VM_SNAPSHOTS(12, RoleType.USER, VdcObjectType.VM),
    // host (vds) actions groups
    CREATE_HOST(100, RoleType.ADMIN, VdcObjectType.VDS),
    EDIT_HOST_CONFIGURATION(101, RoleType.ADMIN, VdcObjectType.VDS),
    DELETE_HOST(102, RoleType.ADMIN, VdcObjectType.VDS),
    MANIPUTLATE_HOST(103, RoleType.ADMIN, VdcObjectType.VDS),
    CONFIGURE_HOST_NETWORK(104, RoleType.ADMIN, VdcObjectType.VDS),
    // templates actions groups
    CREATE_TEMPLATE(200, RoleType.USER, VdcObjectType.VmTemplate),
    EDIT_TEMPLATE_PROPERTIES(201, RoleType.USER, VdcObjectType.VmTemplate),
    DELETE_TEMPLATE(202, RoleType.USER, VdcObjectType.VmTemplate),
    COPY_TEMPLATE(203, RoleType.USER, VdcObjectType.VmTemplate),
    CONFIGURE_TEMPLATE_NETWORK(204, RoleType.USER, VdcObjectType.VmTemplate),
    // vm pools actions groups
    CREATE_VM_POOL(300, RoleType.USER, VdcObjectType.VmPool),
    EDIT_VM_POOL_CONFIGURATION(301, RoleType.USER, VdcObjectType.VmPool),
    DELETE_VM_POOL(302, RoleType.USER, VdcObjectType.VmPool),
    VM_POOL_BASIC_OPERATIONS(303, RoleType.USER, VdcObjectType.VmPool),
    // clusters actions groups
    CREATE_CLUSTER(400, RoleType.ADMIN, VdcObjectType.VdsGroups),
    EDIT_CLUSTER_CONFIGURATION(401, RoleType.ADMIN, VdcObjectType.VdsGroups),
    DELETE_CLUSTER(402, RoleType.ADMIN, VdcObjectType.VdsGroups),
    CONFIGURE_CLUSTER_NETWORK(403, RoleType.ADMIN, VdcObjectType.VdsGroups),
    // users and MLA actions groups
    MANIPULATE_USERS(500, RoleType.ADMIN, VdcObjectType.User),
    MANIPULATE_ROLES(501, RoleType.ADMIN, VdcObjectType.User),
    MANIPULATE_PERMISSIONS(502, RoleType.USER, VdcObjectType.User),
    // storage domains actions groups
    CREATE_STORAGE_DOMAIN(600, RoleType.ADMIN, VdcObjectType.Storage),
    EDIT_STORAGE_DOMAIN_CONFIGURATION(601, RoleType.ADMIN, VdcObjectType.Storage),
    DELETE_STORAGE_DOMAIN(602, RoleType.ADMIN, VdcObjectType.Storage),
    MANIPULATE_STORAGE_DOMAIN(603, RoleType.ADMIN, VdcObjectType.Storage),
    // storage pool actions groups
    CREATE_STORAGE_POOL(700, RoleType.ADMIN, VdcObjectType.StoragePool),
    DELETE_STORAGE_POOL(701, RoleType.ADMIN, VdcObjectType.StoragePool),
    EDIT_STORAGE_POOL_CONFIGURATION(702, RoleType.ADMIN, VdcObjectType.StoragePool),
    CONFIGURE_STORAGE_POOL_NETWORK(703, RoleType.ADMIN, VdcObjectType.StoragePool),

    // engine generic
    CONFIGURE_ENGINE(800, RoleType.ADMIN, VdcObjectType.System),

    // Quota
    CONFIGURE_QUOTA(900, RoleType.ADMIN, VdcObjectType.Quota),
    CONSUME_QUOTA(901, RoleType.USER, VdcObjectType.Quota),

    // Gluster
    CREATE_GLUSTER_VOLUME(1000, RoleType.ADMIN, VdcObjectType.GlusterVolume),
    MANIPULATE_GLUSTER_VOLUME(1001, RoleType.ADMIN, VdcObjectType.GlusterVolume),;

    private int id;
    private RoleType roleType;
    private VdcObjectType vdcObjectType;
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

    private ActionGroup(int value, RoleType type, VdcObjectType objectType) {
        id = value;
        roleType = type;
        vdcObjectType = objectType;

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
