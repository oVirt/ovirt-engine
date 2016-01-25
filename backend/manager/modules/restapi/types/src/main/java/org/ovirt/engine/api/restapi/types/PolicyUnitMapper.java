package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;

public class PolicyUnitMapper {

    @Mapping(from = PolicyUnit.class, to = SchedulingPolicyUnit.class)
    public static SchedulingPolicyUnit map(PolicyUnit entity,
            SchedulingPolicyUnit template) {
        SchedulingPolicyUnit model = template != null ? template : new SchedulingPolicyUnit();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setType(map(entity.getPolicyUnitType(), null));
        model.setEnabled(entity.isEnabled());
        model.setInternal(entity.isInternal());
        if (entity.getParameterRegExMap() != null && !entity.getParameterRegExMap().isEmpty()) {
            model.setProperties(CustomPropertiesParser.fromMap(entity.getParameterRegExMap()));
        }

        return model;
    }

    @Mapping(from = SchedulingPolicyUnit.class, to = PolicyUnit.class)
    public static PolicyUnit map(SchedulingPolicyUnit model,
            PolicyUnit template) {
        PolicyUnit entity =
                template != null ? template : new PolicyUnit();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetType()) {
            entity.setPolicyUnitType(map(model.getType(), null));
        }
        if (model.isSetEnabled()) {
            entity.setEnabled(model.isEnabled());
        }
        if (model.isSetInternal()) {
            entity.setInternal(model.isInternal());
        }
        if (model.isSetProperties()) {
            entity.setParameterRegExMap(CustomPropertiesParser.toMap(model.getProperties()));
        }

        return entity;
    }

    @Mapping(from = org.ovirt.engine.api.model.PolicyUnitType.class, to = PolicyUnitType.class)
    public static PolicyUnitType map(org.ovirt.engine.api.model.PolicyUnitType model, PolicyUnitType template) {
        if (model == null) {
            return null;
        }
        switch (model) {
        case FILTER:
            return PolicyUnitType.FILTER;
        case WEIGHT:
            return PolicyUnitType.WEIGHT;
        case LOAD_BALANCING:
            return PolicyUnitType.LOAD_BALANCING;
        default:
            assert false : "unknown Policy Unit Type value: " + model.toString();
            return null;
        }
    }

    @Mapping(from = PolicyUnitType.class, to = org.ovirt.engine.api.model.PolicyUnitType.class)
    public static org.ovirt.engine.api.model.PolicyUnitType map(PolicyUnitType model,
            org.ovirt.engine.api.model.PolicyUnitType template) {
        if (model == null) {
            return null;
        }
        switch (model) {
        case FILTER:
            return org.ovirt.engine.api.model.PolicyUnitType.FILTER;
        case WEIGHT:
            return org.ovirt.engine.api.model.PolicyUnitType.WEIGHT;
        case LOAD_BALANCING:
            return org.ovirt.engine.api.model.PolicyUnitType.LOAD_BALANCING;
        default:
            assert false : "unknown Policy Unit Type value: " + model.toString();
            return null;
        }
    }
}
