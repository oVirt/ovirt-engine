package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.IpAddressAssignment;
import org.ovirt.engine.api.model.IpAddressAssignments;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;

public class NetworkAttachmentMapper {

    @Mapping(from = NetworkAttachment.class,
            to = org.ovirt.engine.core.common.businessentities.network.NetworkAttachment.class)
    public static org.ovirt.engine.core.common.businessentities.network.NetworkAttachment map(NetworkAttachment model,
            org.ovirt.engine.core.common.businessentities.network.NetworkAttachment template) {
        org.ovirt.engine.core.common.businessentities.network.NetworkAttachment entity = template == null ?
                new org.ovirt.engine.core.common.businessentities.network.NetworkAttachment() :
                template;

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }

        if (model.isSetNetwork() && model.getNetwork().isSetId()) {
            entity.setNetworkId(GuidUtils.asGuid(model.getNetwork().getId()));
        }

        if (model.isSetHostNic()) {
            HostNIC hostNic = model.getHostNic();
            if (hostNic.isSetId()) {
                entity.setNicId(GuidUtils.asGuid(hostNic.getId()));
            } else {
                entity.setNicId(null);
            }

            if (hostNic.isSetName()) {
                entity.setNicName(hostNic.getName());
            } else {
                entity.setNicName(null);
            }
        }

        if (model.isSetProperties()) {
            entity.setProperties(CustomPropertiesParser.toMap(model.getProperties()));
        }

        if (model.isSetIpAddressAssignments()) {
            entity.setIpConfiguration(new org.ovirt.engine.core.common.businessentities.network.IpConfiguration());
            IpAddressAssignments ipAddressAssignments = model.getIpAddressAssignments();
            entity.getIpConfiguration().setIPv4Addresses(new ArrayList<IPv4Address>());

            for (IpAddressAssignment ipAddressAssignment : ipAddressAssignments.getIpAddressAssignments()) {
                entity.getIpConfiguration().getIPv4Addresses().add(mapIpAddressAssignment(ipAddressAssignment));
            }
        }

        return entity;
    }

    private static IPv4Address mapIpAddressAssignment(IpAddressAssignment ipAddressAssignment) {
        IPv4Address iPv4Address = new IPv4Address();

        if (ipAddressAssignment.isSetAssignmentMethod()) {
            NetworkBootProtocol assignmentMethod =
                    BootProtocolMapper.map(BootProtocol.fromValue(ipAddressAssignment.getAssignmentMethod()),
                            null);
            iPv4Address.setBootProtocol(assignmentMethod);
        }

        if (ipAddressAssignment.isSetIp()) {
            if (ipAddressAssignment.getIp().isSetAddress()) {
                iPv4Address.setAddress(ipAddressAssignment.getIp().getAddress());
            }
            if (ipAddressAssignment.getIp().isSetGateway()) {
                iPv4Address.setGateway(ipAddressAssignment.getIp().getGateway());
            }
            if (ipAddressAssignment.getIp().isSetNetmask()) {
                iPv4Address.setNetmask(ipAddressAssignment.getIp().getNetmask());
            }
        }
        return iPv4Address;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.network.NetworkAttachment.class,
            to = NetworkAttachment.class)
    public static NetworkAttachment map(org.ovirt.engine.core.common.businessentities.network.NetworkAttachment entity,
            NetworkAttachment template) {
        NetworkAttachment model =
                template == null ? new NetworkAttachment() : template;

        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }

        if (entity.getNetworkId() != null) {
            getModelNetwork(model).setId(entity.getNetworkId().toString());
        }

        if (entity.getNicId() != null) {
            getModelHostNic(model).setId(entity.getNicId().toString());
        }

        if (entity.hasProperties()) {
            model.setProperties(CustomPropertiesParser.fromMap(entity.getProperties()));
        }

        org.ovirt.engine.core.common.businessentities.network.IpConfiguration entityIpConfiguration =
                entity.getIpConfiguration();
        if (entityIpConfiguration != null && !entityIpConfiguration.getIPv4Addresses().isEmpty()) {
            model.setIpAddressAssignments(new IpAddressAssignments());

            for (IPv4Address iPv4Address : entityIpConfiguration.getIPv4Addresses()) {
                model.getIpAddressAssignments().getIpAddressAssignments().add(mapIpAddressAssignment(iPv4Address));
            }
        }

        return model;
    }

    private static IpAddressAssignment mapIpAddressAssignment(IPv4Address iPv4Address) {
        IpAddressAssignment ipAddressAssignment = new IpAddressAssignment();
        IP ip = new IP();
        if (iPv4Address.getAddress() != null) {
            ip.setAddress(iPv4Address.getAddress());
        }

        if (iPv4Address.getGateway() != null) {
            ip.setGateway(iPv4Address.getGateway());
        }

        if (iPv4Address.getNetmask() != null) {
            ip.setNetmask(iPv4Address.getNetmask());
        }

        ipAddressAssignment.setIp(ip);
        BootProtocol assignmentMethod = BootProtocolMapper.map(iPv4Address.getBootProtocol(), null);
        ipAddressAssignment.setAssignmentMethod(assignmentMethod == null ? null : assignmentMethod.value());
        return ipAddressAssignment;
    }

    private static HostNIC getModelHostNic(NetworkAttachment model) {
        HostNIC hostNic = model.getHostNic();
        if (hostNic == null) {
            hostNic = new HostNIC();
            model.setHostNic(hostNic);
        }
        return hostNic;
    }

    private static Network getModelNetwork(NetworkAttachment model) {
        Network network = model.getNetwork();
        if (network == null) {
            network = new Network();
            model.setNetwork(network);
        }

        return network;
    }
}
