package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.IpAddressAssignment;
import org.ovirt.engine.api.model.IpAddressAssignments;
import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;

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

        if (model.isSetNetwork()) {
            Network networkModel = model.getNetwork();
            if (networkModel.isSetId()) {
                entity.setNetworkId(GuidUtils.asGuid(networkModel.getId()));
            }

            if (networkModel.isSetName()) {
                entity.setNetworkName(networkModel.getName());
            }
        }

        if (model.isSetHostNic()) {
            HostNic hostNic = model.getHostNic();
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
            entity.getIpConfiguration().setIPv4Addresses(new ArrayList<>());
            entity.getIpConfiguration().setIpV6Addresses(new ArrayList<>());

            for (IpAddressAssignment ipAddressAssignment : ipAddressAssignments.getIpAddressAssignments()) {
                if (IpVersion.V6 == getIpVersion(ipAddressAssignment)) {
                    entity.getIpConfiguration().getIpV6Addresses().add(mapIpv6AddressAssignment(ipAddressAssignment));
                } else {
                    entity.getIpConfiguration().getIPv4Addresses().add(mapIpv4AddressAssignment(ipAddressAssignment));
                }
            }
        }

        if (model.isSetDnsResolverConfiguration()) {
            entity.setDnsResolverConfiguration(
                    DnsResolverConfigurationMapper.map(entity.getDnsResolverConfiguration(),
                            model.getDnsResolverConfiguration()));
        }

        if (model.isSetQos()) {
            HostNetworkQos hostNetworkQos = (HostNetworkQos) QosMapper.map(model.getQos(), null);
            entity.setHostNetworkQos(AnonymousHostNetworkQos.fromHostNetworkQos(hostNetworkQos));
        }

        return entity;
    }

    private static IpVersion getIpVersion(IpAddressAssignment ipAddressAssignment) {
        return IpHelper.getVersion(ipAddressAssignment.getIp());
    }

    private static IPv4Address mapIpv4AddressAssignment(IpAddressAssignment ipAddressAssignment) {
        IPv4Address iPv4Address = new IPv4Address();

        if (ipAddressAssignment.isSetAssignmentMethod()) {
            Ipv4BootProtocol assignmentMethod =
                    Ipv4BootProtocolMapper.map(ipAddressAssignment.getAssignmentMethod());
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

    static IpV6Address mapIpv6AddressAssignment(IpAddressAssignment ipAddressAssignment) {
        IpV6Address ipV6Address = new IpV6Address();

        if (ipAddressAssignment.isSetAssignmentMethod()) {
            Ipv6BootProtocol assignmentMethod = Ipv6BootProtocolMapper.map(ipAddressAssignment.getAssignmentMethod());
            ipV6Address.setBootProtocol(assignmentMethod);
        }

        if (ipAddressAssignment.isSetIp()) {
            if (ipAddressAssignment.getIp().isSetAddress()) {
                ipV6Address.setAddress(ipAddressAssignment.getIp().getAddress());
            }
            if (ipAddressAssignment.getIp().isSetGateway()) {
                ipV6Address.setGateway(ipAddressAssignment.getIp().getGateway());
            }
            if (ipAddressAssignment.getIp().isSetNetmask()) {
                final String netmask = ipAddressAssignment.getIp().getNetmask();
                final Integer prefix;
                try {
                    prefix = Integer.valueOf(netmask);
                } catch (NumberFormatException e) {
                    final String message =
                            String.format("IPv6 prefix has to be integer number. '%s' is not a valid value", netmask);
                    throw new WebApplicationException(
                            message,
                            e,
                            Response.status(Status.BAD_REQUEST).entity(fault("Invalid value", message)).build());
                }
                ipV6Address.setPrefix(prefix);
            }
        }
        return ipV6Address;
    }

    private static Fault fault(String reason, String detail) {
        Fault fault = new Fault();
        fault.setReason(reason);
        fault.setDetail(detail);
        return fault;
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
        if (entityIpConfiguration != null) {
            model.setIpAddressAssignments(new IpAddressAssignments());
            if (!entityIpConfiguration.getIPv4Addresses().isEmpty()) {

                entityIpConfiguration.getIPv4Addresses()
                        .stream()
                        .map(NetworkAttachmentMapper::mapIpv4Address)
                        .forEach(model.getIpAddressAssignments().getIpAddressAssignments()::add);
            }

            if (!entityIpConfiguration.getIpV6Addresses().isEmpty()) {
                entityIpConfiguration.getIpV6Addresses()
                        .stream()
                        .map(NetworkAttachmentMapper::mapIpv6AddressAssignment)
                        .forEach(model.getIpAddressAssignments().getIpAddressAssignments()::add);
            }
        }

        if (entity.getDnsResolverConfiguration() != null) {
            model.setDnsResolverConfiguration(
                    DnsResolverConfigurationMapper.map(entity.getDnsResolverConfiguration()));
        }

        if (entity.getReportedConfigurations() != null) {
            model.setInSync(entity.getReportedConfigurations().isNetworkInSync());
            model.setReportedConfigurations(ReportedConfigurationsMapper.map(entity.getReportedConfigurations(), null));
        }

        AnonymousHostNetworkQos hostNetworkQos = entity.getHostNetworkQos();
        if (hostNetworkQos != null) {
            model.setQos(QosMapper.map(HostNetworkQos.fromAnonymousHostNetworkQos(hostNetworkQos), null));
        }

        return model;
    }

    private static IpAddressAssignment mapIpv4Address(IPv4Address iPv4Address) {
        IpAddressAssignment ipAddressAssignment = new IpAddressAssignment();
        Ip ip = new Ip();
        ip.setVersion(IpVersion.V4);
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
        BootProtocol assignmentMethod = Ipv4BootProtocolMapper.map(iPv4Address.getBootProtocol());
        ipAddressAssignment.setAssignmentMethod(assignmentMethod == null ? null : assignmentMethod);
        return ipAddressAssignment;
    }

    static IpAddressAssignment mapIpv6AddressAssignment(IpV6Address ipV6Address) {
        IpAddressAssignment ipAddressAssignment = new IpAddressAssignment();
        Ip ip = new Ip();
        ip.setVersion(IpVersion.V6);
        if (ipV6Address.getAddress() != null) {
            ip.setAddress(ipV6Address.getAddress());
        }

        if (ipV6Address.getGateway() != null) {
            ip.setGateway(ipV6Address.getGateway());
        }

        if (ipV6Address.getPrefix() != null) {
            ip.setNetmask(ipV6Address.getPrefix().toString());
        }

        ipAddressAssignment.setIp(ip);
        BootProtocol assignmentMethod = Ipv6BootProtocolMapper.map(ipV6Address.getBootProtocol());
        ipAddressAssignment.setAssignmentMethod(assignmentMethod);
        return ipAddressAssignment;
    }

    private static HostNic getModelHostNic(NetworkAttachment model) {
        HostNic hostNic = model.getHostNic();
        if (hostNic == null) {
            hostNic = new HostNic();
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
