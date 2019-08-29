/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.Set;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.ClusterFeature;
import org.ovirt.engine.api.model.ClusterFeatures;
import org.ovirt.engine.api.resource.ClusterEnabledFeatureResource;
import org.ovirt.engine.api.resource.ClusterEnabledFeaturesResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterEnabledFeaturesResource extends AbstractBackendCollectionResource<ClusterFeature, AdditionalFeature> implements ClusterEnabledFeaturesResource {

    private Guid clusterId;

    public BackendClusterEnabledFeaturesResource(Guid clusterId) {
        super(ClusterFeature.class, AdditionalFeature.class);
        this.clusterId = clusterId;
    }

    @Override
    public ClusterFeatures list() {
        ClusterFeatures features = new ClusterFeatures();
        Set<SupportedAdditionalClusterFeature> addlFeatures = getOptionalEntity(Set.class,
                QueryType.GetClusterFeaturesByClusterId,
                new IdQueryParameters(clusterId),
                clusterId.toString(),
                false);
        if (addlFeatures != null) {
            for (SupportedAdditionalClusterFeature entity : addlFeatures) {
                if (entity.isEnabled()) {
                    features.getClusterFeatures().add(addLinks(map(entity.getFeature(), null)));
                }
            }
        }
        return features;
    }

    @Override
    public Response add(ClusterFeature feature) {
        validateParameters(feature);

        org.ovirt.engine.core.common.businessentities.AdditionalFeature featureEntity =
                map(feature, null);
        org.ovirt.engine.core.common.businessentities.Cluster cluster =
                BackendClusterFeatureHelper.getClusterEntity(this, clusterId);
        SupportedAdditionalClusterFeature supportedFeature = new SupportedAdditionalClusterFeature();
        supportedFeature.setFeature(featureEntity);
        supportedFeature.setEnabled(true);
        supportedFeature.setClusterId(clusterId);
        cluster.getAddtionalFeaturesSupported().add(supportedFeature);

        ClusterOperationParameters param = new ClusterOperationParameters(cluster);
        return performCreate(ActionType.UpdateCluster, param, new ClusterFeatureIdResolver(clusterId, featureEntity.getId()));
    }

    @Override
    public ClusterEnabledFeatureResource getFeatureResource(String id) {
        return inject(new BackendClusterEnabledFeatureResource(clusterId, id));
    }

    private class ClusterFeatureIdResolver extends EntityIdResolver<Guid> {
        private final Guid clusterId;
        private final Guid featureId;

        ClusterFeatureIdResolver(Guid clusterId, Guid featureId) {
            this.clusterId = clusterId;
            this.featureId = featureId;
        }

        @Override
        public AdditionalFeature lookupEntity(Guid id) throws BackendFailureException {
            SupportedAdditionalClusterFeature supportedFeature = BackendClusterFeatureHelper
                    .getEnabledFeature(BackendClusterEnabledFeaturesResource.this, clusterId, featureId);
            if (supportedFeature != null) {
                return supportedFeature.getFeature();
            } else {
                return null;
            }
        }
    }
}
