package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.Dao;

public interface NetworkFilterDao extends Dao {

    List<NetworkFilter> getAllNetworkFilters();

    List<NetworkFilter> getAllSupportedNetworkFiltersByVersion(Version version);

    NetworkFilter getNetworkFilterById(Guid id);

    NetworkFilter getNetworkFilterByName(String networkFilterName);

}
