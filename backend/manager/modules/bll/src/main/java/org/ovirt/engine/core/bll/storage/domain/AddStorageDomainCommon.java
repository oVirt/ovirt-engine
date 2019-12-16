package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;

public abstract class AddStorageDomainCommon<T extends StorageDomainManagementParameter> extends AddStorageDomainCommand<T> {

    @Inject
    private StorageServerConnectionDao storageServerConnectionDao;
    @Inject
    private StorageDomainDao storageDomainDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected AddStorageDomainCommon(Guid commandId) {
        super(commandId);
    }

    public AddStorageDomainCommon(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected boolean checkStorageConnection(String storageDomainConnection) {
        List<StorageDomain> domains = null;
        StorageServerConnections connection = storageServerConnectionDao.get(storageDomainConnection);
        if (connection == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }
        if (connection.getStorageType().isFileDomain()) {
            domains = getStorageDomainsByConnId(connection.getId());
            if (domains.size() > 0) {
                String domainNames = domains.stream().map(StorageDomain::getName).collect(Collectors.joining(","));
                return prepareFailureMessageForDomains(domainNames);
            }
        }
        return true;
    }

    protected List<StorageDomain> getStorageDomainsByConnId(String connectionId) {
        return storageDomainDao.getAllByConnectionId(Guid.createGuidFromString(connectionId));
    }

    protected boolean prepareFailureMessageForDomains(String domainNames) {
        addValidationMessageVariable("domainNames", domainNames);
        return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_BELONGS_TO_SEVERAL_STORAGE_DOMAINS);
    }

    @Override
    protected boolean canAddDomain() {
        return checkStorageConnection(getStorageDomain().getStorage());
    }

    @Override
    protected String getStorageArgs() {
        return storageServerConnectionDao
                .get(getStorageDomain().getStorage())
                .getConnection();
    }
}
