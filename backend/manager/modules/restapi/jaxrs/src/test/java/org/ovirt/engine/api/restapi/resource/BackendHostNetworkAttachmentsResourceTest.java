package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostNetworkAttachmentsResourceTest
        extends AbstractBackendNetworkAttachmentsResourceTest<BackendHostNetworkAttachmentsResource> {

    public BackendHostNetworkAttachmentsResourceTest() {
        super(new BackendHostNetworkAttachmentsResource(HOST_ID), VdcQueryType.GetNetworkAttachmentsByHostId);
    }
}
