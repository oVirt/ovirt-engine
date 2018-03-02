package org.ovirt.engine.core.dao.provider;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;

/**
 * {@code HostProviderBindingDao} defines a type for performing operation on Host binding ids
 * A host binding id is the id of the local external network driver agent.
 * It is used to specify a mapping between external network ports and hosts in the external network port
 * binding:host_id paramter, as described in the Openstack Networking API
 */
public interface HostProviderBindingDao extends Dao {

    String get(Guid vdsId, String pluginType);

    void update(Guid vdsId, Map<String, Object> values);
}
