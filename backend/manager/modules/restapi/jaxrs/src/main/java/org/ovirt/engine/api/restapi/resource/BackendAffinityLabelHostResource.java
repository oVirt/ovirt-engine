package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.AffinityLabelHostResource;
import org.ovirt.engine.core.common.businessentities.VDS;

public class BackendAffinityLabelHostResource extends AbstractBackendAffinityLabelledEntityResource<Host, VDS>
        implements AffinityLabelHostResource {
    public BackendAffinityLabelHostResource(String parentId, String id) {
        super(parentId, VDS::new, id, Host.class, VDS.class);
    }
}
