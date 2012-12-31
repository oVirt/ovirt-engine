package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkClusterDAOHibernateImpl</code> provides an implementation of {@link NetworkClusterDAO} that uses
 * Hibernate for the persistence implementation.
 *
 */
public class NetworkClusterDAOHibernateImpl extends BaseDAOHibernateImpl<NetworkCluster, Guid> implements NetworkClusterDAO {
    public NetworkClusterDAOHibernateImpl() {
        super(NetworkCluster.class);
    }

    @Override
    public NetworkCluster get(NetworkClusterId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<NetworkCluster> getAllForCluster(Guid cluster) {
        return findByCriteria(Restrictions.eq("clusterId", cluster));
    }

    @Override
    public List<NetworkCluster> getAllForNetwork(Guid network) {
        return findByCriteria(Restrictions.eq("networkId", network));
    }

    @Override
    public void remove(final Guid clusterid, final Guid networkid) {
        Session session = getSession();
        Query query = session.getNamedQuery("delete_network_cluster");

        query.setParameter("cluster_id", clusterid);
        query.setParameter("network_id", networkid);

        session.beginTransaction();
        query.executeUpdate();
        session.getTransaction().commit();
    }

    @Override
    public void setNetworkExclusivelyAsDisplay(Guid vdsGroupId, Guid networkId) {
        throw new UnsupportedOperationException();
    }
}
