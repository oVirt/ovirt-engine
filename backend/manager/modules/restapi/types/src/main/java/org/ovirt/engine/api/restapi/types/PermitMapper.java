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
                 : ActionGroup.valueOf(model.getName());
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
        model.setAdministrative(org.ovirt.engine.api.model.RoleType.ADMIN.equals(permitType.getRole()));
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
        case CREATE_HOST:
            return PermitType.CREATE_HOST;
        case EDIT_HOST_CONFIGURATION:
            return PermitType.EDIT_HOST_CONFIGURATION;
        case DELETE_HOST:
            return PermitType.DELETE_HOST;
        case MANIPUTLATE_HOST:
            return PermitType.MANIPUTLATE_HOST;
        case CONFIGURE_HOST_NETWORK:
            return PermitType.CONFIGURE_HOST_NETWORK;
        case CREATE_TEMPLATE:
            return PermitType.CREATE_TEMPLATE;
        case EDIT_TEMPLATE_PROPERTIES:
            return PermitType.EDIT_TEMPLATE_PROPERTIES;
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
        default:
            return null;
        }
    }

    @Mapping(from = PermitType.class, to = Permit.class)
    public static Permit map(PermitType entity, Permit template) {
        Permit model = new Permit();
        model.setName(entity.value());
        model.setId(String.valueOf(entity.getId()));
        model.setAdministrative(entity.getRole()==org.ovirt.engine.api.model.RoleType.ADMIN);
        return model;
    }
}
