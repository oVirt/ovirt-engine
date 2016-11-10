package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;

public class AffinityGroupMapper {

    @Mapping(from = org.ovirt.engine.core.common.scheduling.AffinityGroup.class, to = AffinityGroup.class)
    public static AffinityGroup map(org.ovirt.engine.core.common.scheduling.AffinityGroup entity,
            AffinityGroup template) {
        AffinityGroup model = template != null ? template : new AffinityGroup();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setPositive(entity.getVmPolarityBooleanObject());
        model.setEnforcing(entity.isVmEnforcing());
        Cluster cluster = new Cluster();
        cluster.setId(entity.getClusterId().toString());
        model.setCluster(cluster);

        return model;
    }

    @Mapping(from = AffinityGroup.class, to = org.ovirt.engine.core.common.scheduling.AffinityGroup.class)
    public static org.ovirt.engine.core.common.scheduling.AffinityGroup map(AffinityGroup model,
            org.ovirt.engine.core.common.scheduling.AffinityGroup template) {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                template != null ? template : new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetCluster() && model.getCluster().isSetId()) {
            entity.setClusterId(GuidUtils.asGuid(model.getCluster().getId()));
        }
        if (model.isSetPositive()) {
            if (model.isPositive()) {
                entity.setVmAffinityRule(EntityAffinityRule.POSITIVE);
            } else {
                entity.setVmAffinityRule(EntityAffinityRule.NEGATIVE);
            }
        }
        if (model.isSetEnforcing()) {
            entity.setVmEnforcing(model.isEnforcing());
        }

        return entity;
    }
}
