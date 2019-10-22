/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ClusterFeature;
import org.ovirt.engine.api.resource.ClusterFeatureResource;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;

public class BackendClusterFeatureResource extends AbstractBackendSubResource<ClusterFeature, AdditionalFeature> implements ClusterFeatureResource {
    private String version;

    public BackendClusterFeatureResource(String version, String id) {
        super(id, ClusterFeature.class, AdditionalFeature.class);
        this.version = version;
    }

    @Override
    public ClusterFeature get() {
        AdditionalFeature feature = BackendClusterFeatureHelper.getClusterFeature(this, version, guid);
        if (feature == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return addLinks(map(feature, null));
    }
}
