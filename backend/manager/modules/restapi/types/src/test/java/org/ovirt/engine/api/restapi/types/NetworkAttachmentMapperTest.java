package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.IpAddressAssignment;
import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;

public class NetworkAttachmentMapperTest extends AbstractInvertibleMappingTest<org.ovirt.engine.api.model.NetworkAttachment, NetworkAttachment, NetworkAttachment> {

    private static final String ADDRESS = "address";
    private static final String GATEWAY = "gateway";
    private static final int PREFIX = 77;
    private static final String PREFIX_STR = String.valueOf(PREFIX);

    public NetworkAttachmentMapperTest() {
        super(org.ovirt.engine.api.model.NetworkAttachment.class, NetworkAttachment.class, NetworkAttachment.class);
    }

    @Test
    public void testMapFromEntityIpv6Assignment() {
        final IpV6Address entity = new IpV6Address();
        entity.setBootProtocol(Ipv6BootProtocol.AUTOCONF);
        entity.setAddress(ADDRESS);
        entity.setGateway(GATEWAY);
        entity.setPrefix(PREFIX);

        final IpAddressAssignment actual = NetworkAttachmentMapper.mapIpv6AddressAssignment(entity);

        assertEquals(BootProtocol.AUTOCONF, actual.getAssignmentMethod());
        assertEquals(ADDRESS, actual.getIp().getAddress());
        assertEquals(GATEWAY, actual.getIp().getGateway());
        assertEquals(PREFIX_STR, actual.getIp().getNetmask());
    }

    @Test
    public void testMapFromModelIpv6Assignment() {

        final IpAddressAssignment model = new IpAddressAssignment();
        model.setAssignmentMethod(BootProtocol.STATIC);
        final Ip ip = new Ip();
        ip.setAddress(ADDRESS);
        ip.setGateway(GATEWAY);
        ip.setNetmask(PREFIX_STR);
        model.setIp(ip);

        final IpV6Address actual = NetworkAttachmentMapper.mapIpv6AddressAssignment(model);

        assertEquals(Ipv6BootProtocol.STATIC_IP, actual.getBootProtocol());
        assertEquals(ADDRESS, actual.getAddress());
        assertEquals(GATEWAY, actual.getGateway());
        assertEquals(Integer.valueOf(PREFIX), actual.getPrefix());
    }

    @Override
    protected void verify(org.ovirt.engine.api.model.NetworkAttachment model,
            org.ovirt.engine.api.model.NetworkAttachment transform) {
        assertNotNull(transform);
        assertEquals(model.getId(), transform.getId());
        assertNotNull(transform.getNetwork());
        assertEquals(model.getNetwork().getId(), transform.getNetwork().getId());
        assertNotNull(transform.getHostNic());
        assertEquals(model.getHostNic().getId(), transform.getHostNic().getId());
        assertNotNull(transform.getProperties());
        assertEquals(CustomPropertiesParser.toMap(model.getProperties()),
            CustomPropertiesParser.toMap(transform.getProperties()));

        assertNotNull(transform.getIpAddressAssignments());
        List<IpAddressAssignment> transformAddresses = transform.getIpAddressAssignments().getIpAddressAssignments();
        assertNotNull(transformAddresses);

        List<IpAddressAssignment> modelAddresses = model.getIpAddressAssignments().getIpAddressAssignments();
        assertEquals(modelAddresses.size(), transformAddresses.size());

        for (int i = 0; i < modelAddresses.size(); i++) {
            assertEquals(modelAddresses.get(i).getAssignmentMethod(), transformAddresses.get(i).getAssignmentMethod());
            Ip modelAddress = modelAddresses.get(i).getIp();
            Ip transformAddress = transformAddresses.get(i).getIp();
            assertEquals(modelAddress.getAddress(), transformAddress.getAddress());
            assertEquals(modelAddress.getNetmask(), transformAddress.getNetmask());
            assertEquals(modelAddress.getGateway(), transformAddress.getGateway());
        }
    }

    @Override
    protected org.ovirt.engine.api.model.NetworkAttachment postPopulate(org.ovirt.engine.api.model.NetworkAttachment model) {
        model.getIpAddressAssignments().getIpAddressAssignments().get(0).setAssignmentMethod(BootProtocol.DHCP);
        model.getQos().setType(QosType.HOSTNETWORK);

        for (IpAddressAssignment ipAddressAssignment : model.getIpAddressAssignments().getIpAddressAssignments()) {
            if (IpVersion.V6 == ipAddressAssignment.getIp().getVersion()) {
                ipAddressAssignment.getIp().setNetmask(String.valueOf(new Random().nextInt(128)));
            }
        }

        return super.postPopulate(model);
    }
}
