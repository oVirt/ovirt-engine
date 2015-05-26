package org.ovirt.engine.api.restapi.types;

import java.util.List;

import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.IpAddressAssignment;
import org.ovirt.engine.api.model.QosType;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;

public class NetworkAttachmentMapperTest extends AbstractInvertibleMappingTest<org.ovirt.engine.api.model.NetworkAttachment, NetworkAttachment, NetworkAttachment> {

    public NetworkAttachmentMapperTest() {
        super(org.ovirt.engine.api.model.NetworkAttachment.class, NetworkAttachment.class, NetworkAttachment.class);
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
            IP modelAddress = modelAddresses.get(i).getIp();
            IP transformAddress = transformAddresses.get(i).getIp();
            assertEquals(modelAddress.getAddress(), transformAddress.getAddress());
            assertEquals(modelAddress.getNetmask(), transformAddress.getNetmask());
            assertEquals(modelAddress.getGateway(), transformAddress.getGateway());
        }
    }

    @Override
    protected org.ovirt.engine.api.model.NetworkAttachment postPopulate(org.ovirt.engine.api.model.NetworkAttachment model) {
        model.getIpAddressAssignments().getIpAddressAssignments().get(0).setAssignmentMethod("dhcp");
        model.getQos().setType(QosType.HOSTNETWORK.name().toLowerCase());
        return super.postPopulate(model);
    }
}
