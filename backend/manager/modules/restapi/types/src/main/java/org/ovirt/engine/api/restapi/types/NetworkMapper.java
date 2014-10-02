package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkStatus;
import org.ovirt.engine.api.model.Usages;
import org.ovirt.engine.api.model.VLAN;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

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
            List<NetworkUsage> networkUsages = new ArrayList<NetworkUsage>();
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
            model.setIp(new IP());
            model.getIp().setAddress(entity.getAddr());
            model.getIp().setNetmask(entity.getSubnet());
            model.getIp().setGateway(entity.getGateway());
        }
        if (entity.getVlanId() != null) {
            model.setVlan(new VLAN());
            model.getVlan().setId(entity.getVlanId());
        }
        model.setStp(entity.getStp());
        model.setMtu(entity.getMtu());

        model.setUsages(new Usages());
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
                model.setStatus(StatusUtils.create(map(entity.getCluster().getStatus(), null)));
            }
            model.setDisplay(entity.getCluster().isDisplay());
            model.setRequired(entity.getCluster().isRequired());
        }
        return model;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.network.NetworkStatus.class, to = NetworkStatus.class)
    public static NetworkStatus map(org.ovirt.engine.core.common.businessentities.network.NetworkStatus entityStatus,
                                    NetworkStatus template) {
        switch (entityStatus) {
        case NON_OPERATIONAL:
            return NetworkStatus.NON_OPERATIONAL;
        case OPERATIONAL:
            return NetworkStatus.OPERATIONAL;
        default:
            return null;
        }
    }

    @Mapping(from = NetworkStatus.class, to = org.ovirt.engine.core.common.businessentities.network.NetworkStatus.class)
    public static org.ovirt.engine.core.common.businessentities.network.NetworkStatus map(NetworkStatus modelStatus,
                                                                             org.ovirt.engine.core.common.businessentities.network.NetworkStatus template) {
        if (modelStatus==null) {
            return null;
        } else {
            switch (modelStatus) {
            case NON_OPERATIONAL:
                return org.ovirt.engine.core.common.businessentities.network.NetworkStatus.NON_OPERATIONAL;
            case OPERATIONAL:
                return org.ovirt.engine.core.common.businessentities.network.NetworkStatus.OPERATIONAL;
            default:
                return null;
            }
        }
    }

}
