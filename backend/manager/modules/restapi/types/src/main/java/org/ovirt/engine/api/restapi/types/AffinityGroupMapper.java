package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.api.model.AffinityRule;
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

        // These two fields are maintained to keep the backwards compatibility
        // with version 4 of the API and will be removed in future.
        model.setPositive(entity.isVmAffinityEnabled() ? entity.getVmPolarityBooleanObject() : null);
        model.setEnforcing(entity.isVmEnforcing());

        AffinityRule hostsRule = model.getHostsRule();
        if (hostsRule == null) {
            hostsRule = new AffinityRule();
            model.setHostsRule(hostsRule);
        }
        hostsRule.setEnabled(true);
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

        AffinityRule hostsRule = model.getHostsRule();
        if (hostsRule != null) {
            if (hostsRule.isSetEnforcing()) {
                entity.setVdsEnforcing(hostsRule.isEnforcing());
            }

            if (hostsRule.isSetPositive()) {
                entity.setVdsAffinityRule(hostsRule.isPositive()
                        ? EntityAffinityRule.POSITIVE : EntityAffinityRule.NEGATIVE);
            }
        }

        AffinityRule vmsRule = model.getVmsRule();
        if (vmsRule != null) {
            if (vmsRule.isSetEnabled()) {
                if (!vmsRule.isEnabled()) {
                    entity.setVmAffinityRule(EntityAffinityRule.DISABLED);
                } else if (vmsRule.isSetPositive()) {
                    entity.setVmAffinityRule(vmsRule.isPositive()
                            ? EntityAffinityRule.POSITIVE : EntityAffinityRule.NEGATIVE);
                }
            }
            if (vmsRule.isSetEnforcing()) {
                entity.setVmEnforcing(vmsRule.isEnforcing());
            }
        } else {
            if (model.isSetPositive()) {
                entity.setVmAffinityRule(model.isPositive()
                        ? EntityAffinityRule.POSITIVE : EntityAffinityRule.NEGATIVE);
            } else {
                entity.setVmAffinityRule(EntityAffinityRule.DISABLED);
            }

            if (model.isSetEnforcing()) {
                entity.setVmEnforcing(model.isEnforcing());
            }
        }

        if (model.isSetHosts()) {
            List<Guid> hostIds = entity.getVdsIds();
            if (hostIds == null) {
                hostIds = new ArrayList<>();
                entity.setVdsIds(hostIds);
            }

            // Replace the existing list with the provided one
            hostIds.clear();
            model.getHosts().getHosts().stream()
                    .filter(Host::isSetId)
                    .map(Host::getId)
                    .map(Guid::createGuidFromString)
                    .forEach(hostIds::add);
        }

        if (model.isSetVms()) {
            List<Guid> vmIds = entity.getVmIds();
            if (vmIds == null) {
                vmIds = new ArrayList<>();
                entity.setVmIds(vmIds);
            }

            // Replace the existing list with the provided one
            vmIds.clear();
            model.getVms().getVms().stream()
                    .filter(Vm::isSetId)
                    .map(Vm::getId)
                    .map(Guid::createGuidFromString)
                    .forEach(vmIds::add);
        }

        return entity;
    }
}
