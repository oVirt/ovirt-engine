package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;

public class PermitMapper {
    /**
     * @pre completeness of "name|id" already validated
     */
    @Mapping(from = Permit.class, to = ActionGroup.class)
    public static ActionGroup map(Permit model, ActionGroup template) {
        assert(model.isSetId() || model.isSetName());
        return template != null
               ? template
               : model.getId() != null
                 ? ActionGroup.forValue(Integer.valueOf(model.getId()))
                 : ActionGroup.valueOf(model.getName().toUpperCase());
    }

    @Mapping(from = String.class, to = ActionGroup.class)
    public static ActionGroup map(String n, ActionGroup template) {
        try {
            return ActionGroup.forValue(Integer.valueOf(n));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @Mapping(from = ActionGroup.class, to = Permit.class)
    public static Permit map(ActionGroup entity, Permit template) {
        PermitType permitType = map(entity, (PermitType)null);
        Permit model = template != null ? template : new Permit();
        model.setId(Integer.toString(entity.getId()));
        model.setName(permitType.value());
        model.setAdministrative(org.ovirt.engine.api.model.RoleType.ADMIN.toString().equals(entity.getRoleType().toString()));
        return model;
    }

    @Mapping(from = ActionGroup.class, to = PermitType.class)
    public static PermitType map(ActionGroup entity, PermitType template) {
        switch (entity) {
        case CREATE_VM:
            return PermitType.CREATE_VM;
        case DELETE_VM:
            return PermitType.DELETE_VM;
        case EDIT_VM_PROPERTIES:
            return PermitType.EDIT_VM_PROPERTIES;
        case VM_BASIC_OPERATIONS:
            return PermitType.VM_BASIC_OPERATIONS;
        case CHANGE_VM_CD:
            return PermitType.CHANGE_VM_CD;
        case MIGRATE_VM:
            return PermitType.MIGRATE_VM;
        case CONNECT_TO_VM:
            return PermitType.CONNECT_TO_VM;
        case IMPORT_EXPORT_VM:
            return PermitType.IMPORT_EXPORT_VM;
        case CONFIGURE_VM_NETWORK:
            return PermitType.CONFIGURE_VM_NETWORK;
        case CONFIGURE_VM_STORAGE:
            return PermitType.CONFIGURE_VM_STORAGE;
        case MOVE_VM:
            return PermitType.MOVE_VM;
        case MANIPULATE_VM_SNAPSHOTS:
            return PermitType.MANIPULATE_VM_SNAPSHOTS;
        case RECONNECT_TO_VM:
            return PermitType.RECONNECT_TO_VM;
        case CHANGE_VM_CUSTOM_PROPERTIES:
            return PermitType.CHANGE_VM_CUSTOM_PROPERTIES;
        case EDIT_ADMIN_VM_PROPERTIES:
            return PermitType.EDIT_ADMIN_VM_PROPERTIES;
        case CREATE_INSTANCE:
            return PermitType.CREATE_INSTANCE;
        case CREATE_HOST:
            return PermitType.CREATE_HOST;
        case EDIT_HOST_CONFIGURATION:
            return PermitType.EDIT_HOST_CONFIGURATION;
        case DELETE_HOST:
            return PermitType.DELETE_HOST;
        case MANIPULATE_HOST:
            return PermitType.MANIPULATE_HOST;
        case CONFIGURE_HOST_NETWORK:
            return PermitType.CONFIGURE_HOST_NETWORK;
        case CREATE_TEMPLATE:
            return PermitType.CREATE_TEMPLATE;
        case EDIT_TEMPLATE_PROPERTIES:
            return PermitType.EDIT_TEMPLATE_PROPERTIES;
        case EDIT_ADMIN_TEMPLATE_PROPERTIES:
            return PermitType.EDIT_ADMIN_TEMPLATE_PROPERTIES;
        case DELETE_TEMPLATE:
            return PermitType.DELETE_TEMPLATE;
        case COPY_TEMPLATE:
            return PermitType.COPY_TEMPLATE;
        case CONFIGURE_TEMPLATE_NETWORK:
            return PermitType.CONFIGURE_TEMPLATE_NETWORK;
        case CREATE_VM_POOL:
            return PermitType.CREATE_VM_POOL;
        case EDIT_VM_POOL_CONFIGURATION:
            return PermitType.EDIT_VM_POOL_CONFIGURATION;
        case DELETE_VM_POOL:
            return PermitType.DELETE_VM_POOL;
        case VM_POOL_BASIC_OPERATIONS:
            return PermitType.VM_POOL_BASIC_OPERATIONS;
        case CREATE_CLUSTER:
            return PermitType.CREATE_CLUSTER;
        case EDIT_CLUSTER_CONFIGURATION:
            return PermitType.EDIT_CLUSTER_CONFIGURATION;
        case DELETE_CLUSTER:
            return PermitType.DELETE_CLUSTER;
        case CONFIGURE_CLUSTER_NETWORK:
            return PermitType.CONFIGURE_CLUSTER_NETWORK;
        case MANIPULATE_USERS:
            return PermitType.MANIPULATE_USERS;
        case MANIPULATE_ROLES:
            return PermitType.MANIPULATE_ROLES;
        case MANIPULATE_PERMISSIONS:
            return PermitType.MANIPULATE_PERMISSIONS;
        case CREATE_STORAGE_DOMAIN:
            return PermitType.CREATE_STORAGE_DOMAIN;
        case EDIT_STORAGE_DOMAIN_CONFIGURATION:
            return PermitType.EDIT_STORAGE_DOMAIN_CONFIGURATION;
        case DELETE_STORAGE_DOMAIN:
            return PermitType.DELETE_STORAGE_DOMAIN;
        case MANIPULATE_STORAGE_DOMAIN:
            return PermitType.MANIPULATE_STORAGE_DOMAIN;
        case CREATE_STORAGE_POOL:
            return PermitType.CREATE_STORAGE_POOL;
        case DELETE_STORAGE_POOL:
            return PermitType.DELETE_STORAGE_POOL;
        case EDIT_STORAGE_POOL_CONFIGURATION:
            return PermitType.EDIT_STORAGE_POOL_CONFIGURATION;
        case CONFIGURE_STORAGE_POOL_NETWORK:
            return PermitType.CONFIGURE_STORAGE_POOL_NETWORK;
        case CREATE_STORAGE_POOL_NETWORK:
            return PermitType.CREATE_STORAGE_POOL_NETWORK;
        case DELETE_STORAGE_POOL_NETWORK:
            return PermitType.DELETE_STORAGE_POOL_NETWORK;
        case ASSIGN_CLUSTER_NETWORK:
            return PermitType.ASSIGN_CLUSTER_NETWORK;
        case CONFIGURE_ENGINE:
            return PermitType.CONFIGURE_RHEVM;
        case CONFIGURE_QUOTA:
            return PermitType.CONFIGURE_QUOTA;
        case CONSUME_QUOTA:
            return PermitType.CONSUME_QUOTA;
        case CREATE_GLUSTER_VOLUME:
            return PermitType.CREATE_GLUSTER_VOLUME;
        case MANIPULATE_GLUSTER_VOLUME:
            return PermitType.MANIPULATE_GLUSTER_VOLUME;
        case DELETE_GLUSTER_VOLUME:
            return PermitType.DELETE_GLUSTER_VOLUME;
        case MANIPULATE_GLUSTER_HOOK:
            return PermitType.MANIPULATE_GLUSTER_HOOK;
        case CREATE_DISK:
            return PermitType.CREATE_DISK;
        case ATTACH_DISK:
            return PermitType.ATTACH_DISK;
        case EDIT_DISK_PROPERTIES:
            return PermitType.EDIT_DISK_PROPERTIES;
        case CONFIGURE_DISK_STORAGE:
            return PermitType.CONFIGURE_DISK_STORAGE;
        case DELETE_DISK:
            return PermitType.DELETE_DISK;
        case CONFIGURE_NETWORK_VNIC_PROFILE:
            return PermitType.CONFIGURE_NETWORK_VNIC_PROFILE;
        case CREATE_NETWORK_VNIC_PROFILE:
            return PermitType.CREATE_NETWORK_VNIC_PROFILE;
        case DELETE_NETWORK_VNIC_PROFILE:
            return PermitType.DELETE_NETWORK_VNIC_PROFILE;
        case LOGIN:
            return PermitType.LOGIN;
        case INJECT_EXTERNAL_EVENTS:
            return PermitType.INJECT_EXTERNAL_EVENTS;
        case MANIPULATE_GLUSTER_SERVICE:
            return PermitType.MANIPULATE_GLUSTER_SERVICE;
        case CONFIGURE_SCSI_GENERIC_IO:
            return PermitType.CONFIGURE_SCSI_GENERIC_IO;
        case INJECT_EXTERNAL_TASKS:
            return PermitType.INJECT_EXTERNAL_TASKS;
        case ACCESS_IMAGE_STORAGE:
            return PermitType.ACCESS_IMAGE_STORAGE;
        case TAG_MANAGEMENT:
            return PermitType.TAG_MANAGEMENT;
        case AUDIT_LOG_MANAGEMENT:
                return PermitType.AUDIT_LOG_MANAGEMENT;
        case BOOKMARK_MANAGEMENT:
            return PermitType.BOOKMARK_MANAGEMENT;
        case EVENT_NOTIFICATION_MANAGEMENT:
            return PermitType.EVENT_NOTIFICATION_MANAGEMENT;
        case MANIPULATE_AFFINITY_GROUPS:
            return PermitType.MANIPULATE_AFFINITY_GROUPS;
        case ADD_USERS_AND_GROUPS_FROM_DIRECTORY:
            return PermitType.ADD_USERS_AND_GROUPS_FROM_DIRECTORY;
        case CREATE_STORAGE_DISK_PROFILE:
            return PermitType.CREATE_STORAGE_DISK_PROFILE;
        case CONFIGURE_STORAGE_DISK_PROFILE:
            return PermitType.CONFIGURE_STORAGE_DISK_PROFILE;
        case DELETE_STORAGE_DISK_PROFILE:
            return PermitType.DELETE_STORAGE_DISK_PROFILE;
        default:
            return null;
        }
    }

    @Mapping(from = PermitType.class, to = ActionGroup.class)
    public static ActionGroup map(PermitType entity, ActionGroup template) {
        switch (entity) {
        case CREATE_VM:
            return ActionGroup.CREATE_VM;
        case DELETE_VM:
            return ActionGroup.DELETE_VM;
        case EDIT_VM_PROPERTIES:
            return ActionGroup.EDIT_VM_PROPERTIES;
        case VM_BASIC_OPERATIONS:
            return ActionGroup.VM_BASIC_OPERATIONS;
        case CHANGE_VM_CD:
            return ActionGroup.CHANGE_VM_CD;
        case MIGRATE_VM:
            return ActionGroup.MIGRATE_VM;
        case CONNECT_TO_VM:
            return ActionGroup.CONNECT_TO_VM;
        case IMPORT_EXPORT_VM:
            return ActionGroup.IMPORT_EXPORT_VM;
        case CONFIGURE_VM_NETWORK:
            return ActionGroup.CONFIGURE_VM_NETWORK;
        case CONFIGURE_VM_STORAGE:
            return ActionGroup.CONFIGURE_VM_STORAGE;
        case MOVE_VM:
            return ActionGroup.MOVE_VM;
        case MANIPULATE_VM_SNAPSHOTS:
            return ActionGroup.MANIPULATE_VM_SNAPSHOTS;
        case RECONNECT_TO_VM:
            return ActionGroup.RECONNECT_TO_VM;
        case CHANGE_VM_CUSTOM_PROPERTIES:
            return ActionGroup.CHANGE_VM_CUSTOM_PROPERTIES;
        case EDIT_ADMIN_VM_PROPERTIES:
            return ActionGroup.EDIT_ADMIN_VM_PROPERTIES;
        case CREATE_INSTANCE:
            return ActionGroup.CREATE_INSTANCE;
        case CREATE_HOST:
            return ActionGroup.CREATE_HOST;
        case EDIT_HOST_CONFIGURATION:
            return ActionGroup.EDIT_HOST_CONFIGURATION;
        case DELETE_HOST:
            return ActionGroup.DELETE_HOST;
        case MANIPULATE_HOST:
            return ActionGroup.MANIPULATE_HOST;
        case CONFIGURE_HOST_NETWORK:
            return ActionGroup.CONFIGURE_HOST_NETWORK;
        case CREATE_TEMPLATE:
            return ActionGroup.CREATE_TEMPLATE;
        case EDIT_TEMPLATE_PROPERTIES:
            return ActionGroup.EDIT_TEMPLATE_PROPERTIES;
        case EDIT_ADMIN_TEMPLATE_PROPERTIES:
            return ActionGroup.EDIT_ADMIN_TEMPLATE_PROPERTIES;
        case DELETE_TEMPLATE:
            return ActionGroup.DELETE_TEMPLATE;
        case COPY_TEMPLATE:
            return ActionGroup.COPY_TEMPLATE;
        case CONFIGURE_TEMPLATE_NETWORK:
            return ActionGroup.CONFIGURE_TEMPLATE_NETWORK;
        case CREATE_VM_POOL:
            return ActionGroup.CREATE_VM_POOL;
        case EDIT_VM_POOL_CONFIGURATION:
            return ActionGroup.EDIT_VM_POOL_CONFIGURATION;
        case DELETE_VM_POOL:
            return ActionGroup.DELETE_VM_POOL;
        case VM_POOL_BASIC_OPERATIONS:
            return ActionGroup.VM_POOL_BASIC_OPERATIONS;
        case CREATE_CLUSTER:
            return ActionGroup.CREATE_CLUSTER;
        case EDIT_CLUSTER_CONFIGURATION:
            return ActionGroup.EDIT_CLUSTER_CONFIGURATION;
        case DELETE_CLUSTER:
            return ActionGroup.DELETE_CLUSTER;
        case CONFIGURE_CLUSTER_NETWORK:
            return ActionGroup.CONFIGURE_CLUSTER_NETWORK;
        case MANIPULATE_USERS:
            return ActionGroup.MANIPULATE_USERS;
        case MANIPULATE_ROLES:
            return ActionGroup.MANIPULATE_ROLES;
        case MANIPULATE_PERMISSIONS:
            return ActionGroup.MANIPULATE_PERMISSIONS;
        case CREATE_STORAGE_DOMAIN:
            return ActionGroup.CREATE_STORAGE_DOMAIN;
        case EDIT_STORAGE_DOMAIN_CONFIGURATION:
            return ActionGroup.EDIT_STORAGE_DOMAIN_CONFIGURATION;
        case DELETE_STORAGE_DOMAIN:
            return ActionGroup.DELETE_STORAGE_DOMAIN;
        case MANIPULATE_STORAGE_DOMAIN:
            return ActionGroup.MANIPULATE_STORAGE_DOMAIN;
        case CREATE_STORAGE_POOL:
            return ActionGroup.CREATE_STORAGE_POOL;
        case DELETE_STORAGE_POOL:
            return ActionGroup.DELETE_STORAGE_POOL;
        case EDIT_STORAGE_POOL_CONFIGURATION:
            return ActionGroup.EDIT_STORAGE_POOL_CONFIGURATION;
        case CONFIGURE_STORAGE_POOL_NETWORK:
            return ActionGroup.CONFIGURE_STORAGE_POOL_NETWORK;
        case CONFIGURE_RHEVM:
            return ActionGroup.CONFIGURE_ENGINE;
        case CONFIGURE_QUOTA:
            return ActionGroup.CONFIGURE_QUOTA;
        case CONSUME_QUOTA:
            return ActionGroup.CONSUME_QUOTA;
        case CREATE_GLUSTER_VOLUME:
            return ActionGroup.CREATE_GLUSTER_VOLUME;
        case MANIPULATE_GLUSTER_VOLUME:
            return ActionGroup.MANIPULATE_GLUSTER_VOLUME;
        case DELETE_GLUSTER_VOLUME:
            return ActionGroup.DELETE_GLUSTER_VOLUME;
        case CREATE_DISK:
            return ActionGroup.CREATE_DISK;
        case ATTACH_DISK:
            return ActionGroup.ATTACH_DISK;
        case EDIT_DISK_PROPERTIES:
            return ActionGroup.EDIT_DISK_PROPERTIES;
        case CONFIGURE_DISK_STORAGE:
            return ActionGroup.CONFIGURE_DISK_STORAGE;
        case DELETE_DISK:
            return ActionGroup.DELETE_DISK;
        case CONFIGURE_NETWORK_VNIC_PROFILE:
            return ActionGroup.CONFIGURE_NETWORK_VNIC_PROFILE;
        case CREATE_NETWORK_VNIC_PROFILE:
            return ActionGroup.CREATE_NETWORK_VNIC_PROFILE;
        case DELETE_NETWORK_VNIC_PROFILE:
            return ActionGroup.DELETE_NETWORK_VNIC_PROFILE;
        case LOGIN:
            return ActionGroup.LOGIN;
        case INJECT_EXTERNAL_EVENTS:
            return ActionGroup.INJECT_EXTERNAL_EVENTS;
        case CREATE_STORAGE_POOL_NETWORK:
            return ActionGroup.CREATE_STORAGE_POOL_NETWORK;
        case DELETE_STORAGE_POOL_NETWORK:
            return ActionGroup.DELETE_STORAGE_POOL_NETWORK;
        case ASSIGN_CLUSTER_NETWORK:
            return ActionGroup.ASSIGN_CLUSTER_NETWORK;
        case CONFIGURE_SCSI_GENERIC_IO:
            return ActionGroup.CONFIGURE_SCSI_GENERIC_IO;
        case INJECT_EXTERNAL_TASKS:
            return ActionGroup.INJECT_EXTERNAL_TASKS;
        case ACCESS_IMAGE_STORAGE:
            return ActionGroup.ACCESS_IMAGE_STORAGE;
        case TAG_MANAGEMENT:
            return ActionGroup.TAG_MANAGEMENT;
        case AUDIT_LOG_MANAGEMENT:
            return ActionGroup.AUDIT_LOG_MANAGEMENT;
        case BOOKMARK_MANAGEMENT:
            return ActionGroup.BOOKMARK_MANAGEMENT;
        case EVENT_NOTIFICATION_MANAGEMENT:
            return ActionGroup.EVENT_NOTIFICATION_MANAGEMENT;
        case MANIPULATE_AFFINITY_GROUPS:
            return ActionGroup.MANIPULATE_AFFINITY_GROUPS;
        case ADD_USERS_AND_GROUPS_FROM_DIRECTORY:
            return ActionGroup.ADD_USERS_AND_GROUPS_FROM_DIRECTORY;
        case CREATE_STORAGE_DISK_PROFILE:
            return ActionGroup.CREATE_STORAGE_DISK_PROFILE;
        case CONFIGURE_STORAGE_DISK_PROFILE:
            return ActionGroup.CONFIGURE_STORAGE_DISK_PROFILE;
        case DELETE_STORAGE_DISK_PROFILE:
            return ActionGroup.DELETE_STORAGE_DISK_PROFILE;
        default:
            return null;
        }
    }

    @Mapping(from = PermitType.class, to = Permit.class)
    public static Permit map(PermitType entity, Permit template) {
        ActionGroup actionGroup = map(entity, (ActionGroup) null);
        if (actionGroup == null) {
            return null;
        }
        return map(actionGroup, (Permit) null);
    }
}
