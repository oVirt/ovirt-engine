package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.SyncStorageDomainsLunsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class SyncStorageDomainsLunsCommand<T extends SyncStorageDomainsLunsParameters>
        extends AbstractSyncStorageDomainsLunsCommand<T> {

    @Inject
    private StorageDomainDao storageDomainDao;

    private Collection<StorageDomain> storageDomainsToSync;

    public SyncStorageDomainsLunsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        super.init();
        storageDomainsToSync = getParameters().getStorageDomainsToSync()
                .stream()
                .map(storageDomainDao::get)
                .collect(Collectors.toList());
    }

    @Override
    protected boolean validate() {
        return validateVds() &&
                validateStorageTypeOfStorageDomainsToSync();
    }

    protected boolean validateStorageTypeOfStorageDomainsToSync() {
        Collection<StorageDomain> nonBlockDomains = getStorageDomainsToSync()
                .filter(storageDomain -> !storageDomain.getStorageType().isBlockDomain())
                .collect(Collectors.toList());
        if (!nonBlockDomains.isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_SYNC_LUNS_OF_NON_BLOCK_DOMAINS,
                    String.format("$storageDomainsIds %s", nonBlockDomains.stream()
                            .map(StorageDomain::getId)
                            .map(Guid::toString)
                            .collect(Collectors.joining(", "))));
        }
        return true;
    }

    @Override
    protected Stream<StorageDomain> getStorageDomainsToSync() {
        return storageDomainsToSync.stream();
    }
}
