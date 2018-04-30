package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostNicNetworkAttachmentsResourceTest
        extends AbstractBackendNetworkAttachmentsResourceTest<BackendHostNicNetworkAttachmentsResource> {

    public BackendHostNicNetworkAttachmentsResourceTest() {
        super(new BackendHostNicNetworkAttachmentsResource(HOST_NIC_ID, HOST_ID),
                QueryType.GetNetworkAttachmentsByHostNicId);
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
