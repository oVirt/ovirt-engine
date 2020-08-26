package org.ovirt.engine.core.bll.quota;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class QuotaManager implements BackendService {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Logger log = LoggerFactory.getLogger(QuotaManager.class);
    private Map<Guid, Map<Guid, Quota>> storagePoolQuotaMap = new HashMap<>();
    private Map<Guid, Guid> storagePoolDefaultQuotaIdMap = new HashMap<>();

    private final List<Integer> nonCountableQutoaVmStatusesList = new ArrayList<>();

    @Inject
    private QuotaDao quotaDao;

    @Inject
    private VmDao vmDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    // constructor is exposed only for Java test. //TODO remove it when arquillian test used.
    protected QuotaManager() {
    }

    @PostConstruct
    private void init() {
        long quotaCacheIntervalInMinutes = Config.<Long>getValue(ConfigValues.QuotaCacheIntervalInMinutes);
        executor.scheduleWithFixedDelay(this::updateQuotaCache,
                1,
                quotaCacheIntervalInMinutes,
                TimeUnit.MINUTES
        );
    }

    /**
     * This method is protected for testing use only
     */
    protected QuotaDao getQuotaDao() {
        return quotaDao;
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
            storagePoolDefaultQuotaIdMap.remove(storagePoolId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void addStoragePoolToCache(Guid storagePoolId) {
        if (storagePoolQuotaMap.containsKey(storagePoolId)) {
            return;
        }

        storagePoolQuotaMap.put(storagePoolId, new HashMap<>());
        Quota defaultQuota = getQuotaDao().getDefaultQuotaForStoragePool(storagePoolId);
        storagePoolDefaultQuotaIdMap.put(storagePoolId, defaultQuota.getId());
    }

    private void addStoragePoolToCacheWithLock(Guid storagePoolId) {
        if (storagePoolQuotaMap.containsKey(storagePoolId)) {
            return;
        }

        lock.writeLock().lock();
        try {
            addStoragePoolToCache(storagePoolId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Roll back quota by VM id. the VM is fetched from DB and the quota is rolled back
     * @param vmId - id for the vm
     */
    public void rollbackQuotaByVmId(Guid vmId) {
        VM vm = vmDao.get(vmId);
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
            return quotaStorageExceeded(quota.getGlobalQuotaStorage());
        }

        // for specific quota
        if (quota.getQuotaStorages() != null) {
            return quota.getQuotaStorages().stream()
                    .anyMatch(this::quotaStorageExceeded);
        }

        return false;
    }

    private boolean quotaStorageExceeded(QuotaStorage quotaStorage) {
        // Treating null limit as unlimited
        if (quotaStorage.getStorageSizeGB() == null) {
            return false;
        }

        return !quotaStorage.getStorageSizeGB().equals(QuotaStorage.UNLIMITED)
                && quotaStorage.getStorageSizeGB() < quotaStorage.getStorageSizeGBUsage();
    }

    /**
     * Consume from quota according to the parameters.
     *
     * @param command - command which consumes the quota
     * @param params  - list of consumption parameters
     * @return - true if the request was validated and set
     */
    public boolean consume(CommandBase<?> command, List<QuotaConsumptionParameter> params) throws InvalidQuotaParametersException {
        StoragePool storagePool = command.getStoragePool();
        if (storagePool == null) {
            throw new InvalidQuotaParametersException("Null storage pool passed to QuotaManager");
        }

        addStoragePoolToCacheWithLock(storagePool.getId());

        QuotaManagerAuditLogger auditLogger = new QuotaManagerAuditLogger(command, auditLogDirector);

        lock.readLock().lock();
        try {
            if (command.getStoragePool().getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
                synchronized (storagePoolQuotaMap.get(storagePool.getId())) {
                    return consumeQuotaParameters(params, command, auditLogger);
                }
            }
        } finally {
            lock.readLock().unlock();
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
        Map<Guid, Quota> quotaMap = storagePoolQuotaMap.get(storagePoolId);

        Quota quota = quotaMap.get(quotaId);
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
                    addStoragePoolToCache(quotaExternal.getStoragePoolId());

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
     * InitializeCache is called by SchedulerUtilQuartzImpl.
     */
    private synchronized void updateQuotaCache() {
        try {
            updateQuotaCacheImpl();
        } catch (Throwable t) {
            log.error("Exception in updating quota cache: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    private synchronized void updateQuotaCacheImpl() {
        if (!isCacheUpdateNeeded()) {
            return;
        }

        log.debug("Updating Quota Cache...");
        long timeStart = System.currentTimeMillis();
        List<Quota> allQuotaIncludingConsumption = getQuotaDao().getAllQuotaIncludingConsumption();

        if (allQuotaIncludingConsumption.isEmpty()) {
            return;
        }

        Map<Guid, Map<Guid, Quota>> newStoragePoolQuotaMap = new HashMap<>();
        Map<Guid, Guid> newDefaultQuotaIdMap = new HashMap<>();

        for (Quota quota : allQuotaIncludingConsumption) {
            if (!newStoragePoolQuotaMap.containsKey(quota.getStoragePoolId())) {
                newStoragePoolQuotaMap.put(quota.getStoragePoolId(), new HashMap<>());
            }
            newStoragePoolQuotaMap.get(quota.getStoragePoolId()).put(quota.getId(), quota);

            if (quota.isDefault()) {
                newDefaultQuotaIdMap.put(quota.getStoragePoolId(), quota.getId());
            }
        }

        lock.writeLock().lock();
        try {
            storagePoolQuotaMap = newStoragePoolQuotaMap;
            storagePoolDefaultQuotaIdMap = newDefaultQuotaIdMap;
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
                if (nonCountableQutoaVmStatusesList.size() == 0) {
                    nonCountableQutoaVmStatusesList.addAll(
                            getQuotaDao().getNonCountableQutoaVmStatuses());
                }
            }
        }
        return !nonCountableQutoaVmStatusesList.contains(status.getValue());
    }

    public Guid getDefaultQuotaId(Guid storagePoolId) {
        if (!storagePoolDefaultQuotaIdMap.containsKey(storagePoolId)) {
            addStoragePoolToCacheWithLock(storagePoolId);
        }

        return storagePoolDefaultQuotaIdMap.get(storagePoolId);
    }

    public Guid getFirstQuotaForUserId(Guid storagePoolId, Guid adElementId) {
        List<Quota> quotas = getQuotaDao().getQuotaByAdElementId(adElementId, storagePoolId, true);
        Guid defaultQuotaId = getDefaultQuotaId(storagePoolId);
        if (quotas.isEmpty()) {
            return defaultQuotaId;
        }
        for (Quota quota : quotas) {
            if (quota.getId().equals(defaultQuotaId)) {
                return defaultQuotaId;
            }
        }
        return quotas.get(0).getId();
    }

    private Guid getDefaultQuotaIfNull(Guid quotaId, Guid storagePoolId) {
        return quotaId != null && !Guid.Empty.equals(quotaId) ?
                quotaId :
                getDefaultQuotaId(storagePoolId);
    }

    public Guid getFirstQuotaForUser(Guid quotaId, Guid storagePoolId, DbUser currentUser) {
        if (currentUser != null) {
            return quotaId != null && !Guid.Empty.equals(quotaId) ?
                    quotaId :
                    getFirstQuotaForUserId(storagePoolId, currentUser.getId());
        }
        return getDefaultQuotaIfNull(quotaId, storagePoolId);
    }

    private boolean consumeQuotaParameters(List<QuotaConsumptionParameter> parameters,
            CommandBase<?> command,
            QuotaManagerAuditLogger auditLogger) {

        boolean hardEnforcement =
                QuotaEnforcementTypeEnum.HARD_ENFORCEMENT == command.getStoragePool().getQuotaEnforcementType();

        // Process the quota consumption parameters to a list of Requests
        // Each Request instance aggregates all requested consumptions against a single quota limit
        Optional<List<Request>> requests = createRequests(parameters, command, hardEnforcement, auditLogger);
        if (!requests.isPresent()) {
            return false;
        }

        // Validate that all requests satisfy the quota limits
        for (Request request : requests.get()) {
            ValidationResult validation = request.validate(hardEnforcement, auditLogger);
            if(!validation.isValid()) {
                command.getReturnValue().getValidationMessages().addAll(validation.getMessagesAsStrings());
                return false;
            }
        }

        // After successful validation, the requests are applied.
        // This changes only the cached quota objects in the QuotaManager, nothing is written to the DB.
        requests.get().forEach(Request::apply);
        return true;
    }

    /**
     * Processes the list of QuotaConsumptionParameter, and creates a list of quota Request objects.
     * Each Request instance aggregates all requests against a singe quota limit.
     * Otherwise the validation would not be correct.
     */
    private Optional<List<Request>> createRequests(List<QuotaConsumptionParameter> parameters,
            CommandBase<?> command,
            boolean hardEnforcement,
            QuotaManagerAuditLogger auditLogger) {

        // The key is: Pair <Quota id, Cluster id>
        Map<Pair<Guid, Guid>, ClusterRequest> clusterRequests = new HashMap<>();

        // The key is: Pair <Quota id, Storage domain id>
        Map<Pair<Guid, Guid>, StorageRequest> storageRequests = new HashMap<>();

        for (QuotaConsumptionParameter param: parameters) {
            // Use default quota if the id is empty
            if(Guid.isNullOrEmpty(param.getQuotaGuid())) {
                param.setQuotaGuid(storagePoolDefaultQuotaIdMap.get(command.getStoragePoolId()));
            }

            Quota quota = fetchQuotaFromCache(param.getQuotaGuid(), command.getStoragePoolId());
            if (quota == null) {
                log.error("The quota id '{}' is not found in backend and DB.", param.getQuotaGuid());
                if (hardEnforcement) {
                    command.getReturnValue().getValidationMessages().add(
                            EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NO_LONGER_AVAILABLE_IN_SYSTEM.toString());
                    command.getReturnValue().getValidationMessages().add(
                            String.format("$VmName %1$s", command.getVmName()));

                    return Optional.empty();
                }

                auditLogger.log(param.getParameterType() == QuotaConsumptionParameter.ParameterType.STORAGE ?
                        AuditLogType.MISSING_QUOTA_STORAGE_PARAMETERS_PERMISSIVE_MODE :
                        AuditLogType.MISSING_QUOTA_CLUSTER_PARAMETERS_PERMISSIVE_MODE);
                continue;
            }

            ValidationResult validation = ValidationResult.VALID;
            if (param instanceof QuotaClusterConsumptionParameter) {
                validation = validateAndAddToClusterRequests((QuotaClusterConsumptionParameter) param,
                        quota,
                        command.getClass().getName(),
                        clusterRequests);
            } else if (param instanceof QuotaStorageConsumptionParameter) {
                validation = validateAndAddToStorageRequests((QuotaStorageConsumptionParameter) param,
                        quota,
                        command.getClass().getName(),
                        storageRequests);
            }

            if (!validation.isValid() && hardEnforcement) {
                command.getReturnValue().getValidationMessages().addAll(validation.getMessagesAsStrings());
                return Optional.empty();
            }
        }

        List<Request> result = new ArrayList<>(clusterRequests.values());
        result.addAll(storageRequests.values());
        return Optional.of(result);
    }

    /**
     * Checks that the QuotaClusterConsumptionParameter is valid for the Quota.
     *
     * If the parameter is valid, it is added to the corresponding Request in the requestMap.
     */
    private ValidationResult validateAndAddToClusterRequests(QuotaClusterConsumptionParameter param,
            Quota quota,
            String commandClassName,
            Map<Pair<Guid, Guid>, ClusterRequest> requestMap) {

        if (param.getClusterId() == null) {
            log.error("Quota Vds parameters from command '{}' are missing vds group id", commandClassName);
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
        }

        Pair<Guid, Guid> key = quota.isGlobalClusterQuota()?
                new Pair<>(quota.getId(), null) :
                new Pair<>(quota.getId(), param.getClusterId());

        if (!requestMap.containsKey(key)) {
            // Quota must be a global cluster quota or be defined for the same cluster as is the consumption parameter.
            QuotaCluster quotaCluster = quota.isGlobalClusterQuota() ?
                    quota.getGlobalQuotaCluster() :
                    quota.getQuotaClusters().stream()
                            .filter(c -> c.getClusterId().equals(param.getClusterId()))
                            .findAny().orElse(null);

            if (quotaCluster == null) {
                log.error("Quota Vds parameters from command '{}'. Vds group does not match quota", commandClassName);
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            }

            requestMap.put(key, new ClusterRequest(quota, quotaCluster));
        }

        // If the quota is released, the values in the request will be negative
        int quotaActionCoef = 1;
        if (param.getQuotaAction() == QuotaConsumptionParameter.QuotaAction.RELEASE) {
            quotaActionCoef = -1;
        }

        // Get the ClusterRequest corresponding to the quota and cluster,
        // and add the new requested memory and cpu.
        ClusterRequest request = requestMap.get(key);
        request.addCpu(quotaActionCoef * param.getRequestedCpu());
        request.addMemory(quotaActionCoef * param.getRequestedMemory());

        return ValidationResult.VALID;
    }

    /**
     * Checks that the QuotaStorageConsumptionParameter is valid for the Quota.
     *
     * If the parameter is valid, it is added to the corresponding Request in the requestMap.
     */
    private ValidationResult validateAndAddToStorageRequests(QuotaStorageConsumptionParameter param,
            Quota quota,
            String commandClassName,
            Map<Pair<Guid, Guid>, StorageRequest> requestMap) {

        if (param.getStorageDomainId() == null) {
            log.error("Quota storage parameters from command '{}' are missing storage domain id", commandClassName);
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
        }

        Pair<Guid, Guid> key = quota.isGlobalStorageQuota() ?
                new Pair<>(quota.getId(), null) :
                new Pair<>(quota.getId(), param.getStorageDomainId());

        if (!requestMap.containsKey(key)) {
            // Quota must be a global storage quota or be defined for
            // the same storage domain as is the consumption parameter.
            QuotaStorage quotaStorage = quota.isGlobalStorageQuota() ?
                    quota.getGlobalQuotaStorage() :
                    quota.getQuotaStorages().stream()
                            .filter(s -> s.getStorageId().equals(param.getStorageDomainId()))
                            .findAny().orElse(null);

            if (quotaStorage == null) {
                log.error("Quota storage parameters from command '{}'. Storage domain does not match quota", commandClassName);
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NO_QUOTA_SET_FOR_DOMAIN);
            }

            requestMap.put(key, new StorageRequest(quota, quotaStorage));
        }

        // If the quota is released, the values in the request will be negative
        int quotaActionCoef = 1;
        if (param.getQuotaAction() == QuotaConsumptionParameter.QuotaAction.RELEASE) {
            quotaActionCoef = -1;
        }

        // Get the StorageRequest corresponding to the quota and storage domain,
        // and add the new requested storage.
        StorageRequest request = requestMap.get(key);
        request.addStorage(quotaActionCoef * param.getRequestedStorageGB());

        return ValidationResult.VALID;
    }

    /**
     * Base class for quota consumption request.
     */
    private abstract class Request {
        private Quota quota;

        protected Request(Quota quota) {
            this.quota = quota;
        }

        public Quota getQuota() {
            return quota;
        }

        /**
         * Validate that the request satisfies quota limits
         */
        public abstract ValidationResult validate(boolean hardEnforcement, QuotaManagerAuditLogger auditLogger);

        /**
         * Apply the request on the current quota in the QuotaManager cache
         */
        public abstract void apply();
    }

    /**
     * Request for cluster quota
     */
    private class ClusterRequest extends Request{
        private QuotaCluster quotaCluster;
        private int coresRequest = 0;
        private long memoryRequestMB = 0L;

        public ClusterRequest(Quota quota, QuotaCluster quotaCluster) {
            super(quota);
            this.quotaCluster = quotaCluster;
        }

        public void addCpu(int cpu) {
            coresRequest += cpu;
        }

        public void addMemory(long memMB) {
            memoryRequestMB += memMB;
        }

        @Override
        public ValidationResult validate(boolean hardEnforcement, QuotaManagerAuditLogger auditLogger) {
            // The ClusterQuota must allow cpu and memory
            if (quotaCluster.getVirtualCpu() == 0 || quotaCluster.getMemSizeMB() == 0) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID);
            }

            int cpuLimit = quotaCluster.getVirtualCpu();
            long memLimit = quotaCluster.getMemSizeMB();

            // Valid if both, CPU and memory, are unlimited
            if (memLimit == QuotaCluster.UNLIMITED_MEM && cpuLimit == QuotaCluster.UNLIMITED_VCPU) {
                return ValidationResult.VALID;
            }

            // Valid if the request releases quota, not consumes it.
            if (coresRequest <=0 && memoryRequestMB <= 0) {
                return ValidationResult.VALID;
            }

            double requestedCoresPercent = 100 * ((double) coresRequest / (double) cpuLimit);
            double currentCoresPercent = 100 * ((double) quotaCluster.getVirtualCpuUsage() / (double) cpuLimit);
            double newCoresPercent = requestedCoresPercent + currentCoresPercent;

            double requestedMemoryPercent = 100 * ((double) memoryRequestMB / (double) memLimit);
            double currentMemoryPercent = 100 * ((double) quotaCluster.getMemSizeMBUsage() / (double) memLimit);
            double newMemoryPercent = requestedMemoryPercent + currentMemoryPercent;

            int threshold = getQuota().getThresholdClusterPercentage();
            int grace = getQuota().getGraceClusterPercentage() + 100;

            // Valid if CPU and memory usages are below grace
            if (newCoresPercent <= grace && newMemoryPercent <= grace) {
                // Warn if the cluster limit or threshold is exceeded
                if (newCoresPercent > 100 || newMemoryPercent > 100) {
                    auditLogger.logClusterLimitExceeded(
                            getQuota().getQuotaName(),
                            getQuota().getId(),
                            (newCoresPercent > 100) ? newCoresPercent : null,
                            (newMemoryPercent > 100) ? newMemoryPercent : null);
                } else if (newCoresPercent > threshold || newMemoryPercent > threshold) {
                    auditLogger.logClusterThresholdExceeded(
                            getQuota().getQuotaName(),
                            getQuota().getId(),
                            (newCoresPercent > threshold) ? newCoresPercent : null,
                            (newMemoryPercent > threshold) ? newMemoryPercent : null);
                }

                return ValidationResult.VALID;
            }

            // CPU or memory is above the grace - fail if enforcement is hard
            auditLogger.logClusterGraceExceeded(
                    getQuota().getQuotaName(),
                    getQuota().getId(),
                    (newCoresPercent > grace) ? currentCoresPercent : null,
                    (newCoresPercent > grace) ? requestedCoresPercent : null,
                    (newMemoryPercent > grace) ? currentMemoryPercent : null,
                    (newMemoryPercent > grace) ? requestedMemoryPercent : null,
                    hardEnforcement);

            if (!hardEnforcement) {
                return ValidationResult.VALID;
            }
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_CLUSTER_LIMIT_EXCEEDED);
        }

        @Override
        public void apply() {
            quotaCluster.setVirtualCpuUsage(coresRequest);
            quotaCluster.setMemSizeMBUsage(memoryRequestMB);
        }
    }

    /**
     * Request for storage quota
     */
    private class StorageRequest extends Request {
        private QuotaStorage quotaStorage;
        private double storageRequestGB = 0.0;

        public StorageRequest(Quota quota, QuotaStorage quotaStorage) {
            super(quota);
            this.quotaStorage = quotaStorage;
        }

        public void addStorage(double storageGB) {
            storageRequestGB += storageGB;
        }

        @Override
        public ValidationResult validate(boolean hardEnforcement, QuotaManagerAuditLogger auditLogger) {
            long storageLimit = quotaStorage.getStorageSizeGB();

            // Valid if quota is unlimited
            if (storageLimit == QuotaStorage.UNLIMITED) {
                return ValidationResult.VALID;
            }

            // Valid if the request releases quota, not consumes it.
            if (storageRequestGB <= 0) {
                return ValidationResult.VALID;
            }

            double requestStoragePercent = 100 * (storageRequestGB / (double) storageLimit);
            double currentStoragePercent = 100 * (quotaStorage.getStorageSizeGBUsage() / (double) storageLimit);
            double newStoragePercent = currentStoragePercent + requestStoragePercent;

            int threshold = getQuota().getThresholdStoragePercentage();
            int grace = getQuota().getGraceStoragePercentage() + 100;

            // Valid if below grace
            if (newStoragePercent <= grace) {
                // Warn if storage limit or threshold is exceeded
                if (newStoragePercent > 100) {
                    auditLogger.logStorageLimitExceeded(
                            getQuota().getQuotaName(),
                            getQuota().getId(),
                            newStoragePercent);
                } else if (newStoragePercent > threshold) {
                    auditLogger.logStorageThresholdExceeded(
                            getQuota().getQuotaName(),
                            getQuota().getId(),
                            newStoragePercent);
                }

                return ValidationResult.VALID;
            }

            // Storage is above the grace - fail if hard enforcement
            auditLogger.logStorageGraceExceeded(
                    getQuota().getQuotaName(),
                    getQuota().getId(),
                    currentStoragePercent,
                    requestStoragePercent,
                    hardEnforcement);

            if (!hardEnforcement) {
                return ValidationResult.VALID;
            }
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QUOTA_STORAGE_LIMIT_EXCEEDED);
        }

        @Override
        public void apply() {
            quotaStorage.setStorageSizeGBUsage(quotaStorage.getStorageSizeGBUsage() + storageRequestGB);
        }
    }
}
