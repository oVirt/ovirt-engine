/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ClusterFeature;
import org.ovirt.engine.api.resource.ClusterEnabledFeatureResource;
import org.ovirt.engine.api.restapi.types.ClusterFeaturesMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterEnabledFeatureResource extends AbstractBackendSubResource<ClusterFeature, SupportedAdditionalClusterFeature> implements ClusterEnabledFeatureResource {
    private Guid clusterId;

    public BackendClusterEnabledFeatureResource(Guid clusterId, String id) {
        super(id, ClusterFeature.class, SupportedAdditionalClusterFeature.class);
        this.clusterId = clusterId;
    }

    @Override
    public ClusterFeature get() {
        SupportedAdditionalClusterFeature feature =
                BackendClusterFeatureHelper.getEnabledFeature(this, clusterId, guid);
        if (feature != null) {
            return addLinks(ClusterFeaturesMapper.map(feature.getFeature(), null));
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @Override
    public Response remove() {
        Cluster cluster = BackendClusterFeatureHelper.getClusterWithFeatureDisabled(this, clusterId, guid);
        ClusterOperationParameters param = new ClusterOperationParameters(cluster);
        return performAction(ActionType.UpdateCluster, param);
    }

}
