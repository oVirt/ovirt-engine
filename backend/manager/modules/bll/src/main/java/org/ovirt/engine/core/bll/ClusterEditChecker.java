package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.Cluster;

/**
 * Contract for CDI beans implementing custom checks for
 * {@link org.ovirt.engine.core.common.businessentities.Cluster} edits
 * that may result in multiple non-operational hosts.
 */
interface ClusterEditChecker<T> {

    /**
     * Returns whether this checks is applicable to given cluster edit.
     * Checker may optionally perform some initialization from the {@code oldCluster} and/or {@code newCluster}
     * to be used in subsequent per-entity calls to {@link #check(T)}.
     */
    boolean isApplicable(Cluster oldCluster, Cluster newCluster);

    /**
     * Returns whether given {@code clusterEntity} passes the check.
     * Is guaranteed to be called only after
     * {@link #isApplicable(org.ovirt.engine.core.common.businessentities.Cluster,
     *                      org.ovirt.engine.core.common.businessentities.Cluster)}.
     */
    boolean check(T clusterEntity);

    /**
     * Returns message that describes general nature of this check.
     * Displayed before the per-entity details.
     *
     * @return non-null name of {@link EngineMessage}
     */
    String getMainMessage();

    /**
     * In case {@code entity} fails this check, returns optional detail text for this entity.
     * @return String detailing reasons of failing the check for given entity. {@code null} if no detail is available.
     */
    default String getDetailMessage(T entity) {
        return null;
    }
}
