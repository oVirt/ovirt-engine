package org.ovirt.engine.core.bll.quota;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.common.action.QuotaCRUDParameters;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;

public abstract class QuotaCRUDCommand extends CommandBase<QuotaCRUDParameters> {
    private Quota quota;
    @Inject
    private QuotaDao quotaDao;

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
    protected void init() {
        fillQuotaParameter();
    }

    @Override
    protected boolean validate() {
        Quota quota = getParameters().getQuota();

        // Cannot add or update a quota to be default using this command
        if (quota.isDefault()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            return false;
        }

        QuotaValidator quotaValidator = QuotaValidator.createInstance(quota, false);

        // Validate quota and check if the name already exists
        return validate(quotaValidator.isValid()) &&
                validateQuotaNameIsUnique(quota);
    }

    private boolean validateQuotaNameIsUnique(Quota quota) {
        Quota quotaByName = quotaDao.getQuotaByQuotaName(quota.getQuotaName(), quota.getStoragePoolId());

        // Check if there is no quota with the same name that already exists.
        if ((quotaByName != null) && !quotaByName.getId().equals(quota.getId())) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_NAME_ALREADY_USED);
            return false;
        }
        return true;
    }

    /**
     * Fills missing data in the quota parameter
     */
    private void fillQuotaParameter() {
        Quota quotaParameter = getParameters().getQuota();

        setQuotaStorage(quotaParameter);
        setQuotaCluster(quotaParameter);
        setQuotaThresholdDefaults(quotaParameter);
    }

    private void setQuotaStorage(Quota quota) {
        // Create unlimited global storage quota if no other is specified
        if (quota.isEmptyStorageQuota()) {
            quota.setGlobalQuotaStorage(new QuotaStorage(Guid.newGuid(),
                    quota.getId(),
                    null,
                    -1L,
                    0.0));
            return;
        }

        if (quota.isGlobalStorageQuota()) {
            quota.getGlobalQuotaStorage().setQuotaId(quota.getId());
            quota.getGlobalQuotaStorage().setQuotaStorageId(Guid.newGuid());
            return;
        }

        for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
            quotaStorage.setQuotaId(quota.getId());
            quotaStorage.setQuotaStorageId(Guid.newGuid());
        }
    }

    private void setQuotaCluster(Quota quota) {
        // Create unlimited global cluster quota if no other is specified
        if (quota.isEmptyClusterQuota()) {
            quota.setGlobalQuotaCluster(new QuotaCluster(Guid.newGuid(),
                    quota.getId(),
                    null,
                    -1,
                    0,
                    -1L,
                    0L));

            return;
        }

        if (quota.isGlobalClusterQuota()) {
            quota.getGlobalQuotaCluster().setQuotaId(quota.getId());
            quota.getGlobalQuotaCluster().setQuotaClusterId(Guid.newGuid());
            return;
        }

        for (QuotaCluster quotaCluster : quota.getQuotaClusters()) {
            quotaCluster.setQuotaId(quota.getId());
            quotaCluster.setQuotaClusterId(Guid.newGuid());
        }
    }

    // Setting defaults for hard and soft limits, for REST
    private void setQuotaThresholdDefaults(Quota quotaParameter) {
        if (quotaParameter.getGraceStoragePercentage() == 0) {
            quotaParameter.setGraceStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaGraceStorage));
        }
        if (quotaParameter.getGraceClusterPercentage() == 0) {
            quotaParameter.setGraceClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaGraceCluster));
        }
        if (quotaParameter.getThresholdStoragePercentage() == 0) {
            quotaParameter.setThresholdStoragePercentage(Config.<Integer> getValue(ConfigValues.QuotaThresholdStorage));
        }
        if (quotaParameter.getThresholdClusterPercentage() == 0) {
            quotaParameter.setThresholdClusterPercentage(Config.<Integer>getValue(ConfigValues.QuotaThresholdCluster));
        }
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
