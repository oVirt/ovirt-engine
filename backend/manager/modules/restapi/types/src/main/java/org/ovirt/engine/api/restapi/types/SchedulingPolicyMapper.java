package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.api.model.Balance;
import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.model.SchedulingPolicyUnit;
import org.ovirt.engine.api.model.Weight;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class SchedulingPolicyMapper {

    @Mapping(from = ClusterPolicy.class, to = SchedulingPolicy.class)
    public static SchedulingPolicy map(ClusterPolicy entity,
            SchedulingPolicy template) {
        SchedulingPolicy model = template != null ? template : new SchedulingPolicy();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());

        model.setLocked(entity.isLocked());
        model.setDefaultPolicy(entity.isDefaultPolicy());
        if (entity.getParameterMap() != null && !entity.getParameterMap().isEmpty()) {
            model.setProperties(CustomPropertiesParser.fromMap(entity.getParameterMap()));
        }
        return model;
    }

    @Mapping(from = SchedulingPolicy.class, to = ClusterPolicy.class)
    public static ClusterPolicy map(SchedulingPolicy model,
            ClusterPolicy template) {
        ClusterPolicy entity =
                template != null ? template : new ClusterPolicy();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetLocked()) {
            entity.setLocked(model.isLocked());
        }
        if (model.isSetDefaultPolicy()) {
            entity.setDefaultPolicy(model.isDefaultPolicy());
        }
        if (model.isSetProperties()) {
            entity.setParameterMap(CustomPropertiesParser.toMap(model.getProperties()));
        }
        return entity;
    }

    @Mapping(from = ClusterPolicy.class, to = Filter.class)
    public static Filter map(ClusterPolicy entity,
            Filter template) {
        if (template == null) {
            assert false : "scheduling filter cannot be null";
            return null;
        }
        Filter model = template;
        SchedulingPolicyUnit schedulingPolicyUnit = new SchedulingPolicyUnit();
        schedulingPolicyUnit.setId(model.getId());
        model.setSchedulingPolicyUnit(schedulingPolicyUnit);
        Integer position = null;
        if (entity.getFilterPositionMap() != null) {
            position = entity.getFilterPositionMap().get(GuidUtils.asGuid(model.getId()));
        }
        model.setPosition(position != null ? position : 0);
        return model;
    }

    @Mapping(from = Filter.class, to = ClusterPolicy.class)
    public static ClusterPolicy map(Filter model,
            ClusterPolicy template) {
        ClusterPolicy entity =
                template != null ? template : new ClusterPolicy();
        if(model.isSetSchedulingPolicyUnit() && model.getSchedulingPolicyUnit().isSetId()){
            Guid guid = GuidUtils.asGuid(model.getSchedulingPolicyUnit().getId());
            if (entity.getFilters() == null) {
                entity.setFilters(new ArrayList<>());
                entity.setFilterPositionMap(new HashMap<>());
            }
            entity.getFilters().add(guid);
            entity.getFilterPositionMap().put(guid, model.isSetPosition() ? model.getPosition() : 0);
        }

        return entity;
    }

    @Mapping(from = ClusterPolicy.class, to = Weight.class)
    public static Weight map(ClusterPolicy entity,
            Weight template) {
        if (template == null) {
            assert false : "scheduling weight cannot be null";
            return null;
        }
        Weight model = template;
        SchedulingPolicyUnit schedulingPolicyUnit = new SchedulingPolicyUnit();
        schedulingPolicyUnit.setId(model.getId());
        model.setSchedulingPolicyUnit(schedulingPolicyUnit);
        model.setFactor(model.getFactor());
        return model;
    }

    @Mapping(from = Weight.class, to = ClusterPolicy.class)
    public static ClusterPolicy map(Weight model,
            ClusterPolicy template) {
        ClusterPolicy entity =
                template != null ? template : new ClusterPolicy();
        if (model.isSetSchedulingPolicyUnit() && model.getSchedulingPolicyUnit().isSetId()) {
            Guid guid = GuidUtils.asGuid(model.getSchedulingPolicyUnit().getId());
            if (entity.getFunctions() == null) {
                entity.setFunctions(new ArrayList<>());
            }
            entity.getFunctions().add(new Pair<>(guid, model.isSetFactor() ? model.getFactor() : 1));
        }

        return entity;
    }

    @Mapping(from = ClusterPolicy.class, to = Balance.class)
    public static Balance map(ClusterPolicy entity,
            Balance template) {
        if (template == null) {
            assert false : "scheduling balance cannot be null";
            return null;
        }
        Balance model = template;
        SchedulingPolicyUnit schedulingPolicyUnit = new SchedulingPolicyUnit();
        schedulingPolicyUnit.setId(model.getId());
        model.setSchedulingPolicyUnit(schedulingPolicyUnit);
        return model;
    }

    @Mapping(from = Balance.class, to = ClusterPolicy.class)
    public static ClusterPolicy map(Balance model,
            ClusterPolicy template) {
        ClusterPolicy entity =
                template != null ? template : new ClusterPolicy();
        if (model.isSetSchedulingPolicyUnit() && model.getSchedulingPolicyUnit().isSetId()) {
            Guid guid = GuidUtils.asGuid(model.getSchedulingPolicyUnit().getId());
            entity.setBalance(guid);
        }

        return entity;
    }
}
