package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.bll.network.host.VfScheduler;
import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerDiscovery;
import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerFactory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingCpuCores;
import org.ovirt.engine.core.bll.scheduling.pending.PendingMemory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingOvercommitMemory;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.pending.PendingVM;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SchedulingManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingManager.class);
    private static final String HIGH_UTILIZATION = "HighUtilization";
    private static final String LOW_UTILIZATION = "LowUtilization";

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private MigrationHandler migrationHandler;
    @Inject
    private ExternalSchedulerDiscovery exSchedulerDiscovery;
    @Inject
    private DbFacade dbFacade;
    @Inject
    private NetworkDeviceHelper networkDeviceHelper;
    @Inject
    private HostDeviceManager hostDeviceManager;

    private PendingResourceManager pendingResourceManager;

    /**
     * [policy id, policy] map
     */
    private final ConcurrentHashMap<Guid, ClusterPolicy> policyMap;
    /**
     * [policy unit id, policy unit] map
     */
    private volatile ConcurrentHashMap<Guid, PolicyUnitImpl> policyUnits;

    private final Object policyUnitsLock = new Object();

    private final ConcurrentHashMap<Guid, Semaphore> clusterLockMap = new ConcurrentHashMap<>();

    private final VdsFreeMemoryChecker noWaitingMemoryChecker = new VdsFreeMemoryChecker(new NonWaitingDelayer());

    private final Map<Guid, Boolean> clusterId2isHaReservationSafe = new HashMap<>();

    private PendingResourceManager getPendingResourceManager() {
        return pendingResourceManager;
    }

    @Inject
    private SchedulingManager() {
        policyMap = new ConcurrentHashMap<>();
        policyUnits = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        log.info("Initializing Scheduling manager");
        initializePendingResourceManager();
        loadPolicyUnits();
        loadClusterPolicies();
        loadExternalScheduler();
        enableLoadBalancer();
        enableHaReservationCheck();
        log.info("Initialized Scheduling manager");
    }

    private void initializePendingResourceManager() {
        pendingResourceManager = new PendingResourceManager(resourceManager);
    }

    private void loadExternalScheduler() {
        if (Config.<Boolean>getValue(ConfigValues.ExternalSchedulerEnabled)) {
            log.info("Starting external scheduler discovery thread");
            ThreadPoolUtil.execute(() -> {
                if (exSchedulerDiscovery.discover()) {
                    reloadPolicyUnits();
                }
            });
        } else {
            exSchedulerDiscovery.markAllExternalPoliciesAsDisabled();
            log.info("External scheduler disabled, discovery skipped");
        }
    }

    private void reloadPolicyUnits() {
        synchronized (policyUnitsLock) {
            policyUnits = new ConcurrentHashMap<>();
            loadPolicyUnits();
        }
    }

    public List<ClusterPolicy> getClusterPolicies() {
        return new ArrayList<>(policyMap.values());
    }

    public ClusterPolicy getClusterPolicy(Guid clusterPolicyId) {
        return policyMap.get(clusterPolicyId);
    }

    public ClusterPolicy getClusterPolicy(String name) {
        if (name == null || name.isEmpty()) {
            return getDefaultClusterPolicy();
        }
        for (ClusterPolicy clusterPolicy : policyMap.values()) {
            if (clusterPolicy.getName().toLowerCase().equals(name.toLowerCase())) {
                return clusterPolicy;
            }
        }
        return null;
    }

    private ClusterPolicy getDefaultClusterPolicy() {
        for (ClusterPolicy clusterPolicy : policyMap.values()) {
            if (clusterPolicy.isDefaultPolicy()) {
                return clusterPolicy;
            }
        }
        return null;
    }

    public Map<Guid, PolicyUnitImpl> getPolicyUnitsMap() {
        synchronized (policyUnitsLock) {
            return policyUnits;
        }
    }

    private void loadClusterPolicies() {
        // Load internal cluster policies
        policyMap.putAll(InternalClusterPolicies.getClusterPolicies());

        Map<Guid, PolicyUnitType> internalTypes = new HashMap<>();
        for (PolicyUnitImpl unit: policyUnits.values()) {
            internalTypes.put(unit.getGuid(), unit.getType());
        }

        // Get all user provided cluster policies
        List<ClusterPolicy> allClusterPolicies = getClusterPolicyDao().getAll(
                Collections.unmodifiableMap(internalTypes));
        for (ClusterPolicy clusterPolicy : allClusterPolicies) {
            policyMap.put(clusterPolicy.getId(), clusterPolicy);
        }
    }

    private void loadPolicyUnits() {
        // Load internal policy units
        for (Class<? extends PolicyUnitImpl> unitType: InternalPolicyUnits.getList()) {
            PolicyUnitImpl unit = InternalPolicyUnits.instantiate(unitType, getPendingResourceManager());
            if (unit != null) {
                policyUnits.put(unit.getGuid(), Injector.injectMembers(unit));
            } else {
                log.error("Could not instantiate a policy unit {}.", unitType.getName());
            }
        }

        // Load all external policy units
        List<PolicyUnit> allPolicyUnits = getPolicyUnitDao().getAll();
        for (PolicyUnit policyUnit : allPolicyUnits) {
            policyUnits.put(policyUnit.getId(), new ExternalPolicyUnit(policyUnit, getPendingResourceManager()));
        }
    }

    private static class SchedulingResult {
        Map<Guid, Pair<EngineMessage, String>> filteredOutReasons;
        Map<Guid, String> hostNames;
        PerHostMessages details;

        public SchedulingResult() {
            filteredOutReasons = new HashMap<>();
            hostNames = new HashMap<>();
            details = new PerHostMessages();
        }

        public void addReason(Guid id, String hostName, EngineMessage filterType, String filterName) {
            filteredOutReasons.put(id, new Pair<>(filterType, filterName));
            hostNames.put(id, hostName);
        }

        public Collection<String> getReasonMessages() {
            List<String> lines = new ArrayList<>();

            for (Entry<Guid, Pair<EngineMessage, String>> line: filteredOutReasons.entrySet()) {
                lines.add(line.getValue().getFirst().name());
                lines.add(String.format("$%1$s %2$s", "hostName", hostNames.get(line.getKey())));
                lines.add(String.format("$%1$s %2$s", "filterName", line.getValue().getSecond()));

                final List<String> detailMessages = details.getMessages(line.getKey());
                if (detailMessages == null || detailMessages.isEmpty()) {
                    lines.add(EngineMessage.SCHEDULING_HOST_FILTERED_REASON.name());
                }
                else {
                    lines.addAll(detailMessages);
                    lines.add(EngineMessage.SCHEDULING_HOST_FILTERED_REASON_WITH_DETAIL.name());
                }
            }

            return lines;
        }

        private PerHostMessages getDetails() {
            return details;
        }

    }

    public Guid schedule(Cluster cluster,
            VM vm,
            List<Guid> hostBlackList,
            List<Guid> hostWhiteList,
            List<Guid> destHostIdList,
            List<String> messages,
            VdsFreeMemoryChecker memoryChecker,
            String correlationId) {
        prepareClusterLock(cluster.getId());
        try {
            log.debug("Scheduling started, correlation Id: {}", correlationId);
            checkAllowOverbooking(cluster);
            lockCluster(cluster.getId());
            List<VDS> vdsList = getVdsDao()
                    .getAllForClusterWithStatus(cluster.getId(), VDSStatus.Up);
            updateInitialHostList(vdsList, hostBlackList, true);
            updateInitialHostList(vdsList, hostWhiteList, false);
            refreshCachedPendingValues(vdsList);
            ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
            Map<String, String> parameters = createClusterPolicyParameters(cluster);

            vdsList =
                    runFilters(policy.getFilters(),
                            cluster,
                            vdsList,
                            vm,
                            parameters,
                            policy.getFilterPositionMap(),
                            messages,
                            memoryChecker,
                            true,
                            correlationId);

            if (vdsList == null || vdsList.isEmpty()) {
                return null;
            }

            Guid bestHost = selectBestHost(cluster, vm, destHostIdList, vdsList, policy, parameters);

            if (bestHost != null) {
                getPendingResourceManager().addPending(new PendingCpuCores(bestHost, vm, vm.getNumOfCpus()));

                VDS bestHostEntity = vdsList.stream().filter(new Predicate<VDS>() {
                    @Override
                    public boolean test(VDS vds) {
                        return vds.getId().equals(bestHost);
                    }
                }).findFirst().get();

                getPendingResourceManager().addPending(new PendingMemory(bestHost, vm, bestHostEntity.getGuestOverhead()));
                getPendingResourceManager().addPending(new PendingOvercommitMemory(bestHost, vm, vm.getMemSizeMb()));
                getPendingResourceManager().addPending(new PendingVM(bestHost, vm));
                getPendingResourceManager().notifyHostManagers(bestHost);

                VfScheduler vfScheduler = Injector.get(VfScheduler.class);
                Map<Guid, String> passthroughVnicToVfMap = vfScheduler.getVnicToVfMap(vm.getId(), bestHost);
                if (passthroughVnicToVfMap != null && !passthroughVnicToVfMap.isEmpty()) {
                    markVfsAsUsedByVm(bestHost, vm.getId(), passthroughVnicToVfMap);
                }
            }

            return bestHost;
        } catch (InterruptedException e) {
            log.error("interrupted", e);
            return null;
        } finally {
            releaseCluster(cluster.getId());

            log.debug("Scheduling ended, correlation Id: {}", correlationId);
        }
    }

    private void releaseCluster(Guid cluster) {
        // ensuring setting the semaphore permits to 1
        synchronized (clusterLockMap.get(cluster)) {
            clusterLockMap.get(cluster).drainPermits();
            clusterLockMap.get(cluster).release();
        }
    }

    private void lockCluster(Guid cluster) throws InterruptedException {
        clusterLockMap.get(cluster).acquire();
    }

    private void prepareClusterLock(Guid cluster) {
        clusterLockMap.putIfAbsent(cluster, new Semaphore(1));
    }

    private void markVfsAsUsedByVm(Guid hostId, Guid vmId, Map<Guid, String> passthroughVnicToVfMap) {
        try {
            hostDeviceManager.acquireHostDevicesLock(hostId);
            networkDeviceHelper.setVmIdOnVfs(hostId, vmId, new HashSet<>(passthroughVnicToVfMap.values()));
        } finally {
            hostDeviceManager.releaseHostDevicesLock(hostId);
        }
    }

    /**
     * Refresh cached VDS pending fields with the current pending
     * values from PendingResourceManager.
     * @param vdsList - list of candidate hosts
     */
    private void refreshCachedPendingValues(List<VDS> vdsList) {
        for (VDS vds: vdsList) {
            int pendingMemory = PendingOvercommitMemory.collectForHost(getPendingResourceManager(), vds.getId());
            int pendingCpuCount = PendingCpuCores.collectForHost(getPendingResourceManager(), vds.getId());

            vds.setPendingVcpusCount(pendingCpuCount);
            vds.setPendingVmemSize(pendingMemory);
        }
    }

    /**
     * @param destHostIdList - used for RunAt preselection, overrides the ordering in vdsList
     * @param availableVdsList - presorted list of hosts (better hosts first) that are available
     */
    private Guid selectBestHost(Cluster cluster,
            VM vm,
            List<Guid> destHostIdList,
            List<VDS> availableVdsList,
            ClusterPolicy policy,
            Map<String, String> parameters) {
        // in case a default destination host was specified and
        // it passed filters, return the first found
        List<VDS> runnableHosts = new LinkedList<>();
        if (destHostIdList.size() > 0) {
            // there are dedicated hosts
            // intersect dedicated hosts list with available list
            for (VDS vds : availableVdsList) {
                for (Guid destHostId : destHostIdList) {
                    if (destHostId.equals(vds.getId())) {
                        runnableHosts.add(vds);
                    }
                }
            }
        }
        if (runnableHosts.isEmpty()) { // no dedicated hosts found
            runnableHosts = availableVdsList;
        }

        switch (runnableHosts.size()){
        case 0:
            // no runnable hosts found, nothing found
            return null;
        case 1:
            // found single available host, in available list return it
            return runnableHosts.get(0).getId();
        default:
            // select best runnable host with scoring functions (from policy)
            List<Pair<Guid, Integer>> functions = policy.getFunctions();
            if (functions != null && !functions.isEmpty()
                    && shouldWeighClusterHosts(cluster, runnableHosts)) {
                Guid bestHostByFunctions = runFunctions(functions, cluster, runnableHosts, vm, parameters);
                if (bestHostByFunctions != null) {
                    return bestHostByFunctions;
                }
            }
        }
        // failed select best runnable host using scoring functions, return the first
        return runnableHosts.get(0).getId();
    }

    /**
     * Checks whether scheduler should schedule several requests in parallel:
     * Conditions:
     * * config option SchedulerAllowOverBooking should be enabled.
     * * cluster optimization type flag should allow over-booking.
     * * more than than X (config.SchedulerOverBookingThreshold) pending for scheduling.
     * In case all of the above conditions are met, we release all the pending scheduling
     * requests.
     */
    private void checkAllowOverbooking(Cluster cluster) {
        if (OptimizationType.ALLOW_OVERBOOKING == cluster.getOptimizationType()
                && Config.<Boolean>getValue(ConfigValues.SchedulerAllowOverBooking)
                && clusterLockMap.get(cluster.getId()).getQueueLength() >=
                Config.<Integer>getValue(ConfigValues.SchedulerOverBookingThreshold)) {
            log.info("Scheduler: cluster '{}' lock is skipped (cluster is allowed to overbook)",
                    cluster.getName());
            // release pending threads (requests) and current one (+1)
            clusterLockMap.get(cluster.getId())
                    .release(Config.<Integer>getValue(ConfigValues.SchedulerOverBookingThreshold) + 1);
        }
    }

    /**
     * Checks whether scheduler should weigh hosts/or skip weighing:
     * * More than one host (it's trivial to weigh a single host).
     * * optimize for speed is enabled for the cluster, and there are less than configurable requests pending (skip
     * weighing in a loaded setup).
     */
    private boolean shouldWeighClusterHosts(Cluster cluster, List<VDS> vdsList) {
        Integer threshold = Config.<Integer>getValue(ConfigValues.SpeedOptimizationSchedulingThreshold);
        // threshold is crossed only when cluster is configured for optimized for speed
        boolean crossedThreshold =
                OptimizationType.OPTIMIZE_FOR_SPEED == cluster.getOptimizationType()
                        && clusterLockMap.get(cluster.getId()).getQueueLength() >
                        threshold;
        if (crossedThreshold) {
            log.info(
                    "Scheduler: skipping whinging hosts in cluster '{}', since there are more than '{}' parallel requests",
                    cluster.getName(),
                    threshold);
        }
        return vdsList.size() > 1
                && !crossedThreshold;
    }

    public boolean canSchedule(Cluster cluster,
            VM vm,
            List<Guid> vdsBlackList,
            List<Guid> vdsWhiteList,
            List<Guid> destVdsIdList,
            List<String> messages) {
        List<VDS> vdsList = getVdsDao()
                .getAllForClusterWithStatus(cluster.getId(), VDSStatus.Up);
        updateInitialHostList(vdsList, vdsBlackList, true);
        updateInitialHostList(vdsList, vdsWhiteList, false);
        refreshCachedPendingValues(vdsList);
        ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
        Map<String, String> parameters = createClusterPolicyParameters(cluster);

        vdsList =
                runFilters(policy.getFilters(),
                        cluster,
                        vdsList,
                        vm,
                        parameters,
                        policy.getFilterPositionMap(),
                        messages,
                        noWaitingMemoryChecker,
                        false,
                        null);

        return vdsList != null && !vdsList.isEmpty();
    }

    private Map<String, String> createClusterPolicyParameters(Cluster cluster) {
        Map<String, String> parameters = new HashMap<>();
        if (cluster.getClusterPolicyProperties() != null) {
            parameters.putAll(cluster.getClusterPolicyProperties());
        }
        return parameters;
    }

    private void updateInitialHostList(List<VDS> vdsList, List<Guid> list, boolean contains) {
        if (list != null && !list.isEmpty()) {
            List<VDS> toRemoveList = new ArrayList<>();
            Set<Guid> listSet = new HashSet<>(list);
            for (VDS vds : vdsList) {
                if (listSet.contains(vds.getId()) == contains) {
                    toRemoveList.add(vds);
                }
            }
            vdsList.removeAll(toRemoveList);
        }
    }

    private List<VDS> runFilters(ArrayList<Guid> filters,
            Cluster cluster,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            Map<Guid, Integer> filterPositionMap,
            List<String> messages,
            VdsFreeMemoryChecker memoryChecker,
            boolean shouldRunExternalFilters,
            String correlationId) {
        SchedulingResult result = new SchedulingResult();
        ArrayList<PolicyUnitImpl> internalFilters = new ArrayList<>();
        ArrayList<PolicyUnitImpl> externalFilters = new ArrayList<>();
        filters = (filters != null) ? new ArrayList<>(filters) : new ArrayList<>();

        sortFilters(filters, filterPositionMap);
        for (Guid filter : filters) {
            PolicyUnitImpl filterPolicyUnit = policyUnits.get(filter);
            if (filterPolicyUnit.getPolicyUnit().isInternal()) {
                internalFilters.add(filterPolicyUnit);
            } else {
                if (filterPolicyUnit.getPolicyUnit().isEnabled()) {
                    externalFilters.add(filterPolicyUnit);
                }
            }
        }

        /* Short circuit filters if there are no hosts at all */
        if (hostList == null || hostList.isEmpty()) {
            messages.add(EngineMessage.SCHEDULING_NO_HOSTS.name());
            messages.addAll(result.getReasonMessages());
            return hostList;
        }

        hostList =
                runInternalFilters(internalFilters, cluster, hostList, vm, parameters, filterPositionMap,
                        memoryChecker, correlationId, result);

        if (shouldRunExternalFilters
                && Config.<Boolean>getValue(ConfigValues.ExternalSchedulerEnabled)
                && !externalFilters.isEmpty()
                && hostList != null
                && !hostList.isEmpty()) {
            hostList = runExternalFilters(externalFilters, hostList, vm, parameters, messages, correlationId, result);
        }

        if (hostList == null || hostList.isEmpty()) {
            messages.add(EngineMessage.SCHEDULING_ALL_HOSTS_FILTERED_OUT.name());
            messages.addAll(result.getReasonMessages());
        }
        return hostList;
    }

    private List<VDS> runInternalFilters(ArrayList<PolicyUnitImpl> filters,
            Cluster cluster,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            Map<Guid, Integer> filterPositionMap,
            VdsFreeMemoryChecker memoryChecker,
            String correlationId, SchedulingResult result) {
        if (filters != null) {
            for (PolicyUnitImpl filterPolicyUnit : filters) {
                if (hostList == null || hostList.isEmpty()) {
                    break;
                }
                filterPolicyUnit.setMemoryChecker(memoryChecker);
                List<VDS> currentHostList = new ArrayList<>(hostList);
                hostList = filterPolicyUnit.filter(cluster, hostList, vm, parameters, result.getDetails());
                logFilterActions(currentHostList,
                        toIdSet(hostList),
                        EngineMessage.VAR__FILTERTYPE__INTERNAL,
                        filterPolicyUnit.getPolicyUnit().getName(),
                        result,
                        correlationId);
            }
        }
        return hostList;
    }

    private Set<Guid> toIdSet(List<VDS> hostList) {
        Set<Guid> set = new HashSet<>();
        if (hostList != null) {
            for (VDS vds : hostList) {
                set.add(vds.getId());
            }
        }
        return set;
    }

    private void logFilterActions(List<VDS> oldList,
                                  Set<Guid> newSet,
                                  EngineMessage actionName,
                                  String filterName,
                                  SchedulingResult result,
                                  String correlationId) {
        for (VDS host: oldList) {
            if (!newSet.contains(host.getId())) {
                result.addReason(host.getId(), host.getName(), actionName, filterName);
                log.info("Candidate host '{}' ('{}') was filtered out by '{}' filter '{}' (correlation id: {})",
                        host.getName(),
                        host.getId(),
                        actionName.name(),
                        filterName,
                        correlationId);
            }
        }
    }

    private List<VDS> runExternalFilters(ArrayList<PolicyUnitImpl> filters,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            List<String> messages,
            String correlationId, SchedulingResult result) {
        List<Guid> filteredIDs = null;
        if (filters != null) {
            List<String> filterNames = new ArrayList<>();
            for (PolicyUnitImpl filter : filters) {
                filterNames.add(filter.getPolicyUnit().getName());
            }
            List<Guid> hostIDs = new ArrayList<>();
            for (VDS host : hostList) {
                hostIDs.add(host.getId());
            }

            filteredIDs =
                    ExternalSchedulerFactory.getInstance().runFilters(filterNames, hostIDs, vm.getId(), parameters);
            if (filteredIDs != null) {
                logFilterActions(hostList,
                        new HashSet<>(filteredIDs),
                        EngineMessage.VAR__FILTERTYPE__EXTERNAL,
                        Arrays.toString(filterNames.toArray()),
                        result,
                        correlationId);
            }
        }

        return intersectHosts(hostList, filteredIDs);
    }

    private List<VDS> intersectHosts(List<VDS> hosts, List<Guid> IDs) {
        if (IDs == null) {
            return hosts;
        }
        List<VDS> retList = new ArrayList<>();
        for (VDS vds : hosts) {
            if (IDs.contains(vds.getId())) {
                retList.add(vds);
            }
        }
        return retList;
    }

    private void sortFilters(ArrayList<Guid> filters, final Map<Guid, Integer> filterPositionMap) {
        if (filterPositionMap == null) {
            return;
        }
        Collections.sort(filters, new Comparator<Guid>() {
            @Override
            public int compare(Guid filter1, Guid filter2) {
                Integer position1 = getPosition(filterPositionMap.get(filter1));
                Integer position2 = getPosition(filterPositionMap.get(filter2));
                return position1 - position2;
            }

            private Integer getPosition(Integer position) {
                if (position == null) {
                    position = 0;
                }
                return position;
            }
        });
    }

    private Guid runFunctions(List<Pair<Guid, Integer>> functions,
            Cluster cluster,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters) {
        List<Pair<PolicyUnitImpl, Integer>> internalScoreFunctions = new ArrayList<>();
        List<Pair<PolicyUnitImpl, Integer>> externalScoreFunctions = new ArrayList<>();

        for (Pair<Guid, Integer> pair : functions) {
            PolicyUnitImpl currentPolicy = policyUnits.get(pair.getFirst());
            if (currentPolicy.getPolicyUnit().isInternal()) {
                internalScoreFunctions.add(new Pair<>(currentPolicy, pair.getSecond()));
            } else {
                if (currentPolicy.getPolicyUnit().isEnabled()) {
                    externalScoreFunctions.add(new Pair<>(currentPolicy, pair.getSecond()));
                }
            }
        }

        Map<Guid, Integer> hostCostTable = runInternalFunctions(internalScoreFunctions, cluster, hostList, vm,
                parameters);

        if (Config.<Boolean>getValue(ConfigValues.ExternalSchedulerEnabled) && !externalScoreFunctions.isEmpty()) {
            runExternalFunctions(externalScoreFunctions, hostList, vm, parameters, hostCostTable);
        }
        Entry<Guid, Integer> bestHostEntry = null;
        for (Entry<Guid, Integer> entry : hostCostTable.entrySet()) {
            if (bestHostEntry == null || bestHostEntry.getValue() > entry.getValue()) {
                bestHostEntry = entry;
            }
        }
        if (bestHostEntry == null) {
            return null;
        }
        return bestHostEntry.getKey();
    }

    private Map<Guid, Integer> runInternalFunctions(List<Pair<PolicyUnitImpl, Integer>> functions,
            Cluster cluster,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters) {
        Map<Guid, Integer> hostCostTable = new HashMap<>();
        for (Pair<PolicyUnitImpl, Integer> pair : functions) {
            List<Pair<Guid, Integer>> scoreResult = pair.getFirst().score(cluster, hostList, vm, parameters);
            for (Pair<Guid, Integer> result : scoreResult) {
                Guid hostId = result.getFirst();
                if (hostCostTable.get(hostId) == null) {
                    hostCostTable.put(hostId, 0);
                }
                hostCostTable.put(hostId,
                        hostCostTable.get(hostId) + pair.getSecond() * result.getSecond());
            }
        }
        return hostCostTable;
    }

    private void runExternalFunctions(List<Pair<PolicyUnitImpl, Integer>> functions,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            Map<Guid, Integer> hostCostTable) {
        List<Pair<String, Integer>> scoreNameAndWeight = new ArrayList<>();
        for (Pair<PolicyUnitImpl, Integer> pair : functions) {
            scoreNameAndWeight.add(new Pair<>(pair.getFirst().getPolicyUnit().getName(),
                    pair.getSecond()));
        }

        List<Guid> hostIDs = new ArrayList<>();
        for (VDS vds : hostList) {
            hostIDs.add(vds.getId());
        }
        List<Pair<Guid, Integer>> externalScores =
                ExternalSchedulerFactory.getInstance().runScores(scoreNameAndWeight,
                        hostIDs,
                        vm.getId(),
                        parameters);
        if (externalScores != null) {
            sumScoreResults(hostCostTable, externalScores);
        }
    }

    private void sumScoreResults(Map<Guid, Integer> hostCostTable, List<Pair<Guid, Integer>> externalScores) {
        if (externalScores == null) {
            // the external scheduler proxy may return null if error happens, in this case the external scores will
            // remain empty
            log.warn("External scheduler proxy returned null score");
        } else {
            for (Pair<Guid, Integer> pair : externalScores) {
                Guid hostId = pair.getFirst();
                if (hostCostTable.get(hostId) == null) {
                    hostCostTable.put(hostId, 0);
                }
                hostCostTable.put(hostId,
                        hostCostTable.get(hostId) + pair.getSecond());
            }
        }
    }

    public Map<String, String> getCustomPropertiesRegexMap(ClusterPolicy clusterPolicy) {
        Set<Guid> usedPolicyUnits = new HashSet<>();
        if (clusterPolicy.getFilters() != null) {
            usedPolicyUnits.addAll(clusterPolicy.getFilters());
        }
        if (clusterPolicy.getFunctions() != null) {
            for (Pair<Guid, Integer> pair : clusterPolicy.getFunctions()) {
                usedPolicyUnits.add(pair.getFirst());
            }
        }
        if (clusterPolicy.getBalance() != null) {
            usedPolicyUnits.add(clusterPolicy.getBalance());
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (Guid policyUnitId : usedPolicyUnits) {
            map.putAll(policyUnits.get(policyUnitId).getPolicyUnit().getParameterRegExMap());
        }
        return map;
    }

    public void addClusterPolicy(ClusterPolicy clusterPolicy) {
        getClusterPolicyDao().save(clusterPolicy);
        policyMap.put(clusterPolicy.getId(), clusterPolicy);
    }

    public void editClusterPolicy(ClusterPolicy clusterPolicy) {
        getClusterPolicyDao().update(clusterPolicy);
        policyMap.put(clusterPolicy.getId(), clusterPolicy);
    }

    public void removeClusterPolicy(Guid clusterPolicyId) {
        getClusterPolicyDao().remove(clusterPolicyId);
        policyMap.remove(clusterPolicyId);
    }

    private VdsDao getVdsDao() {
        return dbFacade.getVdsDao();
    }

    private ClusterDao getClusterDao() {
        return dbFacade.getClusterDao();
    }

    private PolicyUnitDao getPolicyUnitDao() {
        return dbFacade.getPolicyUnitDao();
    }

    private ClusterPolicyDao getClusterPolicyDao() {
        return dbFacade.getClusterPolicyDao();
    }

    private void enableLoadBalancer() {
        if (Config.<Boolean>getValue(ConfigValues.EnableVdsLoadBalancing)) {
            log.info("Start scheduling to enable vds load balancer");
            Injector.get(SchedulerUtilQuartzImpl.class).scheduleAFixedDelayJob(
                    this,
                    "performLoadBalancing",
                    new Class[] {},
                    new Object[] {},
                    Config.<Integer>getValue(ConfigValues.VdsLoadBalancingIntervalInMinutes),
                    Config.<Integer>getValue(ConfigValues.VdsLoadBalancingIntervalInMinutes),
                    TimeUnit.MINUTES);
            log.info("Finished scheduling to enable vds load balancer");
        }
    }

    private void enableHaReservationCheck() {

        if (Config.<Boolean>getValue(ConfigValues.EnableVdsLoadBalancing)) {
            log.info("Start HA Reservation check");
            Integer interval = Config.<Integer> getValue(ConfigValues.VdsHaReservationIntervalInMinutes);
            Injector.get(SchedulerUtilQuartzImpl.class).scheduleAFixedDelayJob(
                    this,
                    "performHaResevationCheck",
                    new Class[] {},
                    new Object[] {},
                    interval,
                    interval,
                    TimeUnit.MINUTES);
            log.info("Finished HA Reservation check");
        }

    }

    @OnTimerMethodAnnotation("performHaResevationCheck")
    public void performHaResevationCheck() {

        log.debug("HA Reservation check timer entered.");
        List<Cluster> clusters = getClusterDao().getAll();
        if (clusters != null) {
            HaReservationHandling haReservationHandling = new HaReservationHandling(getPendingResourceManager());
            for (Cluster cluster : clusters) {
                if (cluster.supportsHaReservation()) {
                    List<VDS> returnedFailedHosts = new ArrayList<>();
                    boolean clusterHaStatus =
                            haReservationHandling.checkHaReservationStatusForCluster(cluster, returnedFailedHosts);
                    if (!clusterHaStatus) {
                        // create Alert using returnedFailedHosts
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setClusterId(cluster.getId());
                        logable.addCustomValue("ClusterName", cluster.getName());

                        String failedHostsStr =
                                returnedFailedHosts.stream().map(VDS::getName).collect(Collectors.joining(", "));

                        logable.addCustomValue("Hosts", failedHostsStr);
                        AlertDirector.alert(logable, AuditLogType.CLUSTER_ALERT_HA_RESERVATION, auditLogDirector);
                        log.info("Cluster '{}' fail to pass HA reservation check.", cluster.getName());
                    }

                    boolean clusterHaStatusFromPreviousCycle =
                            clusterId2isHaReservationSafe.containsKey(cluster.getId()) ? clusterId2isHaReservationSafe.get(cluster.getId())
                                    : true;

                    // Update the status map with the new status
                    clusterId2isHaReservationSafe.put(cluster.getId(), clusterHaStatus);

                    // Create Alert if the status was changed from false to true
                    if (!clusterHaStatusFromPreviousCycle && clusterHaStatus) {
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setClusterId(cluster.getId());
                        logable.addCustomValue("ClusterName", cluster.getName());
                        AlertDirector.alert(logable, AuditLogType.CLUSTER_ALERT_HA_RESERVATION_DOWN, auditLogDirector);
                    }
                }
            }
        }
        log.debug("HA Reservation check timer finished.");
    }

    @OnTimerMethodAnnotation("performLoadBalancing")
    public void performLoadBalancing() {
        log.debug("Load Balancer timer entered.");
        List<Cluster> clusters = getClusterDao().getAll();
        for (Cluster cluster : clusters) {
            ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
            PolicyUnitImpl policyUnit = policyUnits.get(policy.getBalance());
            Pair<List<Guid>, Guid> balanceResult = null;
            if (policyUnit.getPolicyUnit().isEnabled()) {
                List<VDS> hosts = getVdsDao().getAllForClusterWithoutMigrating(cluster.getId());
                if (policyUnit.getPolicyUnit().isInternal()) {
                    balanceResult = internalRunBalance(policyUnit, cluster, hosts);
                } else if (Config.<Boolean> getValue(ConfigValues.ExternalSchedulerEnabled)) {
                    balanceResult = externalRunBalance(policyUnit, cluster, hosts);
                }
            }

            if (balanceResult != null && balanceResult.getSecond() != null) {
                migrationHandler.migrateVM(balanceResult.getFirst(), balanceResult.getSecond());
            }
        }
    }

    private Pair<List<Guid>, Guid> internalRunBalance(PolicyUnitImpl policyUnit, Cluster cluster, List<VDS> hosts) {
        return policyUnit.balance(cluster,
                hosts,
                cluster.getClusterPolicyProperties(),
                new ArrayList<>());
    }

    private Pair<List<Guid>, Guid> externalRunBalance(PolicyUnitImpl policyUnit, Cluster cluster, List<VDS> hosts) {
        List<Guid> hostIDs = new ArrayList<>();
        for (VDS vds : hosts) {
            hostIDs.add(vds.getId());
        }
        return ExternalSchedulerFactory.getInstance()
                .runBalance(policyUnit.getPolicyUnit().getName(), hostIDs, cluster.getClusterPolicyProperties());
    }

    /**
     * returns all cluster policies names containing the specific policy unit.
     * @return List of cluster policy names that use the referenced policyUnitId
     *         or null if the policy unit is not available.
     */
    public List<String> getClusterPoliciesNamesByPolicyUnitId(Guid policyUnitId) {
        List<String> list = new ArrayList<>();
        final PolicyUnitImpl policyUnitImpl = policyUnits.get(policyUnitId);
        if (policyUnitImpl == null) {
            log.warn("Trying to find usages of non-existing policy unit '{}'", policyUnitId);
            return null;
        }

        PolicyUnit policyUnit = policyUnitImpl.getPolicyUnit();
        if (policyUnit != null) {
            for (ClusterPolicy clusterPolicy : policyMap.values()) {
                switch (policyUnit.getPolicyUnitType()) {
                case FILTER:
                    Collection<Guid> filters = clusterPolicy.getFilters();
                    if (filters != null && filters.contains(policyUnitId)) {
                        list.add(clusterPolicy.getName());
                    }
                    break;
                case WEIGHT:
                    Collection<Pair<Guid, Integer>> functions = clusterPolicy.getFunctions();
                    if (functions == null) {
                        break;
                    }
                    for (Pair<Guid, Integer> pair : functions) {
                        if (pair.getFirst().equals(policyUnitId)) {
                            list.add(clusterPolicy.getName());
                            break;
                        }
                    }
                    break;
                case LOAD_BALANCING:
                    if (policyUnitId.equals(clusterPolicy.getBalance())) {
                        list.add(clusterPolicy.getName());
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return list;
    }

    public void removeExternalPolicyUnit(Guid policyUnitId) {
        getPolicyUnitDao().remove(policyUnitId);
        policyUnits.remove(policyUnitId);
    }

    /**
     * update host scheduling statistics:
     * * CPU load duration interval over/under policy threshold
     */
    public void updateHostSchedulingStats(VDS vds) {
        if (vds.getUsageCpuPercent() != null) {
            Cluster cluster = getClusterDao().get(vds.getClusterId());
            if (vds.getUsageCpuPercent() >= NumberUtils.toInt(cluster.getClusterPolicyProperties()
                    .get(HIGH_UTILIZATION),
                    Config.<Integer> getValue(ConfigValues.HighUtilizationForEvenlyDistribute))
                    || vds.getUsageCpuPercent() <= NumberUtils.toInt(cluster.getClusterPolicyProperties()
                            .get(LOW_UTILIZATION),
                            Config.<Integer> getValue(ConfigValues.LowUtilizationForEvenlyDistribute))) {
                if (vds.getCpuOverCommitTimestamp() == null) {
                    vds.setCpuOverCommitTimestamp(new Date());
                }
            } else {
                vds.setCpuOverCommitTimestamp(null);
            }
        }
    }

    /**
     * Clear pending records for a VM.
     * This operation locks the cluster to make sure a possible scheduling operation is not under way.
     */
    public void clearPendingVm(VmStatic vm) {
        prepareClusterLock(vm.getClusterId());
        try {
            lockCluster(vm.getClusterId());
            getPendingResourceManager().clearVm(vm);
        } catch (InterruptedException e) {
            log.warn("Interrupted.. pending counters can be out of sync");
        } finally {
            releaseCluster(vm.getClusterId());
        }
    }
}
