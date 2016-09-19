package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class QuotaCRUDCommand extends CommandBase<QuotaCRUDParameters> {
    private Quota quota;

    public QuotaCRUDCommand(QuotaCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStoragePoolId(getParameters().getQuota().getStoragePoolId());
    }

    public Quota getQuota() {
        if (quota == null) {
            setQuota(quotaDao.getById(getParameters().getQuotaId()));
        }
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    @Override
    protected boolean validate() {
        Quota quota = getParameters().getQuota();

        // Cannot add or update a quota to be default using this command
        if (quota.isDefault()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            return false;
        }

        // Check if quota name exists or
        // If specific Quota for storage is specified or
        // If specific Quota for cluster is specific
        return validateQuotaNameIsUnique(quota) &&
                validateQuotaStorageLimitation(quota) &&
                validateQuotaClusterLimitation(quota);
    }

    public boolean validateQuotaNameIsUnique(Quota quota) {
        Quota quotaByName = quotaDao.getQuotaByQuotaName(quota.getQuotaName(), quota.getStoragePoolId());

        // Check if there is no quota with the same name that already exists.
        if ((quotaByName != null) && !quotaByName.getId().equals(quota.getId())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            return false;
        }
        return true;
    }

    /**
     * Validate Quota storage restrictions.
     */
    private boolean validateQuotaStorageLimitation(Quota quota) {
        boolean isValid = true;
        List<QuotaStorage> quotaStorageList = quota.getQuotaStorages();
        if (quota.isGlobalStorageQuota() && (quotaStorageList != null && !quotaStorageList.isEmpty())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Validate Quota vds group restrictions.
     *
     * @param quota
     *            - Quota we validate
     * @return Boolean value if the quota is valid or not.
     */
    private boolean validateQuotaClusterLimitation(Quota quota) {
        boolean isValid = true;
        List<QuotaCluster> quotaClusterList = quota.getQuotaClusters();
        if (quotaClusterList != null && !quotaClusterList.isEmpty()) {
            boolean isSpecificVirtualCpu = false;
            boolean isSpecificVirtualRam = false;

            for (QuotaCluster quotaCluster : quotaClusterList) {
                isSpecificVirtualCpu = quotaCluster.getVirtualCpu() != null;
                isSpecificVirtualRam = quotaCluster.getMemSizeMB() != null;
            }

            // if the global vds group limit was not specified, then specific limitation must be specified.
            if (quota.isGlobalClusterQuota() && (isSpecificVirtualRam || isSpecificVirtualCpu)) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL);
                isValid = false;
            }
        }
        return isValid;
    }

    protected Guid getQuotaId() {
        return getQuota().getId();
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
    }

    public String getQuotaName() {
        return quota.getQuotaName();
    }

}
