package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostNicNetworkAttachmentResourceTest
        extends AbstractBackendNetworkAttachmentResourceTest<BackendHostNicNetworkAttachmentsResource, BackendHostNicNetworkAttachmentResource> {

    private static final Guid HOST_NIC_ID = Guid.newGuid();

    public BackendHostNicNetworkAttachmentResourceTest() {
        super(new BackendHostNicNetworkAttachmentResource(GUIDS[0].toString(),
                new BackendHostNicNetworkAttachmentsResource(HOST_NIC_ID, hostId)));
    }

    @Override
    protected void createReourceWithBadGuid() {
        new BackendHostNicNetworkAttachmentResource("foo",
                new BackendHostNicNetworkAttachmentsResource(HOST_NIC_ID, hostId));
    }

    @Override
    protected NetworkAttachment getEntity(int index) {
        return setUpEntityExpectations(mock(NetworkAttachment.class), index);
    }

    @Override
    protected void verifyModel(org.ovirt.engine.api.model.NetworkAttachment model) {
        assertEquals(HOST_NIC_ID.toString(), model.getHostNic().getId());
    }

    @Override
    protected org.ovirt.engine.api.model.NetworkAttachment getModel(int index) {
        org.ovirt.engine.api.model.NetworkAttachment model = super.getModel(index);
        model.setHostNic(new HostNic());
        model.getHostNic().setId(HOST_NIC_ID.toString());
        return model;
    }

    @Override
    protected void setUpEntityExpectations(NetworkAttachment entity) {
        when(entity.getNicId()).thenReturn(HOST_NIC_ID);
    }
}
