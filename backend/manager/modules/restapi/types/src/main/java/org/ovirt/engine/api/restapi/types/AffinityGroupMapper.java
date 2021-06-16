package org.ovirt.engine.api.restapi.types;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.model.AffinityLabels;
import org.ovirt.engine.api.model.AffinityRule;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;

public class AffinityGroupMapper {

    @Mapping(from = org.ovirt.engine.core.common.scheduling.AffinityGroup.class, to = AffinityGroup.class)
    public static AffinityGroup map(org.ovirt.engine.core.common.scheduling.AffinityGroup entity,
            AffinityGroup template) {
        AffinityGroup model = template != null ? template : new AffinityGroup();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setBroken(entity.getBroken());

        // These two fields are maintained to keep the backwards compatibility
        // with version 4 of the API and will be removed in future.
        model.setPositive(entity.isVmAffinityEnabled() ? entity.getVmPolarityBooleanObject() : null);
        model.setEnforcing(entity.isVmEnforcing());

        AffinityRule hostsRule = model.getHostsRule();
        if (hostsRule == null) {
            hostsRule = new AffinityRule();
            model.setHostsRule(hostsRule);
        }
        hostsRule.setEnabled(entity.isVdsAffinityEnabled());
        hostsRule.setEnforcing(entity.isVdsEnforcing());
        hostsRule.setPositive(entity.isVdsPositive());

        AffinityRule vmsRule = model.getVmsRule();
        if (vmsRule == null) {
            vmsRule = new AffinityRule();
            model.setVmsRule(vmsRule);
        }
        vmsRule.setEnabled(entity.isVmAffinityEnabled());
        vmsRule.setEnforcing(entity.isVmEnforcing());
        vmsRule.setPositive(entity.isVmPositive());

        Cluster cluster = new Cluster();
        cluster.setId(entity.getClusterId().toString());
        model.setCluster(cluster);

        BigDecimal priority = new BigDecimal(entity.getPriority());
        BigDecimal precision = new BigDecimal(org.ovirt.engine.core.common.scheduling.AffinityGroup.PRIORITY_PRECISION);
        model.setPriority(priority.divide(precision));

        Hosts hosts = model.getHosts();
        if (hosts == null) {
            hosts = new Hosts();
            model.setHosts(hosts);
        }

        entity.getVdsIds().stream().map(id -> {
            Host host = new Host();
            host.setId(id.toString());
            return host;
        }).forEach(model.getHosts().getHosts()::add);

        Vms vms = model.getVms();
        if (vms == null) {
            vms = new Vms();
            model.setVms(vms);
        }

        entity.getVmIds().stream().map(id -> {
            Vm vm = new Vm();
            vm.setId(id.toString());
            return vm;
        }).forEach(model.getVms().getVms()::add);

        if (model.getVmLabels() == null) {
            model.setVmLabels(new AffinityLabels());
        }

        entity.getVmLabels().stream().map(id -> {
            AffinityLabel label = new AffinityLabel();
            label.setId(id.toString());
            return label;
        }).forEach(model.getVmLabels().getAffinityLabels()::add);

        if (model.getHostLabels() == null) {
            model.setHostLabels(new AffinityLabels());
        }

        entity.getHostLabels().stream().map(id -> {
            AffinityLabel label = new AffinityLabel();
            label.setId(id.toString());
            return label;
        }).forEach(model.getHostLabels().getAffinityLabels()::add);

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

        if (model.isSetPriority()) {
            entity.setPriorityFromDouble(model.getPriority().doubleValue());
        }

        AffinityRule hostsRule = model.getHostsRule();
        if (hostsRule != null) {
            if (hostsRule.isSetEnabled() && !hostsRule.isEnabled()) {
                entity.setVdsAffinityRule(EntityAffinityRule.DISABLED);
            } else if (hostsRule.isSetPositive()) {
                entity.setVdsAffinityRule(hostsRule.isPositive()
                        ? EntityAffinityRule.POSITIVE : EntityAffinityRule.NEGATIVE);
            }

            if (hostsRule.isSetEnforcing()) {
                entity.setVdsEnforcing(hostsRule.isEnforcing());
            }
        }

        AffinityRule vmsRule = model.getVmsRule();
        if (vmsRule != null) {
            if (vmsRule.isSetEnabled() && !vmsRule.isEnabled()) {
                entity.setVmAffinityRule(EntityAffinityRule.DISABLED);
            } else if (vmsRule.isSetPositive()) {
                entity.setVmAffinityRule(vmsRule.isPositive()
                        ? EntityAffinityRule.POSITIVE : EntityAffinityRule.NEGATIVE);
            }

            if (vmsRule.isSetEnforcing()) {
                entity.setVmEnforcing(vmsRule.isEnforcing());
            }
        } else {
            if (model.isSetPositive()) {
                entity.setVmAffinityRule(model.isPositive()
                        ? EntityAffinityRule.POSITIVE : EntityAffinityRule.NEGATIVE);
                //Default to DISABLED for new entities,
                //but do not touch existing values when no change is requested
            } else if (entity.getVmAffinityRule() == null) {
                entity.setVmAffinityRule(EntityAffinityRule.DISABLED);
            }

            if (model.isSetEnforcing()) {
                entity.setVmEnforcing(model.isEnforcing());
            }
        }

        if (model.isSetHosts()) {
            entity.setVdsIds(extractIds(model.getHosts().getHosts()));
        }

        if (model.isSetVms()) {
            entity.setVmIds(extractIds(model.getVms().getVms()));
        }

        if (model.isSetHostLabels()) {
            entity.setHostLabels(extractIds(model.getHostLabels().getAffinityLabels()));
        }

        if (model.isSetVmLabels()) {
            entity.setVmLabels(extractIds(model.getVmLabels().getAffinityLabels()));
        }

        return entity;
    }

    private static List<Guid> extractIds(List<? extends BaseResource> resources) {
        return resources.stream()
                .filter(BaseResource::isSetId)
                .map(BaseResource::getId)
                .map(Guid::createGuidFromString)
                .collect(Collectors.toList());
    }
}
