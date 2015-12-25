package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaUsagePerUser;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class QuotaManager implements BackendService {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Logger log = LoggerFactory.getLogger(QuotaManager.class);
    private HashMap<Guid, Map<Guid, Quota>> storagePoolQuotaMap = new HashMap<>();

    private final QuotaManagerAuditLogger quotaManagerAuditLogger = new QuotaManagerAuditLogger();
    private final List<QuotaConsumptionParameter> corruptedParameters = new ArrayList<>();
    private final List<Integer> nonCountableQutoaVmStatusesList = new ArrayList<>();
    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

    // constructor is exposed only for Java test. //TODO remove it when arquillian test used.
    protected QuotaManager() {
    }

    @PostConstruct
    private void init() {
        int quotaCacheIntervalInMinutes = Config.<Integer>getValue(ConfigValues.QuotaCacheIntervalInMinutes);
        schedulerUtil.scheduleAFixedDelayJob(
                this,
                "updateQuotaCache",
                new Class[] {},
                new Object[] {},
                1,
                quotaCacheIntervalInMinutes,
                TimeUnit.MINUTES
        );
    }

    protected QuotaManagerAuditLogger getQuotaManagerAuditLogger() {
        return quotaManagerAuditLogger;
    }

    /**
     * This method is protected for testing use only
     */
    protected QuotaDao getQuotaDao() {
        return DbFacade.getInstance().getQuotaDao();
    }

    public void removeQuotaFromCache(Guid storagePoolId, List<Guid> quotaList) {
        lock.writeLock().lock();
        try {
            if (!storagePoolQuotaMap.containsKey(storagePoolId)) {
                return;
            }
            synchronized (storagePoolQuotaMap.get(storagePoolId)) {
                Map<Guid, Quota> map = storagePoolQuotaMap.get(storagePoolId);
                for (Guid quotaId : quotaList) {
                    map.remove(quotaId);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeQuotaFromCache(Guid storagePoolId, Guid quotaId) {
        removeQuotaFromCache(storagePoolId, Arrays.asList(quotaId));
    }

    public void removeStoragePoolFromCache(Guid storagePoolId) {
        lock.writeLock().lock();
        try {
            storagePoolQuotaMap.remove(storagePoolId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean validateAndSetStorageQuotaHelper(QuotaConsumptionParametersWrapper parameters,
            Pair<AuditLogType, AuditLogableBase> auditLogPair) {
        Map<Guid, Quota> quotaMap = storagePoolQuotaMap.get(parameters.getStoragePoolId());
        Map<Guid, Map<Guid, Double>> desiredStorageSizeQuotaMap = new HashMap<>();

        Map<Guid, Double> newUsedGlobalStorageSize = new HashMap<>();
        Map<Guid, Map<Guid, Double>> newUsedSpecificStorageSize = new HashMap<>();

        generateDesiredStorageSizeQuotaMap(parameters, desiredStorageSizeQuotaMap);

        for (Guid quotaId : desiredStorageSizeQuotaMap.keySet()) {
            Quota quota = quotaMap.get(quotaId);
            if (quota.getGlobalQuotaStorage() != null) {
                if (!checkConsumptionForGlobalStorageQuota(parameters,
                        desiredStorageSizeQuotaMap,
                        newUsedGlobalStorageSize,
                        quotaId,
                        quota,
                        auditLogPair)) {
                    return false;
                }
            } else {
                if (!checkConsumptionForSpecificStorageQuota(parameters,
                        desiredStorageSizeQuotaMap,
                        newUsedSpecificStorageSize,
                        quotaId,
                        quota,
                        auditLogPair)) {
                    return false;
                }
            }
        }
        saveNewConsumptionValues(quotaMap, newUsedGlobalStorageSize, newUsedSpecificStorageSize);
        return true;
    }

    private boolean checkConsumptionForSpecificStorageQuota(QuotaConsumptionParametersWrapper parameters,
            Map<Guid, Map<Guid, Double>> desiredStorageSizeQuotaMap,
            Map<Guid, Map<Guid, Double>> newUsedSpecificStorageSize,
            Guid quotaId,
            Quota quota,
            Pair<AuditLogType, AuditLogableBase> auditLogPair) {
        newUsedSpecificStorageSize.put(quotaId, new HashMap<>());
        for (Guid storageId : desiredStorageSizeQuotaMap.get(quotaId).keySet()) {
            boolean hasStorageId = false;
            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                if (quotaStorage.getStorageId().equals(storageId)) {
                    hasStorageId = true;
                    if (!QuotaStorage.UNLIMITED.equals(quotaStorage.getStorageSizeGB())) {
                        double storageUsagePercentage = quotaStorage.getStorageSizeGBUsage()
                                / quotaStorage.getStorageSizeGB() * 100;
                        double storageRequestPercentage =
                                desiredStorageSizeQuotaMap.get(quotaId)
                                        .get(storageId)
                                        / quotaStorage.getStorageSizeGB() * 100;

                        if (!checkQuotaStorageLimits(parameters.getAuditLogable().getStoragePool().getQuotaEnforcementType(),
                                quota,
                                quotaStorage.getStorageSizeGB(),
                                storageUsagePercentage, storageRequestPercentage,
                                parameters.getValidationMessages(),
                                auditLogPair)) {
                            return false;
                        }
                        newUsedSpecificStorageSize.get(quotaId).put(storageId,
                                quotaStorage.getStorageSizeGBUsage()
                                        + desiredStorageSizeQuotaMap.get(quotaId).get(storageId));
                    }
                }
            }
            if (!hasStorageId){
                if(quota.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT){
                    parameters.getValidationMessages()
                    .add(EngineMessage.ACTION_TYPE_FAILED_NO_QUOTA_SET_FOR_DOMAIN.toString());
                    return false;
                } else {
                    auditLogPair.setFirst(AuditLogType.MISSING_QUOTA_STORAGE_PARAMETERS_PERMISSIVE_MODE);
                }
            }
        }
        return true;
    }

    private boolean checkConsumptionForGlobalStorageQuota(QuotaConsumptionParametersWrapper parameters,
            Map<Guid, Map<Guid, Double>> desiredStorageSizeQuotaMap,
            Map<Guid, Double> newUsedGlobalStorageSize,
            Guid quotaId,
            Quota quota,
            Pair<AuditLogType, AuditLogableBase> auditLogPair) {
        if (!QuotaStorage.UNLIMITED.equals(quota.getGlobalQuotaStorage().getStorageSizeGB())) {
            double sum = 0.0;
            for (Double size : desiredStorageSizeQuotaMap.get(quotaId).values()) {
                sum += size;
            }

            double storageUsagePercentage = quota.getGlobalQuotaStorage().getStorageSizeGBUsage()
                    / quota.getGlobalQuotaStorage().getStorageSizeGB() * 100;
            double storageRequestPercentage = sum
                    / quota.getGlobalQuotaStorage().getStorageSizeGB() * 100;

            if (!checkQuotaStorageLimits(parameters.getAuditLogable().getStoragePool().getQuotaEnforcementType(),
                    quota,
                    quota.getGlobalQuotaStorage().getStorageSizeGB(),
                    storageUsagePercentage, storageRequestPercentage,
                    parameters.getValidationMessages(),
                    auditLogPair)) {
                return false;
            }
            newUsedGlobalStorageSize.put(quotaId, sum
                    + quota.getGlobalQuotaStorage().getStorageSizeGBUsage());
        }
        return true;
    }

    private void saveNewConsumptionValues(Map<Guid, Quota> quotaMap,
            Map<Guid, Double> newUsedGlobalStorageSize,
            Map<Guid, Map<Guid, Double>> newUsedSpecificStorageSize) {
        // cache new storage size.
        for (Map.Entry<Guid, Double> entry : newUsedGlobalStorageSize.entrySet()) {
            Quota quota = quotaMap.get(entry.getKey());
            double value = entry.getValue();
            if (value < 0) {
                log.error("Quota id '{}' cached storage size is negative, removing from cache", entry.getKey());
                quotaMap.remove(entry.getKey());
                continue;
            }
            quota.getGlobalQuotaStorage().setStorageSizeGBUsage(value);
        }
        for (Map.Entry<Guid, Map<Guid, Double>> quotaStorageEntry : newUsedSpecificStorageSize.entrySet()) {
            Quota quota = quotaMap.get(quotaStorageEntry.getKey());
            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                if (quotaStorageEntry.getValue().containsKey(quotaStorage.getStorageId())) {
                    double value = quotaStorageEntry.getValue()
                            .get(quotaStorage.getStorageId());
                    if (value < 0) {
                        log.error("Quota id '{}' cached storage size is negative, removing from cache",
                                quotaStorageEntry.getKey());
                        quotaMap.remove(quotaStorageEntry.getKey());
                        continue;
                    }
                    quotaStorage.setStorageSizeGBUsage(value);
                }
            }
        }
    }

    private void generateDesiredStorageSizeQuotaMap(QuotaConsumptionParametersWrapper parameters,
            Map<Guid, Map<Guid, Double>> desiredStorageSizeQuotaMap) {

        for (QuotaConsumptionParameter param : parameters.getParameters()) {
            QuotaStorageConsumptionParameter storageConsumptionParameter;
            if (param.getParameterType() != QuotaConsumptionParameter.ParameterType.STORAGE) {
                continue;
            } else {
                storageConsumptionParameter = (QuotaStorageConsumptionParameter)param;
            }
            if (!desiredStorageSizeQuotaMap.containsKey(param.getQuotaGuid())) {
                desiredStorageSizeQuotaMap.put(param.getQuotaGuid(), new HashMap<>());
            }
            Map<Guid, Double> quotaStorageMap = desiredStorageSizeQuotaMap.get(param.getQuotaGuid());
            if (!quotaStorageMap.containsKey(storageConsumptionParameter.getStorageDomainId())) {
                quotaStorageMap.put(storageConsumptionParameter.getStorageDomainId(), 0.0);
            }

            double requestedStorage =
                    storageConsumptionParameter.getQuotaAction() == QuotaConsumptionParameter.QuotaAction.CONSUME ?
                            storageConsumptionParameter.getRequestedStorageGB() :
                            -storageConsumptionParameter.getRequestedStorageGB();
            double currentValue = quotaStorageMap.get(storageConsumptionParameter.getStorageDomainId());

            quotaStorageMap.put(storageConsumptionParameter.getStorageDomainId(), currentValue + requestedStorage);
        }
    }

    private boolean checkQuotaStorageLimits(QuotaEnforcementTypeEnum quotaEnforcementTypeEnum,
            Quota quota,
            double limit,
            double storageUsagePercentage,
            double storageRequestPercentage,
            List<String> validationMessages,
            Pair<AuditLogType, AuditLogableBase> log) {
        double storageTotalPercentage = storageUsagePercentage + storageRequestPercentage;

        boolean requestIsApproved;
        if (limit == QuotaStorage.UNLIMITED
                || storageTotalPercentage <= quota.getThresholdStoragePercentage()
                || storageRequestPercentage <= 0) {
            requestIsApproved = true;
        } else if (storageTotalPercentage <= 100) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_THRESHOLD);
            quotaManagerAuditLogger.addCustomValuesStorage(log.getSecond(),
                    quota.getQuotaName(),
                    quota.getId(),
                    storageUsagePercentage + storageRequestPercentage,
                    storageRequestPercentage);
            requestIsApproved = true;
        } else if (storageTotalPercentage <= quota.getGraceStoragePercentage() + 100) {
            log.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_LIMIT);
            quotaManagerAuditLogger.addCustomValuesStorage(log.getSecond(),
                    quota.getQuotaName(),
                    quota.getId(),
                    storageUsagePercentage + storageRequestPercentage,
                    storageRequestPercentage);
            requestIsApproved = true;
        } else {
            log.setFirst(quotaEnforcementTypeEnum == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT ?
                    AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT :
                    AuditLogType.USER_EXCEEDED_QUOTA_STORAGE_GRACE_LIMIT_PERMISSIVE_MODE);
            quotaManagerAuditLogger.addCustomValuesStorage(log.getSecond(),
                    quota.getQuotaName(),
                    quota.getId(),
                    storageUsagePercentage,
                    storageRequestPercentage);
            if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT == quotaEnforcementTypeEnum) {
                validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_STORAGE_LIMIT_EXCEEDED.toString());
                requestIsApproved = false;
            } else {
                requestIsApproved = true;
            }
        }

        if (!requestIsApproved) {
            log.getSecond().setQuotaIdForLog(quota.getId());
        }
        return requestIsApproved;
    }

    private boolean checkQuotaClusterLimits(QuotaEnforcementTypeEnum quotaEnforcementTypeEnum,
            Quota quota,
            QuotaCluster quotaCluster,
            long memToAdd,
            int vcpuToAdd,
            List<String> validationMessages,
            Pair<AuditLogType, AuditLogableBase> auditLogPair) {
        if (quotaCluster.getVirtualCpu() == 0 || quotaCluster.getMemSizeMB() == 0) {
            return false;
        }

        double vcpuToAddPercentage = (double) vcpuToAdd / (double) quotaCluster.getVirtualCpu() * 100;
        double vcpuCurrentPercentage =
                (double) quotaCluster.getVirtualCpuUsage() / (double) quotaCluster.getVirtualCpu() * 100;
        double newVcpuPercent = vcpuToAddPercentage + vcpuCurrentPercentage;
        double memToAddPercentage = (double) memToAdd / (double) quotaCluster.getMemSizeMB() * 100;
        double memCurrentPercentage =
                (double) quotaCluster.getMemSizeMBUsage() / (double) quotaCluster.getMemSizeMB() * 100;
        double newMemoryPercent = memToAddPercentage + memCurrentPercentage;
        long newMemory = memToAdd + quotaCluster.getMemSizeMBUsage();
        int newVcpu = vcpuToAdd + quotaCluster.getVirtualCpuUsage();

        long memLimit = quotaCluster.getMemSizeMB();
        int cpuLimit = quotaCluster.getVirtualCpu();
        boolean requestIsApproved;
        if (memLimit == QuotaCluster.UNLIMITED_MEM && cpuLimit == QuotaCluster.UNLIMITED_VCPU) {
            // if both cpu and
            // mem are unlimited
            requestIsApproved = true;
        } else if ((newVcpuPercent <= quota.getThresholdClusterPercentage() // if cpu and mem usages are under the limit
                && newMemoryPercent <= quota.getThresholdClusterPercentage())
                || (vcpuToAdd <= 0 && memToAdd <= 0)) {
            requestIsApproved = true;
        } else if (newVcpuPercent <= 100
                && newMemoryPercent <= 100) { // passed the threshold (not the quota limit)
            auditLogPair.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_THRESHOLD);
            quotaManagerAuditLogger.addCustomValuesCluster(auditLogPair.getSecond(),
                    quota.getQuotaName(),
                    quota.getId(),
                    vcpuCurrentPercentage + vcpuToAddPercentage,
                    vcpuToAddPercentage,
                    memCurrentPercentage + memToAddPercentage,
                    memToAddPercentage,
                    newVcpuPercent > quota.getThresholdClusterPercentage(),
                    newMemoryPercent > quota.getThresholdClusterPercentage());
            requestIsApproved = true;
        } else if (newVcpuPercent <= quota.getGraceClusterPercentage() + 100
                && newMemoryPercent <= quota.getGraceClusterPercentage() + 100) { // passed the quota limit (not the
            // grace)
            auditLogPair.setFirst(AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_LIMIT);
            quotaManagerAuditLogger.addCustomValuesCluster(auditLogPair.getSecond(),
                    quota.getQuotaName(),
                    quota.getId(),
                    vcpuCurrentPercentage + vcpuToAddPercentage,
                    vcpuToAddPercentage,
                    memCurrentPercentage + memToAddPercentage,
                    memToAddPercentage,
                    newVcpuPercent > 100,
                    newMemoryPercent > 100);
            requestIsApproved = true;
        } else {
            auditLogPair.setFirst(quotaEnforcementTypeEnum == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT ?
                    AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT:
                    AuditLogType.USER_EXCEEDED_QUOTA_CLUSTER_GRACE_LIMIT_PERMISSIVE_MODE); // passed the grace
            quotaManagerAuditLogger.addCustomValuesCluster(auditLogPair.getSecond(),
                    quota.getQuotaName(),
                    quota.getId(),
                    vcpuCurrentPercentage,
                    vcpuToAddPercentage,
                    memCurrentPercentage,
                    memToAddPercentage,
                    newVcpuPercent > quota.getGraceClusterPercentage() + 100,
                    newMemoryPercent > quota.getGraceClusterPercentage() + 100);
            if (QuotaEnforcementTypeEnum.HARD_ENFORCEMENT == quotaEnforcementTypeEnum) {
                validationMessages.add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_CLUSTER_LIMIT_EXCEEDED.toString());
                requestIsApproved = false;
            } else {
                requestIsApproved = true;
            }
        }
        // cache
        if(requestIsApproved) {
            cacheNewValues(quotaCluster, newMemory, newVcpu);
        } else {
            auditLogPair.getSecond().setQuotaIdForLog(quota.getId());
        }
        return requestIsApproved;
    }

    private void cacheNewValues(QuotaCluster quotaCluster, long newMemory, int newVcpu) {
        quotaCluster.setVirtualCpuUsage(newVcpu);
        quotaCluster.setMemSizeMBUsage(newMemory);
    }

    private boolean validateAndSetClusterQuota(QuotaConsumptionParametersWrapper parameters,
            Pair<AuditLogType, AuditLogableBase> auditLogPair) {
        boolean result = true;

        List<QuotaClusterConsumptionParameter> executed = new ArrayList<>();
        for (QuotaConsumptionParameter parameter : parameters.getParameters()) {
            QuotaClusterConsumptionParameter clusterConsumptionParameter;
            if (parameter.getParameterType() != QuotaConsumptionParameter.ParameterType.CLUSTER) {
                continue;
            } else {
                clusterConsumptionParameter = (QuotaClusterConsumptionParameter) parameter;
            }
            Quota quota = parameter.getQuota();
            QuotaCluster quotaCluster = null;

            if (quota.getGlobalQuotaCluster() != null) { // global cluster quota
                quotaCluster = quota.getGlobalQuotaCluster();
            } else {
                for (QuotaCluster cluster : quota.getQuotaClusters()) {
                    if (cluster.getClusterId().equals(clusterConsumptionParameter.getClusterId())) {
                        quotaCluster = cluster;
                        break;
                    }
                }
            }
            if (quotaCluster == null) {
                parameters.getValidationMessages()
                        .add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
                result = false;
                break;
            }

            long requestedMemory =
                    clusterConsumptionParameter.getQuotaAction() == QuotaConsumptionParameter.QuotaAction.CONSUME ?
                    clusterConsumptionParameter.getRequestedMemory() : -clusterConsumptionParameter.getRequestedMemory();
            int requestedCpu =
                    clusterConsumptionParameter.getQuotaAction() == QuotaConsumptionParameter.QuotaAction.CONSUME ?
                    clusterConsumptionParameter.getRequestedCpu() : -clusterConsumptionParameter.getRequestedCpu();

            if (checkQuotaClusterLimits(
                    parameters.getAuditLogable().getStoragePool().getQuotaEnforcementType(),
                    quota,
                    quotaCluster,
                    requestedMemory,
                    requestedCpu,
                    parameters.getValidationMessages(),
                    auditLogPair)) {
                executed.add(clusterConsumptionParameter);
            } else {
                result = false;
                break;
            }
        }

        //if result is false (one or more parameters did not pass) - roll back the parameters that did pass
        if(!result) {
            rollBackClusterConsumptionParameters(executed);
        }

        return result;
    }

    private void rollBackClusterConsumptionParameters(List<QuotaClusterConsumptionParameter> executed) {
        for (QuotaClusterConsumptionParameter parameter : executed) {
            long requestedMemory =
                    parameter.getQuotaAction() == QuotaConsumptionParameter.QuotaAction.CONSUME ?
                            -parameter.getRequestedMemory() : parameter.getRequestedMemory();
            int requestedCpu =
                    parameter.getQuotaAction() == QuotaConsumptionParameter.QuotaAction.CONSUME ?
                            -parameter.getRequestedCpu() : parameter.getRequestedCpu();

            QuotaCluster quotaCluster = null;
            Quota quota = parameter.getQuota();
            if (quota.getGlobalQuotaCluster() != null) { // global cluster quota
                quotaCluster = quota.getGlobalQuotaCluster();
            } else {
                for (QuotaCluster cluster : quota.getQuotaClusters()) {
                    if (cluster.getClusterId().equals(parameter.getClusterId())) {
                        quotaCluster = cluster;
                        break;
                    }
                }
            }

            if (quotaCluster != null) {
                long newMemory = requestedMemory + quotaCluster.getMemSizeMBUsage();
                int newVcpu = requestedCpu + quotaCluster.getVirtualCpuUsage();
                cacheNewValues(quotaCluster, newMemory, newVcpu);
            }
        }
    }

    /**
     * Roll back quota by VM id. the VM is fetched from DB and the quota is rolled back
     * @param vmId - id for the vm
     */
    public void rollbackQuotaByVmId(Guid vmId) {
        VM vm = DbFacade.getInstance().getVmDao().get(vmId);
        if (vm != null) {
            removeQuotaFromCache(vm.getStoragePoolId(), vm.getQuotaId());
        }
    }

    /**
     * Check if the quota exceeded the storage limit (ether for global limit or one of the specific limits).
     *
     * @param quotaId
     *            - quota id
     * @return - true if the quota exceeded the storage limitation. false if quota was not found, limit was not defined
     *         or limit not crossed.
     */
    public boolean isStorageQuotaExceeded(Guid quotaId) {
        if (quotaId == null) {
            return false;
        }

        Quota quota = getQuotaDao().getById(quotaId);

        if (quota == null) {
            return false;
        }

        // for global quota
        if (quota.getGlobalQuotaStorage() != null) {
            if (quota.getGlobalQuotaStorage().getStorageSizeGB() != null
                    && !quota.getGlobalQuotaStorage().getStorageSizeGB().equals(QuotaStorage.UNLIMITED)
                    && quota.getGlobalQuotaStorage().getStorageSizeGB()
                    < quota.getGlobalQuotaStorage().getStorageSizeGBUsage()) {
                return true;
            }
        } else if (quota.getQuotaStorages() != null) { // for specific quota
            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                if (quotaStorage.getStorageSizeGB() < quotaStorage.getStorageSizeGBUsage()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Consume from quota according to the parameters.
     *
     * @param parameters
     *            - Quota consumption parameters
     * @return - true if the request was validated and set
     */
    public boolean consume(QuotaConsumptionParametersWrapper parameters) throws InvalidQuotaParametersException {

        Pair<AuditLogType, AuditLogableBase> auditLogPair = new Pair<>();
        auditLogPair.setSecond(parameters.getAuditLogable());

        StoragePool storagePool = parameters.getAuditLogable().getStoragePool();
        if (storagePool == null) {
            throw new InvalidQuotaParametersException("Null storage pool passed to QuotaManager");
        }

        lock.writeLock().lock();
        try {
            if (!storagePoolQuotaMap.containsKey(storagePool.getId())) {
                storagePoolQuotaMap.put(storagePool.getId(), new HashMap<>());
            }
        } finally {
            lock.writeLock().unlock();
        }

        lock.readLock().lock();
        try {
            synchronized (storagePoolQuotaMap.get(storagePool.getId())) {
                return validateAndCompleteParameters(parameters, auditLogPair)
                        && (parameters.getStoragePool().getQuotaEnforcementType() == QuotaEnforcementTypeEnum.DISABLED
                        || internalConsumeAndReleaseHandler(parameters, auditLogPair));
            }
        } finally {
            lock.readLock().unlock();
            getQuotaManagerAuditLogger().auditLog(auditLogPair.getFirst(), auditLogPair.getSecond());
        }
    }

    /**
     * This is the start point for all quota consumption and release. This method is called after the parameters were
     * validated and competed, and the cache was updated to support all the requests in the parameters.
     *
     *
     * @param parameters
     *            - Quota consumption parameters
     * @param auditLogPair - auditLog pair
     * @return - true if the request was validated and set
     */
    private boolean internalConsumeAndReleaseHandler(QuotaConsumptionParametersWrapper parameters, Pair<AuditLogType,
            AuditLogableBase> auditLogPair) {
        boolean result = validateAndSetStorageQuotaHelper(parameters, auditLogPair);
        if (result) {
            result = validateAndSetClusterQuota(parameters, auditLogPair);
            if (result) {
                return true;
            } else {
                QuotaConsumptionParametersWrapper revertedParams = revertParametersQuantities(parameters);
                validateAndSetStorageQuotaHelper(revertedParams, auditLogPair);
            }
        }

        return result;
    }

    /**
     * Revert the quantities of the storage, cpu and mem So that a request for 5GB storage is reverted to (-5)GB request
     *
     * @param parameters
     *            the consumption properties. This object would not be mutated.
     * @return new QuotaConsumptionParameters object with reverted quantities,
     */
    private QuotaConsumptionParametersWrapper revertParametersQuantities(QuotaConsumptionParametersWrapper parameters) {
        QuotaConsumptionParametersWrapper revertedParams = null;
        try {
            revertedParams = parameters.clone();
            for (QuotaConsumptionParameter parameter : revertedParams.getParameters()) {
                parameter.setQuotaAction(QuotaConsumptionParameter.QuotaAction.CONSUME == parameter.getQuotaAction() ?
                        QuotaConsumptionParameter.QuotaAction.RELEASE : QuotaConsumptionParameter.QuotaAction.CONSUME);
            }
        } catch (CloneNotSupportedException ignored) {}

        return revertedParams;
    }

    /**
     * Validate parameters. Look for null pointers and missing data Complete the missing data in the parameters from DB
     * and cache all the needed entities.
     *
     * @param parameters
     *            - Quota consumption parameters
     */

    private boolean validateAndCompleteParameters(QuotaConsumptionParametersWrapper parameters,
            Pair<AuditLogType, AuditLogableBase> auditLogPair) throws InvalidQuotaParametersException {

        if (QuotaEnforcementTypeEnum.DISABLED == parameters.getAuditLogable().getStoragePool().getQuotaEnforcementType()) {
            return true;
        }

        boolean hardEnforcement =
                QuotaEnforcementTypeEnum.HARD_ENFORCEMENT == parameters.getAuditLogable().getStoragePool().getQuotaEnforcementType();

        // for each parameter - check and complete
        for (QuotaConsumptionParameter param : parameters.getParameters()) {
            // check that quota id is valid and fetch the quota from db (or cache). add the quota to the param
            boolean validQuotaId = checkAndFetchQuota(parameters, param, auditLogPair);
            boolean validCluster = true;
            boolean  validStorageDomain = true;

            if (validQuotaId) {
                // In case this param is a QuotaVdsConsumptionParameter - check that it has a valid
                // vds group id which is handled by this quota
                if (param instanceof QuotaClusterConsumptionParameter) {
                    validCluster = checkClusterMatchQuota(parameters, param);
                }

                // In case this param is a QuotaStorageConsumptionParameter - check that it has a valid
                // storage domain id which is handled by this quota
                if (param instanceof QuotaStorageConsumptionParameter) {
                    validStorageDomain = checkStoragePoolMatchQuota(parameters, param);
                }
            }

            if (!validQuotaId || !validCluster || !validStorageDomain) {
                // if in hard enforcement - return false
                if (hardEnforcement) {
                    return false;
                } else {
                    // clear any messages written to the validationMessages
                    parameters.getValidationMessages().clear();
                    if (QuotaEnforcementTypeEnum.DISABLED == parameters.getAuditLogable().getStoragePool().getQuotaEnforcementType()) {
                        auditLogPair.setFirst(null);
                    }
                }
            }
        }
        parameters.getParameters().removeAll(corruptedParameters);
        corruptedParameters.clear();

        return true;
    }

    // check that quota id is valid and fetch the quota from db (or cache). add the quota to the param
    private boolean checkAndFetchQuota(QuotaConsumptionParametersWrapper parameters, QuotaConsumptionParameter param,
            Pair<AuditLogType, AuditLogableBase> auditLogPair)
            throws InvalidQuotaParametersException {
        if(param.getQuotaGuid() == null || Guid.Empty.equals(param.getQuotaGuid())) {
            parameters.getValidationMessages().add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            parameters.getValidationMessages().add(String.format("$VmName %1$s",
                    parameters.getAuditLogable()
                            .getVmName()));
            if (QuotaEnforcementTypeEnum.SOFT_ENFORCEMENT == parameters
                    .getStoragePool()
                    .getQuotaEnforcementType()) {
                auditLogPair.setFirst(param.getParameterType() == QuotaConsumptionParameter.ParameterType.STORAGE ?
                        AuditLogType.MISSING_QUOTA_STORAGE_PARAMETERS_PERMISSIVE_MODE
                        :
                        AuditLogType.MISSING_QUOTA_CLUSTER_PARAMETERS_PERMISSIVE_MODE);
            }
            log.error("No Quota id passed from command '{}'", parameters.getAuditLogable().getClass().getName());
            corruptedParameters.add(param);
            return false;
        }

        Quota quota = fetchQuotaFromCache(param.getQuotaGuid(), parameters.getStoragePool().getId());
        if (quota == null) {
            parameters.getValidationMessages().add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NO_LONGER_AVAILABLE_IN_SYSTEM.toString());
            parameters.getValidationMessages().add(String.format("$VmName %1$s",
                    parameters.getAuditLogable()
                    .getVmName()));
            auditLogPair.setFirst(param.getParameterType() == QuotaConsumptionParameter.ParameterType.STORAGE ?
                    AuditLogType.MISSING_QUOTA_STORAGE_PARAMETERS_PERMISSIVE_MODE :
                    AuditLogType.MISSING_QUOTA_CLUSTER_PARAMETERS_PERMISSIVE_MODE);
            log.error("The quota id '{}' is not found in backend and DB.", param.getQuotaGuid());
            corruptedParameters.add(param);
            return false;
        } else {
            param.setQuota(quota);
        }
        if (!quota.getStoragePoolId().equals(parameters.getStoragePoolId())) {
            throw new InvalidQuotaParametersException("The Quota storage pool id does not match the passed storage pool");
        }
        return true;
    }

    // In case this param is a QuotaVdsConsumptionParameter - check that it has a valid
    // vds group id which is handled by this quota
    private boolean checkClusterMatchQuota(QuotaConsumptionParametersWrapper parameters, QuotaConsumptionParameter param) {
        Quota quota = param.getQuota();
        QuotaClusterConsumptionParameter paramVds = (QuotaClusterConsumptionParameter) param;

        if (paramVds.getClusterId() == null) {
            parameters.getValidationMessages().add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            log.error("Quota Vds parameters from command '{}' are missing vds group id",
                    parameters.getAuditLogable().getClass().getName());
            return false;
        }
        boolean clusterInQuota = false;
        if(quota.getGlobalQuotaCluster() != null) {
            clusterInQuota = true;
        } else {
            for (QuotaCluster cluster : quota.getQuotaClusters()) {
                if (cluster.getClusterId().equals(paramVds.getClusterId())) {
                    clusterInQuota = true;
                    break;
                }
            }
        }

        if (!clusterInQuota) {
            parameters.getValidationMessages().add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            log.error("Quota Vds parameters from command '{}'. Vds group does not match quota",
                    parameters.getAuditLogable().getClass().getName());
            return false;
        }
        return true;
    }

    // In case this param is a QuotaStorageConsumptionParameter - check that it has a valid
    // storage domain id which is handled by this quota
    private boolean checkStoragePoolMatchQuota(QuotaConsumptionParametersWrapper parameters, QuotaConsumptionParameter param) {
        Quota quota = param.getQuota();
        QuotaStorageConsumptionParameter paramStorage = (QuotaStorageConsumptionParameter) param;

        if (paramStorage.getStorageDomainId() == null) {
            parameters.getValidationMessages().add(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID.toString());
            log.error("Quota storage parameters from command '{}' are missing storage domain id",
                    parameters.getAuditLogable().getClass().getName());
            return false;
        }
        boolean storageDomainInQuota = false;
        if(quota.getGlobalQuotaStorage() != null) {
            storageDomainInQuota = true;
        } else {
            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                if (quotaStorage.getStorageId().equals(paramStorage.getStorageDomainId())) {
                    storageDomainInQuota = true;
                    break;
                }
            }
        }

        if (!storageDomainInQuota) {
            parameters.getValidationMessages().add(EngineMessage.ACTION_TYPE_FAILED_NO_QUOTA_SET_FOR_DOMAIN.toString());
            log.error("Quota storage parameters from command '{}'. Storage domain does not match quota",
                    parameters.getAuditLogable().getClass().getName());
            return false;
        }
        return true;
    }

    /**
     * Get Quota by Id. If in cache - get from cache. else get from Dao and add to cache.
     *
     * @param quotaId - quota id
     * @param storagePoolId - storage pool containing this quota
     * @return - found quota. null if not found.
     */
    private Quota fetchQuotaFromCache(Guid quotaId, Guid storagePoolId) throws InvalidQuotaParametersException {
        Quota quota;


        Map<Guid, Quota> quotaMap = storagePoolQuotaMap.get(storagePoolId);

        quota = quotaMap.get(quotaId);
        // if quota was not found in cache - look for it in DB
        if (quota == null) {
            quota = getQuotaDao().getById(quotaId);
            if (quota != null) {
                // cache in quota map
                if (storagePoolId.equals(quota.getStoragePoolId())) {
                    quotaMap.put(quotaId, quota);
                } else {
                    throw new InvalidQuotaParametersException(
                            String.format("Quota %s does not match storage pool %s", quotaId.toString()
                                    , storagePoolId.toString()));
                }
            }
        }
        return quota;
    }

    /**
     * REturn a list of QuotaUsagePerUser representing the status of all the quotas in quotaIdsList
     *
     * @param quotaList
     *            quota list
     */
    public void updateUsage(List<Quota> quotaList) {
        List<Quota> needToCache = new ArrayList<>();

        if (quotaList == null) {
            return;
        }

        lock.readLock().lock();
        try {
            for (Quota quotaExternal : quotaList) {
                // look for the quota in the cache
                Map<Guid, Quota> quotaMap = storagePoolQuotaMap.get(quotaExternal.getStoragePoolId());
                Quota quota = null;
                if (quotaMap != null) {
                    quota = quotaMap.get(quotaExternal.getId());
                }

                // if quota not in cache look for it in DB and add it to cache
                if (quota == null) {
                    needToCache.add(quotaExternal);
                } else {
                    copyUsageData(quota, quotaExternal);
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        // if some of the quota are not in cache and need to be cached
        if (!needToCache.isEmpty()) {
            lock.writeLock().lock();
            try {
                for (Quota quotaExternal : needToCache) {
                    if (!storagePoolQuotaMap.containsKey(quotaExternal.getStoragePoolId())) {
                        storagePoolQuotaMap.put(quotaExternal.getStoragePoolId(), new HashMap<>());
                    }
                    Quota quota = fetchQuotaFromCache(quotaExternal.getId(), quotaExternal.getStoragePoolId());
                    if (quota != null) {
                        copyUsageData(quota, quotaExternal);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    private void copyUsageData(Quota quota, Quota quotaExternal) {
        if (quota.getGlobalQuotaStorage() != null) {
            quotaExternal.setGlobalQuotaStorage(copyQuotaStorageUsage(quota.getGlobalQuotaStorage()));
        }
        if (quota.getGlobalQuotaCluster() != null) {
            quotaExternal.setGlobalQuotaCluster(copyQuotaClusterUsage(quota.getGlobalQuotaCluster()));
        }

        if (quota.getQuotaStorages() != null) {
            quotaExternal.setQuotaStorages(new ArrayList<>());
            for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                quotaExternal.getQuotaStorages().add(copyQuotaStorageUsage(quotaStorage));
            }
        }

        if (quota.getQuotaClusters() != null) {
            quotaExternal.setQuotaClusters(new ArrayList<>());
            for (QuotaCluster quotaCluster : quota.getQuotaClusters()) {
                quotaExternal.getQuotaClusters().add(copyQuotaClusterUsage(quotaCluster));
            }
        }
    }

    private QuotaStorage copyQuotaStorageUsage(QuotaStorage quotaStorage) {
        return new QuotaStorage(null, null, null,
                quotaStorage.getStorageSizeGB(),
                quotaStorage.getStorageSizeGBUsage());
    }

    private QuotaCluster copyQuotaClusterUsage(QuotaCluster quotaCluster) {
        return new QuotaCluster(null, null, null,
                quotaCluster.getVirtualCpu(),
                quotaCluster.getVirtualCpuUsage(),
                quotaCluster.getMemSizeMB(),
                quotaCluster.getMemSizeMBUsage());
    }

    /**
     * Return a list of QuotaUsagePerUser representing the status of all the quotas available for a specific user
     *
     *
     * @param quotaIdsList
     *            - quotas available for user
     * @return - list of QuotaUsagePerUser
     */
    public Map<Guid, QuotaUsagePerUser> generatePerUserUsageReport(List<Quota> quotaIdsList) {
        Map<Guid, QuotaUsagePerUser> quotaPerUserUsageEntityMap = new HashMap<>();
        List<Quota> needToCache = new ArrayList<>();

        if (quotaIdsList != null) {
            lock.readLock().lock();
            try {
                for (Quota quotaExternal : quotaIdsList) {
                    // look for the quota in the cache
                    Map<Guid, Quota> quotaMap = storagePoolQuotaMap.get(quotaExternal.getStoragePoolId());
                    Quota quota = null;
                    if (quotaMap != null) {
                        quota = quotaMap.get(quotaExternal.getId());
                    }

                    // if quota not in cache look for it in DB and add it to cache
                    if (quota == null) {
                        needToCache.add(quotaExternal);
                    } else {
                        QuotaUsagePerUser usagePerUser = addQuotaEntry(quota);
                        if (usagePerUser != null) {
                            quotaPerUserUsageEntityMap.put(quota.getId(), usagePerUser);
                        }
                    }
                }
            } finally {
                lock.readLock().unlock();
            }

            if (!needToCache.isEmpty()) {
                lock.writeLock().lock();
                try {
                    for (Quota quotaExternal : needToCache) {
                        // look for the quota in the cache again (it may have been added by now)
                        if (!storagePoolQuotaMap.containsKey(quotaExternal.getStoragePoolId())) {
                            storagePoolQuotaMap.put(quotaExternal.getStoragePoolId(), new HashMap<>());
                        }
                        Quota quota = fetchQuotaFromCache(quotaExternal.getId(), quotaExternal.getStoragePoolId());

                        QuotaUsagePerUser usagePerUser = addQuotaEntry(quota);
                        if (usagePerUser != null) {
                            quotaPerUserUsageEntityMap.put(quota.getId(), usagePerUser);
                        }
                    }
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }

        return quotaPerUserUsageEntityMap;
    }

    private QuotaUsagePerUser addQuotaEntry(Quota quota) {
        // if quota is not null (found in cache or DB) - add entry to quotaPerUserUsageEntityMap
        if (quota != null) {
            long storageLimit = 0;
            double storageUsage = 0;
            int cpuLimit = 0;
            int cpuUsage = 0;
            long memLimit = 0;
            long memUsage = 0;

            // calc storage
            if (quota.getGlobalQuotaStorage() != null) {
                storageLimit = quota.getGlobalQuotaStorage().getStorageSizeGB();
                storageUsage = quota.getGlobalQuotaStorage().getStorageSizeGBUsage();
            } else {
                for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
                    // once storage was set unlimited it will remain so
                    if (QuotaStorage.UNLIMITED.equals(quotaStorage.getStorageSizeGB())) {
                        storageLimit = QuotaStorage.UNLIMITED; // Do not break because usage is still counting
                    }
                    if (storageLimit != QuotaStorage.UNLIMITED) {
                        storageLimit += quotaStorage.getStorageSizeGB();
                    }
                    storageUsage += quotaStorage.getStorageSizeGBUsage();
                }
            }

            // calc cpu and mem
            if (quota.getGlobalQuotaCluster() != null) {
                memLimit = quota.getGlobalQuotaCluster().getMemSizeMB();
                memUsage = quota.getGlobalQuotaCluster().getMemSizeMBUsage();
                cpuLimit = quota.getGlobalQuotaCluster().getVirtualCpu();
                cpuUsage = quota.getGlobalQuotaCluster().getVirtualCpuUsage();
            } else {
                for (QuotaCluster quotaCluster : quota.getQuotaClusters()) {

                    // once mem was set unlimited it will remain so
                    if (QuotaCluster.UNLIMITED_MEM.equals(quotaCluster.getMemSizeMB())) {
                        memLimit = QuotaCluster.UNLIMITED_MEM; // Do not break because usage is still counting
                    }
                    if (memLimit != QuotaCluster.UNLIMITED_MEM) {
                        memLimit += quotaCluster.getMemSizeMB();
                    }

                    // once cpu was set unlimited it will remain so
                    if (QuotaCluster.UNLIMITED_VCPU.equals(quotaCluster.getVirtualCpu())) {
                        cpuLimit = QuotaCluster.UNLIMITED_VCPU; // Do not break because usage is still counting
                    }
                    if (cpuLimit != QuotaCluster.UNLIMITED_VCPU) {
                        cpuLimit += quotaCluster.getVirtualCpu();
                    }

                    memUsage += quotaCluster.getMemSizeMBUsage();
                    cpuUsage += quotaCluster.getVirtualCpuUsage();
                }
            }

            return new QuotaUsagePerUser(quota.getId(),
                    quota.getQuotaName(),
                    storageLimit,
                    storageUsage,
                    cpuLimit,
                    cpuUsage,
                    memLimit,
                    memUsage);
        }
        return null;
    }

    /**
     * InitializeCache is called by SchedulerUtilQuartzImpl.
     */
    @OnTimerMethodAnnotation("updateQuotaCache")
    public synchronized void updateQuotaCache() {
        if (!isCacheUpdateNeeded()) {
            return;
        }

        log.debug("Updating Quota Cache...");
        long timeStart = System.currentTimeMillis();
        List<Quota> allQuotaIncludingConsumption = getQuotaDao().getAllQuotaIncludingConsumption();

        if (allQuotaIncludingConsumption.isEmpty()) {
            return;
        }

        HashMap<Guid, Map<Guid, Quota>> newStoragePoolQuotaMap = new HashMap<>();
        for (Quota quota : allQuotaIncludingConsumption) {
            if (!newStoragePoolQuotaMap.containsKey(quota.getStoragePoolId())) {
                newStoragePoolQuotaMap.put(quota.getStoragePoolId(), new HashMap<>());
            }
            newStoragePoolQuotaMap.get(quota.getStoragePoolId()).put(quota.getId(), quota);
        }

        lock.writeLock().lock();
        try {
            storagePoolQuotaMap = newStoragePoolQuotaMap;
        } finally {
            lock.writeLock().unlock();
        }
        long timeEnd = System.currentTimeMillis();
        log.info("Quota Cache updated. ({} msec)", timeEnd-timeStart);
    }

    public boolean isCacheUpdateNeeded() {
        int quotaCount = getQuotaDao().getQuotaCount();
        int cacheCount = 0;

        lock.readLock().lock();
        try {
            for(Map<Guid, Quota> quotaMap : storagePoolQuotaMap.values()) {
                cacheCount += quotaMap.size();
            }
        } finally {
            lock.readLock().unlock();
        }

        return cacheCount < quotaCount * Config.<Integer> getValue(ConfigValues.MinimumPercentageToUpdateQuotaCache)/100;
    }

    public boolean isVmStatusQuotaCountable(VMStatus status) {
        if (nonCountableQutoaVmStatusesList.size() == 0) {
            synchronized (nonCountableQutoaVmStatusesList) {
                nonCountableQutoaVmStatusesList.addAll(DbFacade.getInstance()
                        .getQuotaDao()
                        .getNonCountableQutoaVmStatuses());
            }
        }
        return !nonCountableQutoaVmStatusesList.contains(status.getValue());
    }
}
