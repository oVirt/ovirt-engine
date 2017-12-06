package org.ovirt.engine.core.bll.validator;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.di.Injector;

public class QuotaValidator {

    @Inject
    private QuotaDao quotaDao;

    private Guid quotaId;
    private boolean allowNullId;
    private Quota quota;
    private boolean quotaDaoAccessed = false;

    public static QuotaValidator createInstance(Guid quotaId, boolean allowNullId) {
        return Injector.injectMembers(new QuotaValidator(quotaId, allowNullId));
    }

    public static QuotaValidator createInstance(Quota quota, boolean allowNullId) {
        return Injector.injectMembers(new QuotaValidator(quota, allowNullId));
    }

    protected QuotaValidator(Guid quotaId, boolean allowNullId) {
        this.quotaId = quotaId;
        this.allowNullId = allowNullId;
    }

    protected QuotaValidator(Quota quota, boolean allowNullId) {
        this.quota = quota;
        this.quotaId = quota.getId();
        this.allowNullId = allowNullId;
    }

    public ValidationResult isValid() {
        if (Guid.isNullOrEmpty(quotaId)) {
            return ValidationResult
                    .failWith(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NULL_NOT_ALLOWED)
                    .unless(allowNullId);
        }

        Quota quota = getQuota();
        if (quota == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST);
        }

        if (quota.isEmptyStorageQuota() || quota.isEmptyClusterQuota()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
        }

        if (quota.isGlobalClusterQuota() && quota.getQuotaClusters() != null && !quota.getQuotaClusters().isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL);
        }

        if (quota.isGlobalStorageQuota() && quota.getQuotaStorages() != null && !quota.getQuotaStorages().isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isDefinedForStoragePool(Guid storagePoolId) {
        if (allowNullId && Guid.isNullOrEmpty(quotaId)) {
            return ValidationResult.VALID;
        }

        return ValidationResult
                .failWith(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID)
                .unless(getQuota().getStoragePoolId().equals(storagePoolId));
    }

    public ValidationResult isDefinedForStorageDomain(Guid storageDomainId) {
        if (allowNullId && Guid.isNullOrEmpty(quotaId)) {
            return ValidationResult.VALID;
        }

        Quota quota = getQuota();
        if (quota.isGlobalStorageQuota()) {
            return ValidationResult.VALID;
        }

        if (quota.getQuotaStorages().stream()
                .map(QuotaStorage::getStorageId)
                .anyMatch(storageDomainId::equals)) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_DEFINED_FOR_DOMAIN);
    }

    public ValidationResult isDefinedForCluster(Guid clusterId) {
        if (allowNullId && quotaId == null) {
            return ValidationResult.VALID;
        }

        Quota quota = getQuota();
        if (quota.isGlobalClusterQuota()) {
            return ValidationResult.VALID;
        }

        if (quota.getQuotaClusters().stream()
                .map(QuotaCluster::getClusterId)
                .anyMatch(clusterId::equals)) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_DEFINED_FOR_CLUSTER);
    }

    private Quota getQuota() {
        if (quota == null && !quotaDaoAccessed) {
            quota = quotaDao.getById(quotaId);
            quotaDaoAccessed = true;
        }

        return quota;
    }
}
