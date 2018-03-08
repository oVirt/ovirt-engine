package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.compat.Guid;

public interface StorageDomainDynamicDao extends GenericDao<StorageDomainDynamic, Guid>, ExternalStatusAwareDao<Guid, ExternalStatus> {
    void updateConfirmedSize(StorageDomainDynamic domain);
}
