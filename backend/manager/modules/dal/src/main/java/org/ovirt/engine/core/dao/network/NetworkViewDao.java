package org.ovirt.engine.core.dao.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.SearchDAO;

/**
 * <code>NetworkViewDao</code> defines a type for performing Search queries on instances of {@link NetworkView}.
 *
 *
 */
public interface NetworkViewDao extends DAO, SearchDAO<NetworkView> {
}
