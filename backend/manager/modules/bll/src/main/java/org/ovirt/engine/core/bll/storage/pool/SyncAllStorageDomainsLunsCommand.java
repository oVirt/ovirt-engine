package org.ovirt.engine.core.bll.storage.pool;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.SyncLunsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.dao.StorageDomainDao;

/**
 * This command is responsible for synchronizing all the active block storage domains in the storage pool.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SyncAllStorageDomainsLunsCommand<T extends SyncLunsParameters>
        extends AbstractSyncStorageDomainsLunsCommand<T> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public SyncAllStorageDomainsLunsCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected Stream<StorageDomain> getStorageDomainsToSync() {
        return storageDomainDao.getAllForStoragePool(getParameters().getStoragePoolId())
                .stream()
                .filter(storageDomain -> storageDomain.getStatus() == StorageDomainStatus.Active)
                .filter(storageDomain -> storageDomain.getStorageType().isBlockDomain());
    }
}
