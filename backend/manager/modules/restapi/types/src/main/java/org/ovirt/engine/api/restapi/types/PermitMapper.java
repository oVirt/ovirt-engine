package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleType;

public class PermitMapper {
    @Mapping(from = Permit.class, to = ActionGroup.class)
    public static ActionGroup map(Permit model, ActionGroup template) {
        ActionGroup entity = template;
        if (model.isSetId()) {
            entity = ActionGroup.forValue(Integer.parseInt(model.getId()));
        }
        if (model.isSetName()) {
            entity = ActionGroup.valueOf(model.getName().toUpperCase());
        }
        return entity;
    }

    @Mapping(from = ActionGroup.class, to = Permit.class)
    public static Permit map(ActionGroup entity, Permit template) {
        Permit model = template != null ? template : new Permit();
        model.setId(Integer.toString(entity.getId()));
        model.setName(entity.name().toLowerCase());
        model.setAdministrative(entity.getRoleType() == RoleType.ADMIN);
        return model;
    }
}
