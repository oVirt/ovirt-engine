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
import org.ovirt.engine.core.dao.QuotaDao;

public abstract class QuotaCRUDCommand extends CommandBase<QuotaCRUDParameters> {

    private Quota quota;

    public QuotaCRUDCommand(QuotaCRUDParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public Quota getQuota() {
        if (quota == null) {
            setQuota(getQuotaDao().getById(getParameters().getQuotaId()));
        }
        return quota;
    }

    public void setQuota(Quota quota) {
        this.quota = quota;
    }

    protected boolean checkQuotaValidationCommon(Quota quota, List<String> messages) {
        if (quota == null) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            return false;
        }

        // Check if quota name exists or
        // If specific Quota for storage is specified or
        // If specific Quota for cluster is specific
        if (!checkQuotaNameExisting(quota, messages) ||
                !validateQuotaStorageLimitation(quota, messages) ||
                !validateQuotaClusterLimitation(quota, messages)) {
            return false;
        }

        return true;
    }

    public boolean checkQuotaNameExisting(Quota quota, List<String> messages) {
        Quota quotaByName = getQuotaDao().getQuotaByQuotaName(quota.getQuotaName());

        // Check if there is no quota with the same name that already exists.
        if ((quotaByName != null) && !quotaByName.getId().equals(quota.getId())) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED.toString());
            return false;
        }
        return true;
    }

    /**
     * Validate Quota storage restrictions.
     */
    private static boolean validateQuotaStorageLimitation(Quota quota, List<String> messages) {
        boolean isValid = true;
        List<QuotaStorage> quotaStorageList = quota.getQuotaStorages();
        if (quota.isGlobalStorageQuota() && (quotaStorageList != null && !quotaStorageList.isEmpty())) {
            messages.add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString());
            isValid = false;
        }
        return isValid;
    }

    /**
     * Validate Quota vds group restrictions.
     *
     * @param quota
     *            - Quota we validate
     * @param messages
     *            - Messages of can do action.
     * @return Boolean value if the quota is valid or not.
     */
    private static boolean validateQuotaClusterLimitation(Quota quota, List<String> messages) {
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
                messages.add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString());
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

    protected QuotaDao getQuotaDao() {
        return getDbFacade().getQuotaDao();
    }

    public String getQuotaName() {
        return quota.getQuotaName();
    }

}
