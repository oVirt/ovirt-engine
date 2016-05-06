package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkStatus;
import org.ovirt.engine.api.model.Qos;
import org.ovirt.engine.api.model.Vlan;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;

public class NetworkMapper {

    @Mapping(from = Network.class, to = org.ovirt.engine.core.common.businessentities.network.Network.class)
    public static org.ovirt.engine.core.common.businessentities.network.Network map(Network model, org.ovirt.engine.core.common.businessentities.network.Network template) {
        org.ovirt.engine.core.common.businessentities.network.Network entity = template != null ? template : new org.ovirt.engine.core.common.businessentities.network.Network();
        entity.setCluster(template != null && template.getCluster() != null ? template.getCluster() : new NetworkCluster());
        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }
        if (model.isSetComment()) {
            entity.setComment(model.getComment());
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetId()) {
            entity.setDataCenterId(GuidUtils.asGuid(model.getDataCenter().getId()));
        }
        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
                entity.setAddr(model.getIp().getAddress());
            }
            if (model.getIp().isSetNetmask()) {
                entity.setSubnet(model.getIp().getNetmask());
            }
            if (model.getIp().isSetGateway()) {
                entity.setGateway(model.getIp().getGateway());
            }
        }
        if (model.isSetVlan()) {
            entity.setVlanId(model.getVlan().getId());
        }
        if (model.isSetStp()) {
            entity.setStp(model.isStp());
        }
        if (model.isSetDisplay()) { // for backward compatibility use display tag or usage tag
            entity.getCluster().setDisplay(model.isDisplay());
        }
        if (model.isSetUsages()) {
            List<NetworkUsage> networkUsages = new ArrayList<>();
            for (String usage : model.getUsages().getUsages()) {
                networkUsages.add(NetworkUsage.fromValue(usage));
            }
            entity.getCluster().setDisplay(networkUsages.contains(NetworkUsage.DISPLAY));
            entity.getCluster().setMigration(networkUsages.contains(NetworkUsage.MIGRATION));
            entity.getCluster().setManagement(networkUsages.contains(NetworkUsage.MANAGEMENT));
            entity.setVmNetwork(networkUsages.contains(NetworkUsage.VM));
        }
        if (model.isSetMtu()) {
            entity.setMtu(model.getMtu());
        }
        if (model.isSetRequired()) {
            entity.getCluster().setRequired(model.isRequired());
        }

        if (model.isSetQos()) {
            entity.setQosId(Guid.createGuidFromString(model.getQos().getId()));
        }

        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.network.Network.class, to = Network.class)
    public static Network map(org.ovirt.engine.core.common.businessentities.network.Network entity, Network template) {
        Network model = template != null ? template : new Network();
        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setDescription(entity.getDescription());
        model.setComment(entity.getComment());
        if (entity.getDataCenterId() != null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.getDataCenterId().toString());
            model.setDataCenter(dataCenter);
        }
        if (entity.getAddr() != null ||
            entity.getSubnet() != null ||
            entity.getGateway() != null) {
            model.setIp(new Ip());
            model.getIp().setAddress(entity.getAddr());
            model.getIp().setNetmask(entity.getSubnet());
            model.getIp().setGateway(entity.getGateway());
        }
        if (entity.getVlanId() != null) {
            model.setVlan(new Vlan());
            model.getVlan().setId(entity.getVlanId());
        }
        model.setStp(entity.getStp());
        model.setMtu(entity.getMtu());

        model.setUsages(new Network.UsagesList());
        if (entity.isVmNetwork()) {
            model.getUsages().getUsages().add(NetworkUsage.VM.value());
        }
        if (entity.getCluster() != null) {
            if (entity.getCluster().isDisplay()) {
                model.getUsages().getUsages().add(NetworkUsage.DISPLAY.value());
            }
            if (entity.getCluster().isMigration()) {
                model.getUsages().getUsages().add(NetworkUsage.MIGRATION.value());
            }
            if (entity.getCluster().isManagement()) {
                model.getUsages().getUsages().add(NetworkUsage.MANAGEMENT.value());
            }
            if (entity.getCluster().getStatus() != null) {
                model.setStatus(mapNetworkStatus(entity.getCluster().getStatus()));
            }
            model.setDisplay(entity.getCluster().isDisplay());
            model.setRequired(entity.getCluster().isRequired());
        }

        Guid entityQosId = entity.getQosId();
        if (entityQosId != null) {
            Qos qos = new Qos();
            qos.setId(entityQosId.toString());
            model.setQos(qos);
        }

        return model;
    }

    private static NetworkStatus mapNetworkStatus(org.ovirt.engine.core.common.businessentities.network.NetworkStatus status) {
        switch (status) {
        case NON_OPERATIONAL:
            return NetworkStatus.NON_OPERATIONAL;
        case OPERATIONAL:
            return NetworkStatus.OPERATIONAL;
        default:
            return null;
        }
    }
}
