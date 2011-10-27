package org.ovirt.engine.core.dao;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkDAOHibernateImpl</code> provides an implementation of {@Link NetworkDAO} using Hibernate.
 *
 */
public class NetworkDAOHibernateImpl extends BaseDAOHibernateImpl<network, Guid> implements NetworkDAO {
    public NetworkDAOHibernateImpl() {
        super(network.class);
    }

    @Override
    public List<network> getAllForDataCenter(Guid id) {
        return findByCriteria(Restrictions.eq("storage_pool_id", id));
    }

    @Override
    public List<network> getAllForCluster(Guid id) {
        return findByCriteria(Restrictions.eq("cluster.clusterId", id));
    }
}
