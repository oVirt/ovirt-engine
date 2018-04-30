package org.ovirt.engine.api.restapi.resource;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostNetworkAttachmentsResourceTest
        extends AbstractBackendNetworkAttachmentsResourceTest<BackendHostNetworkAttachmentsResource> {

    public BackendHostNetworkAttachmentsResourceTest() {
        super(new BackendHostNetworkAttachmentsResource(HOST_ID), QueryType.GetNetworkAttachmentsByHostId);
    }
}
