package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;


public interface ClusterFeatureDao extends Dao {

    /**
     * Add the given feature as a supported for the cluster
     */
    public void addSupportedClusterFeature(SupportedAdditionalClusterFeature feature);

    /**
     * Update the supported cluster feature.
     */
    public void updateSupportedClusterFeature(SupportedAdditionalClusterFeature feature);

    /**
     * Add all the supported features in batch
     */
    public void addAllSupportedClusterFeature(Collection<SupportedAdditionalClusterFeature> features);

    /**
     * get all the features supported by the cluster
     */
    public Set<SupportedAdditionalClusterFeature> getSupportedFeaturesByClusterId(Guid clusterId);

    /**
     * get all the features for the given version
     */
    public Set<AdditionalFeature> getClusterFeaturesForVersionAndCategory(String version, ApplicationMode category);

}
