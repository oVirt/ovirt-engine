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
import org.ovirt.engine.core.common.businessentities.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;

public class NetworkMapper {

    @Mapping(from = Network.class, to = org.ovirt.engine.core.common.businessentities.Network.class)
    public static org.ovirt.engine.core.common.businessentities.Network map(Network model, org.ovirt.engine.core.common.businessentities.Network template) {
        org.ovirt.engine.core.common.businessentities.Network entity = template != null ? template : new org.ovirt.engine.core.common.businessentities.Network();
        entity.setCluster(template != null && template.getCluster() != null ? template.getCluster() : new NetworkCluster());
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setname(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setdescription(model.getDescription());
        }
        if (model.isSetDataCenter() && model.getDataCenter().isSetId()) {
            entity.setstorage_pool_id(new Guid(model.getDataCenter().getId()));
        }
        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
                entity.setaddr(model.getIp().getAddress());
            }
            if (model.getIp().isSetNetmask()) {
                entity.setsubnet(model.getIp().getNetmask());
            }
            if (model.getIp().isSetGateway()) {
                entity.setgateway(model.getIp().getGateway());
            }
        }
        if (model.isSetVlan() && model.getVlan().getId()!=null) {
            try {
                entity.setvlan_id(model.getVlan().getId());
            } catch (NumberFormatException e) {
                // REVIST: handle parse error
            }
        }
        if (model.isSetStp()) {
            entity.setstp(model.isStp());
        }
        if (model.isSetUsages()) {
            List<NetworkUsage> networkUsages = new ArrayList<NetworkUsage>();
            for (String usage : model.getUsages().getUsages()) {
                networkUsages.add(NetworkUsage.fromValue(usage));
            }
            entity.getCluster().setis_display(networkUsages.contains(NetworkUsage.DISPLAY));
            entity.setVmNetwork(networkUsages.contains(NetworkUsage.VM));
        }
        if (model.isSetMtu()) {
            entity.setMtu(model.getMtu());
        }
        if (model.isSetDisplay()) { // for backward compatibility use display tag or usage tag
            entity.getCluster().setis_display(model.isDisplay());
        }
        if (model.isSetRequired()) {
            entity.getCluster().setRequired(model.isRequired());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.Network.class, to = Network.class)
    public static Network map(org.ovirt.engine.core.common.businessentities.Network entity, Network template) {
        Network model = template != null ? template : new Network();
        model.setId(entity.getId().toString());
        model.setName(entity.getname());
        model.setDescription(entity.getdescription());
        if (entity.getstorage_pool_id() != null) {
            DataCenter dataCenter = new DataCenter();
            dataCenter.setId(entity.getstorage_pool_id().toString());
            model.setDataCenter(dataCenter);
        }
        if (entity.getaddr() != null ||
            entity.getsubnet() != null ||
            entity.getgateway() != null) {
            model.setIp(new IP());
            model.getIp().setAddress(entity.getaddr());
            model.getIp().setNetmask(entity.getsubnet());
            model.getIp().setGateway(entity.getgateway());
        }
        if (entity.getvlan_id() != null) {
            model.setVlan(new VLAN());
            model.getVlan().setId(entity.getvlan_id());
        }
        model.setStp(entity.getstp());
        model.setMtu(entity.getMtu());

        model.setUsages(new Usages());
        if (entity.isVmNetwork()) {
            model.getUsages().getUsages().add(NetworkUsage.VM.value());
        }
        if (entity.getCluster() != null && entity.getCluster().getis_display()) {
            model.getUsages().getUsages().add(NetworkUsage.DISPLAY.value());
        }
        if (entity.getCluster() != null) {
            if (entity.getCluster().getstatus() != null) {
                model.setStatus(StatusUtils.create(map(entity.getCluster().getstatus(), null)));
            }
            model.setDisplay(entity.getCluster().getis_display());
            model.setRequired(entity.getCluster().isRequired());
        }
        return model;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.NetworkStatus.class, to = NetworkStatus.class)
    public static NetworkStatus map(org.ovirt.engine.core.common.businessentities.NetworkStatus entityStatus,
                                    NetworkStatus template) {
        switch (entityStatus) {
        case NonOperational:
            return NetworkStatus.NON_OPERATIONAL;
        case Operational:
            return NetworkStatus.OPERATIONAL;
        default:
            return null;
        }
    }

    @Mapping(from = NetworkStatus.class, to = org.ovirt.engine.core.common.businessentities.NetworkStatus.class)
    public static org.ovirt.engine.core.common.businessentities.NetworkStatus map(NetworkStatus modelStatus,
                                                                             org.ovirt.engine.core.common.businessentities.NetworkStatus template) {
        if (modelStatus==null) {
            return null;
        } else {
            switch (modelStatus) {
            case NON_OPERATIONAL:
                return org.ovirt.engine.core.common.businessentities.NetworkStatus.NonOperational;
            case OPERATIONAL:
                return org.ovirt.engine.core.common.businessentities.NetworkStatus.Operational;
            default:
                return null;
            }
        }
    }

}
