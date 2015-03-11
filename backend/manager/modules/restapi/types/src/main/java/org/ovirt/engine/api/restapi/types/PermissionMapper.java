package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.Permissions;
import org.ovirt.engine.core.compat.Guid;

public class PermissionMapper {

    @Mapping(from = Permission.class, to = Permissions.class)
    public static Permissions map(Permission model, Permissions template) {
        Permissions entity = template != null ? template : new Permissions();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetRole() && model.getRole().isSetId()) {
            entity.setRoleId(GuidUtils.asGuid(model.getRole().getId()));
        }
        entity.setObjectId(map(model, template != null ? template.getObjectId() : null));
        entity.setObjectType(map(model, template != null ? template.getObjectType() : null));
        return entity;
    }

    @Mapping(from = Permissions.class, to = Role.class)
    public static Role map(Permissions entity, Role template) {
        Role model = template != null ? template : new Role();
        model.setName(entity.getRoleName());
        model.setId(entity.getRoleId().toString());
        return model;
    }

    @Mapping(from = Permissions.class, to = Permission.class)
    public static Permission map(Permissions entity, Permission template) {
        Permission model = template != null ? template : new Permission();
        model.setId(entity.getId().toString());
        if (entity.getRoleId() != null) {
            model.setRole(new Role());
            model.getRole().setId(entity.getRoleId().toString());
        }
        if (entity.getAdElementId() != null && (template == null || !template.isSetGroup())) {
            model.setUser(new User());
            model.getUser().setId(entity.getAdElementId().toString());
        }
        if (entity.getObjectId() != null) {
            setObjectId(model, entity);
        }
        return model;
    }

    @Mapping(from = Permission.class, to = Guid.class)
    public static Guid map(Permission p, Guid template) {
        return p.isSetDataCenter() && p.getDataCenter().isSetId()
               ? GuidUtils.asGuid(p.getDataCenter().getId())
               : p.isSetCluster() && p.getCluster().isSetId()
                 ? GuidUtils.asGuid(p.getCluster().getId())
                 : p.isSetHost() && p.getHost().isSetId()
                   ? GuidUtils.asGuid(p.getHost().getId())
                   : p.isSetStorageDomain() && p.getStorageDomain().isSetId()
                     ? GuidUtils.asGuid(p.getStorageDomain().getId())
                     : p.isSetVm() && p.getVm().isSetId()
                       ? GuidUtils.asGuid(p.getVm().getId())
                       : p.isSetVmpool() && p.getVmpool().isSetId()
                         ? GuidUtils.asGuid(p.getVmpool().getId())
                         : p.isSetTemplate() && p.getTemplate().isSetId()
                           ? GuidUtils.asGuid(p.getTemplate().getId())
                           : template;
    }

    @Mapping(from = Permission.class, to = VdcObjectType.class)
    public static VdcObjectType map(Permission p, VdcObjectType template) {
        return p.isSetDataCenter() && p.getDataCenter().isSetId()
               ? VdcObjectType.StoragePool
               : p.isSetCluster() && p.getCluster().isSetId()
                 ? VdcObjectType.VdsGroups
                 : p.isSetHost() && p.getHost().isSetId()
                   ? VdcObjectType.VDS
                   : p.isSetStorageDomain() && p.getStorageDomain().isSetId()
                     ? VdcObjectType.Storage
                     : p.isSetVm() && p.getVm().isSetId()
                       ? VdcObjectType.VM
                       : p.isSetVmpool() && p.getVmpool().isSetId()
                         ? VdcObjectType.VmPool
                         : p.isSetTemplate() && p.getTemplate().isSetId()
                           ? VdcObjectType.VmTemplate
                           : template;
    }

    /**
     * @pre completeness of "{entityType}.id" already validated
     */
    private static void setObjectId(Permission model, Permissions entity) {
        String id = entity.getObjectId().toString();
        switch (entity.getObjectType()) {
        case System:
            break;
        case StoragePool :
            model.setDataCenter(new DataCenter());
            model.getDataCenter().setId(id);
            break;
        case VdsGroups :
            model.setCluster(new Cluster());
            model.getCluster().setId(id);
            break;
        case VDS :
            model.setHost(new Host());
            model.getHost().setId(id);
            break;
        case Storage :
            model.setStorageDomain(new StorageDomain());
            model.getStorageDomain().setId(id);
            break;
        case VM :
            model.setVm(new VM());
            model.getVm().setId(id);
            break;
        case VmPool :
            model.setVmpool(new VmPool());
            model.getVmpool().setId(id);
            break;
        case VmTemplate :
            model.setTemplate(new Template());
            model.getTemplate().setId(id);
            break;
        case Disk :
            model.setDisk(new Disk());
            model.getDisk().setId(id);
            break;
        default:
            assert false;
        }
    }
}
