package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.compat.Guid;

public class RoleMapper {

    @Mapping(from = roles.class, to = Role.class)
    public static Role map(roles entity, Role template) {
        Role model = template != null ? template : new Role();
        model.setName(entity.getname());
        model.setDescription(entity.getdescription());
        model.setId(entity.getId().toString());
        model.setMutable(!entity.getis_readonly());
        model.setAdministrative(RoleType.ADMIN.equals(entity.getType()));
        return model;
    }

    @Mapping(from = Role.class, to = roles.class)
    public static roles map(Role model, roles template) {
        roles entity = template != null ? template : new roles();
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setname(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setdescription(model.getDescription());
        }
        if (model.isSetMutable()) {
            entity.setis_readonly(!model.isMutable());
        }
        if (model.isSetAdministrative()) {
            entity.setType(model.isAdministrative() ? RoleType.ADMIN : RoleType.USER);
        }
        return entity;
    }
}
