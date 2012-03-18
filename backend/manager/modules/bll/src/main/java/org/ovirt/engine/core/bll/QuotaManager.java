package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcmentTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.utils.Pair;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class QuotaManager {
    /**
     * commandDeltaMap is a map which reflects the delta consumption of commands before persisted into the DB.<BR/>
     * The map key represents quota limitation id, and the value is an inner map which represent all the commands
     * consumption.<BR/>
     * The inner map key is the command id and the value is <class>QuotaDeltaValue</class>, which is an inner class
     * representing all the usage of the resources.
     */
    private static Map<Guid, Map<Guid, QuotaDeltaValue>> commandDeltaMap =
            new HashMap<Guid, Map<Guid, QuotaDeltaValue>>();

    /**
     * concurrent map of quota locks.<BR/>
     * Each quota operation should be synchronized with a lock, only with the quota it self, for avoiding concurrency
     * issues.
     */
    private static ConcurrentMap<Guid, Lock> quotaSync = new ConcurrentHashMap<Guid, Lock>();

    /**
     * Enum which represent the status of the operation in relation to the limit.
     */
    private enum LimitQuotaUsedType {
        VALID_LIMIT,
        THRESHOLD_LIMIT,
        GRACE_LIMIT,
        OFF_LIMIT
    };

    private static QuotaDAO getQuotaDAO() {
        return DbFacade.getInstance()
                .getQuotaDAO();
    }

    public static void removeStorageDeltaQuotaCommand(Guid quotaId, Guid storageDomainId, QuotaEnforcmentTypeEnum quotaEnforcedType, Guid commandId) {
        if (quotaEnforcedType != QuotaEnforcmentTypeEnum.DISABLED) {
            QuotaStorage quotaStorage = getQuotaStorageForStorageDomainId(quotaId, storageDomainId);
            getLockForQuotaId(quotaId).lock();
            try {
                if ((quotaStorage != null) && (commandDeltaMap.get(quotaStorage.getQuotaStorageId()) != null)) {
                    log.debugFormat("Found delta quota storage with id {0} for command id {0}",
                            quotaStorage.getQuotaStorageId(),
                            commandId);
                    commandDeltaMap.get(quotaStorage.getQuotaStorageId()).remove(commandId);
                    return;
                }
            } finally {
                getLockForQuotaId(quotaId).unlock();
            }
        }
    }

    public static void removeMultiStorageDeltaQuotaCommand(Map<Pair<Guid, Guid>, Double> quotaForStorageConsumeMap, QuotaEnforcmentTypeEnum quotaEnforcedType, Guid commandId) {
        for (Pair<Guid, Guid> quotaConsumeKey : quotaForStorageConsumeMap.keySet()) {
            removeStorageDeltaQuotaCommand(quotaConsumeKey.getFirst(), quotaConsumeKey.getSecond(), quotaEnforcedType, commandId);
        }
    }

    public static void removeVdsGroupDeltaQuotaCommand(Guid quotaId, Guid vdsGroupId, Guid commandId) {
        QuotaVdsGroup quotaVdsGroup = getQuotaVdsGroupForVdsGroupId(quotaId, vdsGroupId);
        getLockForQuotaId(quotaId).lock();
        try {
            if ((quotaVdsGroup != null) && (commandDeltaMap.get(quotaVdsGroup.getQuotaVdsGroupId()) != null)) {
                log.debugFormat("Found delta quota storage with id {0} for command id {0}",
                        vdsGroupId,
                        commandId);
                commandDeltaMap.get(quotaVdsGroup.getQuotaVdsGroupId()).remove(commandId);
                return;
            }
        } finally {
            getLockForQuotaId(quotaId).unlock();
        }
    }

    public static void reduceCommandVdsGroupSize(Guid vdsGroupId,
            Integer subtractedCpuSize,
            QuotaEnforcmentTypeEnum quotaEnforcedType,
            Double subtractedMemSize,
            Guid commandId, Guid quotaId) {
        if (!validateReduceQuotaParameters(vdsGroupId, quotaEnforcedType, commandId, quotaId)) {
            return;
        }
        if ((subtractedCpuSize == null || subtractedCpuSize.longValue() == 0)
                && (subtractedMemSize == null || subtractedMemSize.longValue() == 0)) {
            log.errorFormat("Subtracted size is not valid, quota delta will not be reduced for qutoa Id {0} and vds group Id {1}.",
                    quotaId,
                    vdsGroupId);
            return;
        }
        QuotaVdsGroup quotaVdsGroup = getQuotaVdsGroupForVdsGroupId(quotaId, vdsGroupId);
        getLockForQuotaId(quotaId).lock();
        try {
            Map<Guid, QuotaDeltaValue> quotaDeltaMap = commandDeltaMap.get(quotaVdsGroup.getQuotaVdsGroupId());
            if (quotaDeltaMap == null) {
                log.errorFormat("Quota id {0} for vds group id {1} has no associated command map",
                        quotaId,
                        vdsGroupId);
            } else if (quotaDeltaMap.get(commandId) == null) {
                log.errorFormat("Quota id {0} for vds group id {1} and command id (2) has no associated delta quota",
                        quotaId,
                        vdsGroupId,
                        commandId);
            } else {
                QuotaDeltaValue quotaDeltaValue = quotaDeltaMap.get(commandId);
                Integer cpuDeltaForCommandVal = quotaDeltaValue.getCpuSizeToBeUsed();
                Double memDeltaForCommandVal = quotaDeltaValue.getMemSizeToBeUsed();

                if (cpuDeltaForCommandVal == null || memDeltaForCommandVal == null) {
                    log.errorFormat("Quota id {0} for vds group id {1} and command id (2) does not have cpu or memory delta parameters.",
                            quotaId,
                            vdsGroupId,
                            commandId);
                } else if ((cpuDeltaForCommandVal < subtractedCpuSize) || (memDeltaForCommandVal < subtractedMemSize)) {
                    log.errorFormat("Quota id {0} for vds group id {1} and command id (2) has less vds group delta from the size requested to be reduced. The delta value will be set to 0",
                            quotaId,
                            vdsGroupId,
                            commandId);
                    quotaDeltaValue.setCpuSizeToBeUsed(0);
                    quotaDeltaValue.setMemSizeToBeUsed(0d);
                } else {
                    quotaDeltaValue.setMemSizeToBeUsed(quotaDeltaValue.getMemSizeToBeUsed() - subtractedMemSize);
                    quotaDeltaValue.setCpuSizeToBeUsed(quotaDeltaValue.getCpuSizeToBeUsed() - subtractedCpuSize);
                }
            }
        } finally {
            getLockForQuotaId(quotaId).unlock();
        }
    }

    public static void reduceCommandStorageSize(Guid storageDomainId,
            Long subtractedSize,
            QuotaEnforcmentTypeEnum quotaEnforcedType,
            Guid commandId, Guid quotaId) {
        if (!validateReduceQuotaParameters(storageDomainId, quotaEnforcedType, commandId, quotaId)) {
            return;
        }
        if (subtractedSize == null || subtractedSize.longValue() == 0) {
            log.errorFormat("Subtracted size is not valid, quota delta will not be reduced for qutoa Id {0} and storage domain Id {1}.",
                    quotaId,
                    storageDomainId);
            return;
        }
        QuotaStorage quotaStorage = getQuotaStorageForStorageDomainId(quotaId, storageDomainId);
        getLockForQuotaId(quotaId).lock();
        try {
            Map<Guid, QuotaDeltaValue> quotaDeltaMap = commandDeltaMap.get(quotaStorage.getQuotaStorageId());
            if (quotaDeltaMap == null) {
                log.errorFormat("Quota id {0} for storage domain id {1} has no associated command map",
                        quotaId,
                        storageDomainId);
            } else if (quotaDeltaMap.get(commandId) == null) {
                log.errorFormat("Quota id {0} for storage domain id {1} and command id (2) has no associated delta quota",
                        quotaId,
                        storageDomainId,
                        commandId);
            } else {
                QuotaDeltaValue quotaDeltaValue = quotaDeltaMap.get(commandId);
                Double storageSize = quotaDeltaValue.getStorageSizeToBeUsed();
                if (storageSize == null) {
                    log.errorFormat("Quota id {0} for storage domain id {1} and command id (2) does not have storage delta size",
                            quotaId,
                            storageDomainId,
                            commandId);
                } else if (storageSize < subtractedSize) {
                    log.errorFormat("Quota id {0} for storage domain id {1} and command id (2) has less storage delta size from the size requested to be reduced. The delta value will be se to 0",
                            quotaId,
                            storageDomainId,
                            commandId);
                    quotaDeltaValue.setStorageSizeToBeUsed(0d);
                } else {
                    quotaDeltaValue.setStorageSizeToBeUsed(quotaDeltaValue.getStorageSizeToBeUsed() - subtractedSize);
                }
            }
        } finally {
            getLockForQuotaId(quotaId).unlock();
        }

    }

    public static boolean validateStorageQuota(Guid storageDomainId,
            Guid quotaId,
            QuotaEnforcmentTypeEnum quotaEnforcedType,
            Double desiredSizeInGB,
            Guid commandId,
            List<String> canDoActionMessages) {
        if (quotaEnforcedType == QuotaEnforcmentTypeEnum.DISABLED) {
            return true;
        }
        if (quotaId == null || commandId == null || desiredSizeInGB == null || storageDomainId == null) {
            canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            return false;
        }
        Quota quota = getQuotaDAO().getById(quotaId);
        if (quota == null) {
            canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            return false;
        }

        double graceStoragePercentage = 1 + new Double(quota.getGraceStoragePercentage()) / 100;
        double thresholdStoragePercentage = new Double(quota.getThresholdStoragePercentage()) / 100;
        String quotaName = quota.getQuotaName();

        // Get limitation and usage of quota which is enforced on the storage domain, if the limitation is
        // global (On the storage pool) then return the limitation on all the storage pool.
        // For specific quota the quota which will be returned will be the same one (quotaID parameter).
        QuotaStorage quotaStorage = getQuotaStorageForStorageDomainId(quotaId, storageDomainId);
        getLockForQuotaId(quotaId).lock();
        try {
            if (!validateStorageForEnforcedStoragePool(quotaStorage,
                    desiredSizeInGB,
                    thresholdStoragePercentage,
                    graceStoragePercentage,
                    quotaName,
                    quotaEnforcedType,
                    commandId,
                    canDoActionMessages)) {
                return false;
            } else {
                addCommandForStorageUse(desiredSizeInGB, commandId, quotaStorage.getQuotaStorageId());
            }

            return true;
        } finally {
            getLockForQuotaId(quotaId).unlock();
        }
    }

    public static boolean validateVdsGroupQuota(Guid vdsGroupId,
            Guid quotaId,
            QuotaEnforcmentTypeEnum quotaEnforcedType,
            Integer desiredCpu,
            Double desiredMem,
            Guid commandId,
            List<String> canDoActionMessages) {
        if (quotaEnforcedType == QuotaEnforcmentTypeEnum.DISABLED) {
            return true;
        }
        if (quotaId == null || commandId == null || desiredMem == null || desiredCpu == null || vdsGroupId == null) {
            canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            return false;
        }

        Quota quota = getQuotaDAO().getById(quotaId);
        double graceVdsGroupPercentage = 1 + new Double(quota.getGraceVdsGroupPercentage()) / 100;
        double thresholdVdsGroupPercentage = new Double(quota.getThresholdVdsGroupPercentage()) / 100;
        String quotaName = quota.getQuotaName();

        // Get limitation and usage of quota which is enforced on the vds group domain, if the limitation is
        // global (On the storage pool) then return the limitation on all the storage pool.
        // For specific quota the quota which will be returned will be the same one (quotaID parameter).
        QuotaVdsGroup quotaVdsGroup = getQuotaVdsGroupForVdsGroupId(quotaId, vdsGroupId);

        getLockForQuotaId(quotaId).lock();
        try {
            if (!validateVdsGroupForEnforcedStoragePool(quotaVdsGroup,
                    desiredCpu,
                    desiredMem,
                    thresholdVdsGroupPercentage,
                    graceVdsGroupPercentage,
                    quotaName,
                    quotaEnforcedType,
                    canDoActionMessages)) {
                return false;
            } else {
                addCommandForVdsGroupUse(desiredCpu, desiredMem, commandId, quotaId);
            }
            return true;
        } finally {
            getLockForQuotaId(quotaId).unlock();
        }
    }

    private static boolean validateReduceQuotaParameters(Guid entityId,
            QuotaEnforcmentTypeEnum quotaEnforcedType,
            Guid commandId,
            Guid quotaId) {
        if (quotaEnforcedType == QuotaEnforcmentTypeEnum.DISABLED) {
            log.debugFormat("Data Center is disabled, so no delta quota will be counting.");
            return false;
        }
        if (quotaId == null || commandId == null || entityId == null) {
            log.errorFormat("parameters are null.");
            return false;
        }
        return true;
    }

    /**
     * @param quotaEnforcedType
     * @param quotaForStorageConsumeMap - Map key is a <code>Pair</code> object which is assembled from quota id as first parameter and the second value is storage domain id
     * @param commandId
     * @param canDoActionMessages
     * @return
     */
    public static boolean validateMultiStorageQuota(QuotaEnforcmentTypeEnum quotaEnforcedType,
            Map<Pair<Guid, Guid>, Double> quotaForStorageConsumeMap,
            Guid commandId,
            List<String> canDoActionMessages) {
        if (quotaEnforcedType == QuotaEnforcmentTypeEnum.DISABLED) {
            return true;
        }
        boolean isSucceeded = true;
        Map<Pair<Guid, Guid>, Double> quotaForStorageCompensationMap = new HashMap<Pair<Guid, Guid>, Double>();
        try {
            isSucceeded = updateMultiStorageQuotaMap(quotaEnforcedType,
                    quotaForStorageConsumeMap,
                    commandId,
                    canDoActionMessages,
                    quotaForStorageCompensationMap);
        } finally {
            // If command not succeeded then compensate all the storage quota maps that were updated.
            if (!isSucceeded) {
                updateMultiStorageQuotaMap(quotaEnforcedType,
                        quotaForStorageCompensationMap,
                        commandId,
                        canDoActionMessages,
                        new HashMap<Pair<Guid, Guid>, Double>());
                return false;
            }
        }
        return true;
    }

    private static boolean updateMultiStorageQuotaMap(QuotaEnforcmentTypeEnum quotaEnforcedType,
            Map<Pair<Guid, Guid>, Double> quotaForStorageConsumeMap,
            Guid commandId,
            List<String> canDoActionMessages,
            Map<Pair<Guid, Guid>, Double> quotaForStorageCompensationMap) {
        if (quotaEnforcedType == QuotaEnforcmentTypeEnum.DISABLED) {
            return true;
        }
        boolean isSucceeded = true;
        for (Pair<Guid, Guid> quotaForStorageKey : quotaForStorageConsumeMap.keySet()) {
            Guid quotaId = quotaForStorageKey.getFirst();
            Guid storageDomainId = quotaForStorageKey.getSecond();
            Double storageSize = quotaForStorageConsumeMap.get(quotaForStorageKey);

            // Add storage size to the delta map.
            isSucceeded = isSucceeded &&
                    validateStorageQuota(storageDomainId,
                            quotaId,
                            quotaEnforcedType,
                            storageSize,
                            commandId,
                            canDoActionMessages);
            if (isSucceeded) {
                quotaForStorageCompensationMap.put(quotaForStorageKey, storageSize * -1);
            }

        }
        return isSucceeded;
    }

    private static QuotaStorage getQuotaStorageForStorageDomainId(Guid quotaId, Guid storageDomainId) {
        List<QuotaStorage> listQuotaStorage = getQuotaDAO().getQuotaStorageByStorageGuid(storageDomainId, quotaId);
        return listQuotaStorage != null && !listQuotaStorage.isEmpty() ? listQuotaStorage.get(0) : null;
    }

    private static QuotaVdsGroup getQuotaVdsGroupForVdsGroupId(Guid quotaId, Guid vdsGroupId) {
        List<QuotaVdsGroup> listQuotaVdsGroup = getQuotaDAO().getQuotaVdsGroupByVdsGroupGuid(vdsGroupId, quotaId);
        return listQuotaVdsGroup != null && !listQuotaVdsGroup.isEmpty() ? listQuotaVdsGroup.get(0) : null;
    }

    private static void addCommandForVdsGroupUse(Integer desiredCpu,
            Double desiredMem,
            Guid commandId,
            Guid quotaLimitId) {
        Map<Guid, QuotaDeltaValue> quotaDeltaMap = getCommandForQuotaId(quotaLimitId);
        if (quotaDeltaMap == null || quotaDeltaMap.isEmpty()) {
            addCommandForNewQuota(commandId, quotaLimitId, 0d, desiredCpu, desiredMem);
        }
        QuotaDeltaValue quotaDeltaVal = quotaDeltaMap.get(commandId);
        if (quotaDeltaVal != null) {
            if ((quotaDeltaVal.getCpuSizeToBeUsed() != null && quotaDeltaVal.getCpuSizeToBeUsed() > 0) ||
                    (quotaDeltaVal.getMemSizeToBeUsed() != null && quotaDeltaVal.getMemSizeToBeUsed() > 0)) {
                log.warnFormat("Command id {0} already has quota id {1} associated with it with delta size {2}. The desired cpu and memory will be added to the existing delta size.",
                        commandId,
                        quotaLimitId,
                        quotaDeltaVal.getStorageSizeToBeUsed());
                Integer cpuSizeToBeUsed =
                        quotaDeltaVal.getCpuSizeToBeUsed() != null ? quotaDeltaVal.getCpuSizeToBeUsed() : 0;
                Double memSizeToBeUsed =
                        quotaDeltaVal.getMemSizeToBeUsed() != null ? quotaDeltaVal.getMemSizeToBeUsed() : 0;
                quotaDeltaVal.setCpuSizeToBeUsed(cpuSizeToBeUsed + desiredCpu);
                quotaDeltaVal.setMemSizeToBeUsed(memSizeToBeUsed + desiredMem);
            } else {
                log.warnFormat("Command id {0} already has quota id {1} associated with it with no delta size. The desired delta size will be added to the existing command.",
                        commandId,
                        quotaLimitId);
                quotaDeltaVal.setCpuSizeToBeUsed(desiredCpu);
                quotaDeltaVal.setMemSizeToBeUsed(desiredMem);
            }
        } else {
            addCommandForExistingQuota(commandId, quotaLimitId, 0d, desiredCpu, desiredMem, quotaDeltaMap);
        }
    }

    private static void addCommandForStorageUse(Double desiredSize,
            Guid commandId, Guid quotaLimitId) {
        Map<Guid, QuotaDeltaValue> quotaDeltaMap = getCommandForQuotaId(quotaLimitId);
        if (quotaDeltaMap == null || quotaDeltaMap.isEmpty()) {
            addCommandForNewQuota(commandId, quotaLimitId, desiredSize, 0, 0d);
        }
        QuotaDeltaValue quotaDeltaVal = quotaDeltaMap.get(commandId);
        if (quotaDeltaVal != null) {
            if (quotaDeltaVal.getStorageSizeToBeUsed() != null && quotaDeltaVal.getStorageSizeToBeUsed() > 0) {
                log.warnFormat("Command id {0} already has quota id {1} associated with it with delta size {2}. The desired storage size will be added to the existing delta size.",
                        commandId,
                        quotaLimitId,
                        quotaDeltaVal.getStorageSizeToBeUsed());
                quotaDeltaVal.setStorageSizeToBeUsed(quotaDeltaVal.getStorageSizeToBeUsed() + desiredSize);
            } else {
                log.warnFormat("Command id {0} already has quota id {1} associated with it with no delta size. The desired delta size will be added to the existing command.",
                        commandId,
                        quotaLimitId);
                quotaDeltaVal.setStorageSizeToBeUsed(desiredSize);
            }
        } else {
            addCommandForExistingQuota(commandId, quotaLimitId, desiredSize, 0, 0d, quotaDeltaMap);
        }
    }

    private static void addCommandForNewQuota(Guid commandId,
            Guid quotaLimitId,
            Double desiredStorageSize,
            Integer desiredCPUSize,
            Double desiredMemSize) {
        Map<Guid, QuotaDeltaValue> commandMap = new HashMap<Guid, QuotaDeltaValue>();
        addCommandForExistingQuota(commandId,
                quotaLimitId,
                desiredStorageSize,
                desiredCPUSize,
                desiredMemSize,
                commandMap);
    }

    private static void addCommandForExistingQuota(Guid commandId,
            Guid quotaLimitId,
            Double desiredStorageSize,
            Integer desiredCPUSize,
            Double desiredMemSize,
            Map<Guid, QuotaDeltaValue> commandMap) {
        QuotaDeltaValue quotaStorageDeltaValue =
                new QuotaDeltaValue(desiredStorageSize, desiredCPUSize, desiredMemSize);
        commandMap.put(commandId, quotaStorageDeltaValue);
    }

    private static boolean validateVdsGroupForEnforcedStoragePool(QuotaVdsGroup quotaVdsGroup,
            int desiredCPUSize,
            Double desiredMemorySize,
            double minThresholdVdsGroupPercentage,
            double maxGraceVdsGroupPercentage,
            String quotaName,
            QuotaEnforcmentTypeEnum quotaEnforceType,
            List<String> canDoActionMessages) {
        // if Vds group is unlimited it should be unlimited for both cpu and memory.
        Integer vdsGroupCpuLimit = quotaVdsGroup.getVirtualCpu();
        Long vdsMemSizeLimit = quotaVdsGroup.getMemSizeMB();

        boolean isVdsGroupCpuUnlimited = vdsGroupCpuLimit.intValue() == QuotaHelper.UNLIMITED.intValue();
        boolean isVdsGroupCpuEmpty = vdsGroupCpuLimit.intValue() == QuotaHelper.EMPTY.intValue();
        boolean isVdsGroupMemUnlimited = vdsMemSizeLimit.intValue() == QuotaHelper.UNLIMITED.intValue();
        boolean isVdsGroupMemEmpty = vdsMemSizeLimit.intValue() == QuotaHelper.EMPTY.intValue();

        if (isVdsGroupCpuUnlimited && isVdsGroupMemUnlimited) {
            return true;
        }

        Pair<Integer, Long> totalCpuAndMemLimit = getTotalCpuAndMemForQuotaLimit(quotaVdsGroup.getQuotaVdsGroupId());
        LimitQuotaUsedType limitCpuUsedType;
        LimitQuotaUsedType limitMemUsedType;
        double cpuUsedPercentage = 0;
        double memUsedPercentage = 0;

        // Update limitCpuUsedType for cpu.
        if (isVdsGroupCpuEmpty) {
            limitCpuUsedType = (desiredCPUSize == 0) ? LimitQuotaUsedType.VALID_LIMIT : LimitQuotaUsedType.OFF_LIMIT;
        } else if (isVdsGroupCpuUnlimited) {
            limitCpuUsedType = LimitQuotaUsedType.VALID_LIMIT;
        } else {
            cpuUsedPercentage =
                    (quotaVdsGroup.getVirtualCpuUsage() + quotaVdsGroup.getVirtualCpuUsage()
                            + totalCpuAndMemLimit.getFirst() + desiredCPUSize)
                            / quotaVdsGroup.getVirtualCpu();
            limitCpuUsedType =
                    getLimitQuotaUsedType(minThresholdVdsGroupPercentage, maxGraceVdsGroupPercentage, cpuUsedPercentage);
        }

        // Update limitMemUsedType for memory.
        if (isVdsGroupMemEmpty) {
            limitMemUsedType = (desiredMemorySize == 0) ? LimitQuotaUsedType.VALID_LIMIT : LimitQuotaUsedType.OFF_LIMIT;
        } else if (isVdsGroupMemUnlimited) {
            limitMemUsedType = LimitQuotaUsedType.VALID_LIMIT;
        } else {
            memUsedPercentage =
                    (quotaVdsGroup.getMemSizeMBUsage() + totalCpuAndMemLimit.getSecond() + desiredMemorySize)
                            / quotaVdsGroup.getMemSizeMB();
            limitMemUsedType =
                    getLimitQuotaUsedType(minThresholdVdsGroupPercentage, maxGraceVdsGroupPercentage, memUsedPercentage);
        }

        // Set audit parameters, for any future audit log.
        AuditLogableBase auditLogableBase =
                getLoggableQuotaVdsGroupParams(quotaName, cpuUsedPercentage, memUsedPercentage);

        if (limitCpuUsedType == LimitQuotaUsedType.OFF_LIMIT || limitMemUsedType == LimitQuotaUsedType.OFF_LIMIT) {
            AuditLogDirector.log(auditLogableBase, AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_GRACE_LIMIT);
            if (quotaEnforceType == QuotaEnforcmentTypeEnum.HARD_ENFORCEMENT) {
                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_VDS_GROUP_LIMIT_EXCEEDED.toString());
                return false;
            }
        } else if (limitCpuUsedType == LimitQuotaUsedType.GRACE_LIMIT
                || limitMemUsedType == LimitQuotaUsedType.GRACE_LIMIT) {
            AuditLogDirector.log(auditLogableBase, AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_LIMIT);
        } else if (limitCpuUsedType == LimitQuotaUsedType.THRESHOLD_LIMIT
                || limitMemUsedType == LimitQuotaUsedType.THRESHOLD_LIMIT) {
            AuditLogDirector.log(auditLogableBase, AuditLogType.USER_EXCEEDED_QUOTA_VDS_GROUP_THRESHOLD);
        }
        return true;
    }

    private static boolean validateStorageForEnforcedStoragePool(QuotaStorage quotaStorage,
            Double desiredSizeInGB,
            double minThresholdStoragePercentage,
            double maxGraceStoragePercentage,
            String quotaName,
            QuotaEnforcmentTypeEnum quotaEnforceType,
            Guid commandId,
            List<String> canDoActionMessages) {
        if (quotaStorage.getStorageSizeGB().equals(QuotaHelper.UNLIMITED)) {
            return true;
        }

        // If storage limit is empty, return false if there is a request for any storage resource.
        if (quotaStorage.getStorageSizeGB().equals(QuotaHelper.EMPTY)) {
            return desiredSizeInGB.intValue() == 0;
        }

        double storageUsedPercentage =
                (quotaStorage.getStorageSizeGBUsage()
                        + getTotalStorageDeltaForQuotaLimit(quotaStorage.getQuotaStorageId()) + desiredSizeInGB)
                        / quotaStorage.getStorageSizeGB();

        LimitQuotaUsedType limitStorageUsedType =
                getLimitQuotaUsedType(minThresholdStoragePercentage, maxGraceStoragePercentage, storageUsedPercentage);

        // Set audit parameters, for any future audit log.
        AuditLogableBase auditLogableBase = getLoggableQuotaStorageParams(quotaName, storageUsedPercentage);

        if (limitStorageUsedType == LimitQuotaUsedType.OFF_LIMIT) {
            AuditLogDirector.log(auditLogableBase, AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT);
            if (quotaEnforceType == QuotaEnforcmentTypeEnum.HARD_ENFORCEMENT) {
                canDoActionMessages.add(VdcBllMessages.ACTION_TYPE_FAILED_QUOTA_STORAGE_LIMIT_EXCEEDED.toString());
                return false;
            }
        } else if (limitStorageUsedType == LimitQuotaUsedType.GRACE_LIMIT) {
            AuditLogDirector.log(auditLogableBase, AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT);
        } else if (limitStorageUsedType == LimitQuotaUsedType.THRESHOLD_LIMIT) {
            AuditLogDirector.log(auditLogableBase, AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD);
        }
        return true;
    }

    private static LimitQuotaUsedType getLimitQuotaUsedType(double minThresholdPercentage,
            double maxGracePercentage,
            double usedPercentage) {
        // Checks storage grace and threshold.
        if (usedPercentage > maxGracePercentage) {
            return LimitQuotaUsedType.OFF_LIMIT;
        } else if (usedPercentage > 1) {
            return LimitQuotaUsedType.GRACE_LIMIT;
        } else if (usedPercentage > minThresholdPercentage) {
            return LimitQuotaUsedType.THRESHOLD_LIMIT;
        } else {
            return LimitQuotaUsedType.VALID_LIMIT;
        }
    }

    private static Long getTotalStorageDeltaForQuotaLimit(Guid quotaLimitId) {
        Map<Guid, QuotaDeltaValue> commandsForQuota = getCommandForQuotaId(quotaLimitId);
        long totalStorageUseForQuota = 0l;
        for (Guid quotaDeltaKey : commandsForQuota.keySet()) {
            QuotaDeltaValue quotaDeltaValue = commandsForQuota.get(quotaDeltaKey);
            Double storageDeltaForCommandVal = quotaDeltaValue.getStorageSizeToBeUsed();
            totalStorageUseForQuota += (storageDeltaForCommandVal != null) ? storageDeltaForCommandVal : 0;
        }
        return totalStorageUseForQuota;
    }

    private static Pair<Integer, Long> getTotalCpuAndMemForQuotaLimit(Guid quotaLimitId) {
        Map<Guid, QuotaDeltaValue> commandsForQuota = getCommandForQuotaId(quotaLimitId);
        int totalCpuUseForQuota = 0;
        long totalMemUseForQuota = 0l;
        for (Guid quotaDeltaKey : commandsForQuota.keySet()) {
            QuotaDeltaValue quotaDeltaValue = commandsForQuota.get(quotaDeltaKey);
            Integer cpuDeltaForCommandVal = quotaDeltaValue.getCpuSizeToBeUsed();
            Double memDeltaForCommandVal = quotaDeltaValue.getMemSizeToBeUsed();
            totalCpuUseForQuota += (cpuDeltaForCommandVal != null) ? cpuDeltaForCommandVal : 0;
            totalMemUseForQuota += (memDeltaForCommandVal != null) ? memDeltaForCommandVal : 0;
        }
        Pair<Integer, Long> totalVdsGroupDeltaUsage = new Pair<Integer, Long>(totalCpuUseForQuota, totalMemUseForQuota);
        return totalVdsGroupDeltaUsage;
    }

    private static Map<Guid, QuotaDeltaValue> getCommandForQuotaId(Guid quotaLimitId) {
        Map<Guid, QuotaDeltaValue> commandsForQuota = commandDeltaMap.get(quotaLimitId);
        if (commandsForQuota == null) {
            commandsForQuota = new HashMap<Guid, QuotaDeltaValue>();
            commandDeltaMap.put(quotaLimitId, commandsForQuota);
        }
        return commandsForQuota;
    }

    private static Lock getLockForQuotaId(Guid quotaId) {
        if (quotaSync.get(quotaId) == null) {
            quotaSync.putIfAbsent(quotaId, new ReentrantLock());
        }
        return quotaSync.get(quotaId);
    }

    /**
     * Private class which indicated delta quota usage for command id.
     */
    private static class QuotaDeltaValue {

        Double storageSizeToBeUsed = 0d;
        Integer cpuSizeToBeUsed = 0;
        Double memSizeToBeUsed = 0d;

        public QuotaDeltaValue(Double storageSizeToBeUsed,
                Integer cpuSizeToBeUsed,
                Double memSizeToBeUsed) {
            this.storageSizeToBeUsed = storageSizeToBeUsed;
            this.cpuSizeToBeUsed = cpuSizeToBeUsed;
            this.memSizeToBeUsed = memSizeToBeUsed;
        }

        public Integer getCpuSizeToBeUsed() {
            return cpuSizeToBeUsed;
        }

        public void setCpuSizeToBeUsed(Integer cpuSizeToBeUsed) {
            this.cpuSizeToBeUsed = cpuSizeToBeUsed;
        }

        public Double getMemSizeToBeUsed() {
            return memSizeToBeUsed;
        }

        public void setMemSizeToBeUsed(Double memSizeToBeUsed) {
            this.memSizeToBeUsed = memSizeToBeUsed;
        }

        public Double getStorageSizeToBeUsed() {
            return storageSizeToBeUsed;
        }

        public void setStorageSizeToBeUsed(Double storageSizeToBeUsed) {
            this.storageSizeToBeUsed = storageSizeToBeUsed;
        }
    }

    private static AuditLogableBase getLoggableQuotaStorageParams(String quotaName, Double storageUsagePercentage) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("QuotaName", quotaName);
        logable.AddCustomValue("storageUsage", storageUsagePercentage.toString());
        return logable;
    }

    private static AuditLogableBase getLoggableQuotaVdsGroupParams(String quotaName, Double VCPUPercentage, Double memPercentage) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.AddCustomValue("QuotaName", quotaName);
        logable.AddCustomValue("VCPUPercentage", VCPUPercentage.toString());
        logable.AddCustomValue("memPercentage", memPercentage.toString());
        return logable;
    }


    private static Log log = LogFactory.getLog(QuotaManager.class);
}
