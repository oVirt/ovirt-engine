package org.ovirt.engine.core.bll.validator.storage;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.di.Injector;

public class StorageConnectionValidator {
    private static final String STORAGE_DOMAIN_NAME_REPLACEMENT = "$domainNames %1$s";

    private StorageServerConnections connection;

    public StorageConnectionValidator(StorageServerConnections connection) {
        this.connection = connection;
    }

    public ValidationResult isConnectionExists() {
        if (connection == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_NOT_EXIST);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isSameStorageType(StorageDomain storageDomain) {
        StorageType connectionStorageType = connection.getStorageType();
        StorageType storageDomainType = storageDomain.getStorageType();

        if (!connectionStorageType.equals(storageDomainType)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_CONNECTION_UNSUPPORTED_ACTION_NOT_SAME_STORAGE_TYPE);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isISCSIConnectionAndDomain(StorageDomain storageDomain) {
        ValidationResult validationResult = isSameStorageType(storageDomain);
        if (!validationResult.isValid()) {
            return validationResult;
        }

        StorageType connectionStorageType = connection.getStorageType();
        StorageType storageDomainType = storageDomain.getStorageType();

        if (!connectionStorageType.equals(StorageType.ISCSI) || !storageDomainType.equals(StorageType.ISCSI)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_ISCSI_DOMAINS);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isDomainOfConnectionExistsAndInactive(StorageDomain storageDomain) {
        if (storageDomain == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }
        if (storageDomain.getStatus() != StorageDomainStatus.Maintenance
                && storageDomain.getStorageDomainSharedStatus() != StorageDomainSharedStatus.Unattached) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_UNSUPPORTED_ACTION_DOMAIN_MUST_BE_IN_MAINTENANCE_OR_UNATTACHED,
                    String.format(STORAGE_DOMAIN_NAME_REPLACEMENT, storageDomain.getStorageName()));
        }

        return ValidationResult.VALID;
    }

    public boolean isConnectionForISCSIDomainAttached(StorageDomain storageDomain) {
        List<StorageServerConnections> connectionsForDomain = getAllConnectionsForDomain(storageDomain.getId());
        for (StorageServerConnections connectionForDomain : connectionsForDomain) {
            if (connectionForDomain.getId().equals(connection.getId())) {
                return true;
            }
        }
        return false;
    }

    protected List<StorageServerConnections> getAllConnectionsForDomain(Guid storageDomainId) {
        return getStorageServerConnectionDao().getAllForDomain(storageDomainId);
    }

    protected StorageServerConnectionDao getStorageServerConnectionDao() {
        return Injector.get(StorageServerConnectionDao.class);
    }
}
