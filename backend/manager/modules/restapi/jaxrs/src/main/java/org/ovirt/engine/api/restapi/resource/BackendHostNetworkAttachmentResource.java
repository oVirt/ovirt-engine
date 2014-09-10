package org.ovirt.engine.api.restapi.resource;


public class BackendHostNetworkAttachmentResource
        extends AbstractBackendNetworkAttachmentResource<BackendHostNetworkAttachmentsResource> {

    protected BackendHostNetworkAttachmentResource(String id, BackendHostNetworkAttachmentsResource parent) {
        super(id, parent);
    }
}
