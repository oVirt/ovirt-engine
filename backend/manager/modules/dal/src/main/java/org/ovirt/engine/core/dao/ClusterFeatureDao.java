package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Guid;


public interface ClusterFeatureDao extends DAO {

    /**
     * Add the given feature as a supported for the cluster
     *
     * @param feature
     */
    public void addSupportedClusterFeature(SupportedAdditionalClusterFeature feature);

    /**
     * Update the supported cluster feature.
     *
     * @param feature
     */
    public void updateSupportedClusterFeature(SupportedAdditionalClusterFeature feature);

    /**
     * Add all the supported features in batch
     *
     * @param features
     */
    public void addAllSupportedClusterFeature(Collection<SupportedAdditionalClusterFeature> features);

    /**
     * get all the features supported by the cluster
     *
     * @param clusterId
     * @return
     */
    public Set<SupportedAdditionalClusterFeature> getSupportedFeaturesByClusterId(Guid clusterId);

    /**
     * get all the features for the given version
     *
     * @param version
     * @return
     */
    public Set<AdditionalFeature> getClusterFeaturesForVersionAndCategory(String version, ApplicationMode category);

}
