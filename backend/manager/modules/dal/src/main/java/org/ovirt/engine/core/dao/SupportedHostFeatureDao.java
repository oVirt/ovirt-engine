package org.ovirt.engine.core.dao;

import java.util.Set;

import org.ovirt.engine.core.compat.Guid;


public interface SupportedHostFeatureDao extends DAO {

    /**
     * Add the given feature to the supported_host_features table.
     *
     * @param hostId
     * @param feature
     */
    void addSupportedHostFeature(Guid hostId, String feature);

    /**
     * Add all the given features to the supported_host_features table.
     *
     * @param hostId
     * @param features
     */
    void addAllSupportedHostFeature(Guid hostId, Set<String> features);

    /**
     * Remove all the given features from the supported_host_features table.
     *
     * @param hostId
     * @param features
     */
    void removeAllSupportedHostFeature(Guid hostId, Set<String> features);

    /**
     * Returns the list of features supported by the host
     *
     * @param hostId
     * @return
     */
    Set<String> getSupportedHostFeaturesByHostId(Guid hostId);
}
