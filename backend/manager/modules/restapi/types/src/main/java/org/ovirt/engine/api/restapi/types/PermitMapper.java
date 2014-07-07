package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.PermitType;
import org.ovirt.engine.api.restapi.utils.MappingException;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class PermitMapper {

    private static final Log log = LogFactory.getLog(PermitMapper.class);

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
        try {
            return PermitType.valueOf(entity);
        } catch (IllegalArgumentException e) {
            log.error(new MappingException("Missing mapping "+entity+" --> "+PermitType.class.getName(), e));
            return null;
        }
    }

    @Mapping(from = PermitType.class, to = ActionGroup.class)
    public static ActionGroup map(PermitType entity, ActionGroup template) {
        final ActionGroup result = entity.getActionGroup();

        if (result == null) {
            log.error(new MappingException("Missing mapping " + entity + " --> " + ActionGroup.class.getName()));
        }

        return result;
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
