package org.ovirt.engine.core.dao.network;

import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface DnsResolverConfigurationDao extends GenericDao<DnsResolverConfiguration, Guid> {
    void removeByNetworkAttachmentId(Guid id);

    void removeByNetworkId(Guid id);

    void removeByVdsDynamicId(Guid id);
}
