package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.RoleType;

public class RoleMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Role.class, to = Role.class)
    public static Role map(org.ovirt.engine.core.common.businessentities.Role entity, Role template) {
        Role model = template != null ? template : new Role();
        model.setName(entity.getname());
        model.setDescription(entity.getdescription());
        model.setId(entity.getId().toString());
        model.setMutable(!entity.getis_readonly());
        model.setAdministrative(RoleType.ADMIN.equals(entity.getType()));
        return model;
    }

    @Mapping(from = Role.class, to = org.ovirt.engine.core.common.businessentities.Role.class)
    public static org.ovirt.engine.core.common.businessentities.Role map(Role model, org.ovirt.engine.core.common.businessentities.Role template) {
        org.ovirt.engine.core.common.businessentities.Role entity = template != null ? template : new org.ovirt.engine.core.common.businessentities.Role();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
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
