/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/
package org.ovirt.engine.api.restapi.resource;

import java.util.Set;

import org.ovirt.engine.api.model.ClusterFeature;
import org.ovirt.engine.api.model.ClusterFeatures;
import org.ovirt.engine.api.model.ClusterLevel;
import org.ovirt.engine.api.resource.ClusterFeatureResource;
import org.ovirt.engine.api.resource.ClusterFeaturesResource;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;

public class BackendClusterFeaturesResource extends AbstractBackendCollectionResource<ClusterFeature, org.ovirt.engine.core.common.businessentities.AdditionalFeature>
implements ClusterFeaturesResource {
    private String version;

    public BackendClusterFeaturesResource(String version) {
        super(ClusterFeature.class, org.ovirt.engine.core.common.businessentities.AdditionalFeature.class);
        this.version = version;
    }

    @Override
    public ClusterFeatures list() {
        ClusterFeatures features = new ClusterFeatures();
        Set<AdditionalFeature> addlFeatures = BackendClusterFeatureHelper.getClusterFeatures(this, version);
        addlFeatures.forEach(clusterFeature -> {
            org.ovirt.engine.api.model.ClusterFeature feature =
                    addLinks(map(clusterFeature, null));
            features.getClusterFeatures().add(feature);
        });
        return features;
    }

    @Override
    public ClusterFeatureResource getFeatureResource(String id) {
        return inject(new BackendClusterFeatureResource(version, id));
    }

    @Override
    protected ClusterFeature addParents(ClusterFeature model) {
        ClusterLevel clusterLevel = new ClusterLevel();
        clusterLevel.setId(version);
        model.setClusterLevel(clusterLevel);
        return super.addParents(model);
    }

}
