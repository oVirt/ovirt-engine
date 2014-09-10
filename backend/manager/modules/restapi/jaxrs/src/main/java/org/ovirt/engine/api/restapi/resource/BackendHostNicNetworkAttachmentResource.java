package org.ovirt.engine.api.restapi.resource;

public class BackendHostNicNetworkAttachmentResource
        extends AbstractBackendNetworkAttachmentResource<BackendHostNicNetworkAttachmentsResource> {

    protected BackendHostNicNetworkAttachmentResource(String id, BackendHostNicNetworkAttachmentsResource parent) {
        super(id, parent);
    }
}
