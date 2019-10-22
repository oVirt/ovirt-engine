/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.queries.GetClusterFeaturesByVersionAndCategoryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class BackendClusterFeatureHelper {

    public static SupportedAdditionalClusterFeature getEnabledFeature(BackendResource resource,
            Guid clusterId,
            Guid id) {
        Set<SupportedAdditionalClusterFeature> addlFeatures = resource.getOptionalEntity(Set.class,
                QueryType.GetClusterFeaturesByClusterId,
                new IdQueryParameters(clusterId),
                clusterId.toString(),
                false);
        SupportedAdditionalClusterFeature feature = Optional.ofNullable(addlFeatures)
                .orElse(Collections.emptySet())
                .stream()
                .filter(f -> f.getFeature().getId().equals(id) && f.isEnabled())
                .findFirst()
                .orElse(null);
        return feature;
    }

    public static Cluster getClusterEntity(BackendResource resource, Guid clusterId) {
        Cluster cluster = resource.getEntity(Cluster.class,
                QueryType.GetClusterById,
                new IdQueryParameters(clusterId),
                null,
                true);
        return cluster;
    }

    public static Cluster getClusterWithFeatureDisabled(BackendResource resource, Guid clusterId, Guid id) {
        Cluster cluster = getClusterEntity(resource, clusterId);
        SupportedAdditionalClusterFeature feature = cluster.getAddtionalFeaturesSupported()
                .stream()
                .filter(f -> f.getFeature().getId().equals(id))
                .findFirst()
                .orElse(null);
        if (feature != null) {
            feature.setEnabled(false);
        }
        return cluster;
    }

    public static Set<AdditionalFeature> getClusterFeatures(BackendResource resource, String version) {
        Set<AdditionalFeature> addlFeatures = resource.getEntity(Set.class,
                QueryType.GetClusterFeaturesByVersionAndCategory,
                new GetClusterFeaturesByVersionAndCategoryParameters(new Version(version),
                        resource.getCurrent().getApplicationMode()),
                "features for version:" + version);
        return addlFeatures;
    }

    public static AdditionalFeature getClusterFeature(BackendResource resource, String version, Guid id) {
        Set<AdditionalFeature> addlFeatures = getClusterFeatures(resource, version);
        return addlFeatures.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
