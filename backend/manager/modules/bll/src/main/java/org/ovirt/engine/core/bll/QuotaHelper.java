package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcmentTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class QuotaHelper {
    public static final Long UNLIMITED = -1l;
    public static final Long EMPTY = 0l;
    private static final Log log = LogFactory.getLog(QuotaHelper.class);

    private QuotaHelper() {
    }

    private static final QuotaHelper quotaHelper = new QuotaHelper();

    public static QuotaHelper getInstance() {
        return quotaHelper;
    }

    /**
     * Returns default quota id if the <code>Data Center</code> is disabled, <BR/>
     * or the quota id that was send.
     * @param quotaId
     * @param storagePoolId
     * @return
     */
    public Guid getQuotaIdToConsume(Guid quotaId, storage_pool storagePool) {
        Guid returnedQuotaGuid = quotaId;
        if (storagePool == null) {
            log.errorFormat("Storage pool is null, Quota id will be set from the parameter");
        } else if (storagePool.getQuotaEnforcementType() == QuotaEnforcmentTypeEnum.DISABLED) {
            // If storage pool has disabled quota enforcement, then initialize default quota.
            log.debugFormat("Storage pool quota is disabled, Quota id which will be consume from is the default DC quota");
            returnedQuotaGuid =
                    getQuotaDAO()
                            .getDefaultQuotaByStoragePoolId(storagePool.getId())
                            .getId();
        }
        return returnedQuotaGuid;
    }

    public void setDefaultQuotaAsRegularQuota(storage_pool storagePool) {
        Quota quota = getQuotaDAO().getDefaultQuotaByStoragePoolId(storagePool.getId());
        if (quota != null) {
            quota.setIsDefaultQuota(false);
            getQuotaDAO().update(quota);
        }
    }

    public List<PermissionSubject> addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList,
            storage_pool storagePool,
            Guid quotaId) {
        if (storagePool != null && storagePool.getQuotaEnforcementType() != QuotaEnforcmentTypeEnum.DISABLED) {
            log.debug("Adding validation for consume quota to permission subjects list");
            quotaPermissionList.add(new PermissionSubject(quotaId, VdcObjectType.Quota, ActionGroup.CONSUME_QUOTA));
        }
        return quotaPermissionList;
    }

    /**
     * Returns unlimited Quota for storage pool.
     *
     * @param storagePool
     *            - The storage pool to create the unlimited Quota for.
     * @return Boolean value if succeeded or not.
     */
    public Quota getUnlimitedQuota(storage_pool storagePool, boolean isDefaultQuota) {
        if (storagePool == null || storagePool.getId() == null) {
            log.error("Unlimited Quota cannot be created, Storage pool is not valid ");
            return null;
        }

        // Set new Quota definition.
        Quota quota = new Quota();
        Guid quotaId = Guid.NewGuid();
        quota.setId(quotaId);
        quota.setStoragePoolId(storagePool.getId());
        quota.setQuotaName(getDefaultQuotaName(storagePool));
        quota.setDescription("Automatic generated Quota for Data Center " + storagePool.getname());
        quota.setThresholdVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdVdsGroup));
        quota.setThresholdStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaThresholdStorage));
        quota.setGraceVdsGroupPercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceVdsGroup));
        quota.setGraceStoragePercentage(Config.<Integer> GetValue(ConfigValues.QuotaGraceStorage));
        quota.setIsDefaultQuota(isDefaultQuota);
        quota.setQuotaVdsGroups(new ArrayList<QuotaVdsGroup>());
        quota.setQuotaStorages(new ArrayList<QuotaStorage>());

        // Set Quota storage capacity definition.
        quota.setStorageSizeGB(UNLIMITED);

        // Set Quota cluster virtual memory definition.
        quota.setMemSizeMB(UNLIMITED);

        // Set Quota cluster virtual CPU definition.
        quota.setVirtualCpu(UNLIMITED.intValue());

        return quota;
    }

    public String getDefaultQuotaName(storage_pool storagePool) {
        List<Quota> quotaList = getQuotaDAO().getQuotaByStoragePoolGuid(storagePool.getId());
        String quotaDefaultName = "Quota_Def_" + storagePool.getname();
        String regexToolPattern = quotaDefaultName + "_[0-9]{1,}$";
        int suffixQuotaName = 0;
        for (Quota quota : quotaList) {
            String quotaName = quota.getQuotaName();
            if (quotaName.contains(quotaDefaultName)
                    && (quotaName.length() == quotaDefaultName.length() && suffixQuotaName == 0)) {
                suffixQuotaName = 1;
            } else if (quotaName.matches(regexToolPattern)) {
                String defaultQuotaString = quotaName.substring(quotaName.lastIndexOf("_") + 1);
                Integer defaultQuotaNumber = new Integer(defaultQuotaString);
                if (suffixQuotaName <= defaultQuotaNumber.intValue()) {
                    suffixQuotaName = defaultQuotaNumber.intValue() + 1;
                }
            }
        }
        return (suffixQuotaName > 0) ? String.format("%1$s_%2$s", quotaDefaultName, suffixQuotaName)
                : quotaDefaultName;
    }

    public boolean checkQuotaValidationForAddEdit(Quota quota, List<String> messages) {
        if (quota == null) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            return false;
        }

        // Check if quota name exists.
        if (!checkQuotaNameExisting(quota, messages)) {
            return false;
        }

        // Check quota added is not default quota.
        if (quota.getIsDefaultQuota()) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_CAN_NOT_HAVE_DEFAULT_INDICATION.toString());
            return false;
        }

        // If specific Quota for storage is specified
        if (!validateQuotaStorageLimitation(quota, messages)) {
            return false;
        }
        if (!validateQuotaVdsGroupLimitation(quota, messages)) {
            return false;
        }

        return true;
    }

    /**
     * Save new <code>Quota</code> with permissions for ad_element_id to consume from.
     *
     * @param quota
     *            - The quota to be saved
     * @param ad_element_id
     *            - The user which will have consume permissions on the quota.
     */
    public void saveQuotaForUser(Quota quota, Guid ad_element_id) {
        DbFacade.getInstance().getQuotaDAO().save(quota);
        permissions perm =
                new permissions(ad_element_id,
                        PredefinedRoles.QUOTA_CONSUMER.getId(),
                        quota.getId(),
                        VdcObjectType.Quota);
        PermissionsOperationsParametes permParams = new PermissionsOperationsParametes(perm);
        Backend.getInstance().runInternalAction(VdcActionType.AddPermission,
                permParams);
    }

    /**
     * Helper method which get as an input disk image list for VM or template and returns a list of quotas and their
     * desired limitation to be used.<BR/>
     *
     * @param diskImages
     *            - The disk image list to be grouped by
     * @return List of summarized requested size for quota.
     */
    public Map<Pair<Guid, Guid>, Double> getQuotaConsumeMap(Collection<DiskImage> diskImages) {
        Map<Pair<Guid, Guid>, Double> quotaForStorageConsumption = new HashMap<Pair<Guid, Guid>, Double>();
        for (DiskImage disk : diskImages) {
            Pair<Guid, Guid> quotaForStorageKey =
                    new Pair<Guid, Guid>(disk.getQuotaId(), disk.getstorage_ids().get(0).getValue());
            Double storageRequest = quotaForStorageConsumption.get(quotaForStorageKey);
            if (storageRequest != null) {
                storageRequest += disk.getsize();
            } else {
                storageRequest = new Double(disk.getsize());
            }
            quotaForStorageConsumption.put(quotaForStorageKey, storageRequest);
        }

        return quotaForStorageConsumption;
    }

    public boolean checkQuotaNameExisting(Quota quota, List<String> messages) {
        Quota quotaByName = getQuotaDAO().getQuotaByQuotaName(quota.getQuotaName());

        // Check if there is no quota with the same name that already exists.
        if ((quotaByName != null) && (!quotaByName.getId().equals(quota.getId()))) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_NAME_ALREADY_EXISTS.toString());
            return false;
        }
        return true;
    }

    /**
     * Validate Quota storage restrictions.
     *
     * @param quota
     * @param messages
     * @return
     */
    private static boolean validateQuotaStorageLimitation(Quota quota, List<String> messages) {
        boolean isValid = true;
        List<QuotaStorage> quotaStorageList = quota.getQuotaStorages();
        if (quotaStorageList != null && !quotaStorageList.isEmpty() && isGlobalLimitExist(quota.getStorageSizeGB())) {
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString());
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
    private static boolean validateQuotaVdsGroupLimitation(Quota quota, List<String> messages) {
        boolean isValid = true;
        List<QuotaVdsGroup> quotaVdsGroupList = quota.getQuotaVdsGroups();
        if (quotaVdsGroupList != null && !quotaVdsGroupList.isEmpty()) {
            boolean isSpecificVirtualCpu = false;
            boolean isSpecificVirtualRam = false;

            for (QuotaVdsGroup quotaVdsGroup : quotaVdsGroupList) {
                if (quotaVdsGroup.getVirtualCpu() != null) {
                    isSpecificVirtualCpu = true;
                }
                if (quotaVdsGroup.getMemSizeMB() != null) {
                    isSpecificVirtualRam = true;
                }
            }

            // if the global vds group limit was not specified, then specific limitation must be specified.
            if ((isGlobalLimitExist(quota.getMemSizeMB()) && isSpecificVirtualRam)
                    || (isGlobalLimitExist(quota.getVirtualCpu()) && isSpecificVirtualCpu)) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_LIMIT_IS_SPECIFIC_AND_GENERAL.toString());
                isValid = false;
            }
        }
        return isValid;
    }

    private static boolean isGlobalLimitExist(Object globalStorageLimit) {
        return globalStorageLimit != null;
    }

    /**
     * @return The VdsGroupDAO
     */
    protected VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDAO();
    }

    /**
     * @return The StorageDomainDAO
     */
    protected StorageDomainDAO getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDAO();
    }

    /**
     * @return The quotaDAO
     */
    protected QuotaDAO getQuotaDAO() {
        return DbFacade.getInstance().getQuotaDAO();
    }

    /**
     * @return The StoragePoolDAO
     */
    protected StoragePoolDAO getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDAO();
    }
}
