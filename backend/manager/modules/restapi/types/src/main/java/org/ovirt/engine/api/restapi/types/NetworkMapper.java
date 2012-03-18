package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkStatus;
import org.ovirt.engine.api.model.VLAN;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class NetworkMapper {

    @Mapping(from = Network.class, to = network.class)
    public static network map(Network model, network template) {
        network entity = template != null ? template : new network();
        if (model.isSetId()) {
            entity.setId(new Guid(model.getId()));
        }
        if (model.isSetName()) {
            entity.setname(model.getName());
        }
        if (model.isSetDescription()) {
            entity.setdescription(model.getDescription());
        }
        if (model.isSetStatus()) {
            entity.setStatus(map(NetworkStatus.fromValue(model.getStatus().getState()), null));
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
        if (model.isSetDisplay()) {
            entity.setis_display(model.isDisplay());
        }
        if (model.isSetMtu()) {
            entity.setMtu(model.getMtu());
        }
        return entity;
    }

    @Mapping(from = network.class, to = Network.class)
    public static Network map(network entity, Network template) {
        Network model = template != null ? template : new Network();
        model.setId(entity.getId().toString());
        model.setName(entity.getname());
        model.setDescription(entity.getdescription());
        if (entity.getStatus() != null) {
            model.setStatus(StatusUtils.create(map(entity.getStatus(), null)));
        }
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
        model.setDisplay(entity.getis_display());
        model.setMtu(entity.getMtu());
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
