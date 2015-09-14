package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostNicNetworkAttachmentsResourceTest
        extends AbstractBackendNetworkAttachmentsResourceTest<BackendHostNicNetworkAttachmentsResource> {

    public BackendHostNicNetworkAttachmentsResourceTest() {
        super(new BackendHostNicNetworkAttachmentsResource(HOST_NIC_ID, HOST_ID),
                VdcQueryType.GetNetworkAttachmentsByHostNicId);
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
        expect(entity.getNicId()).andReturn(HOST_NIC_ID).anyTimes();
    }
}
