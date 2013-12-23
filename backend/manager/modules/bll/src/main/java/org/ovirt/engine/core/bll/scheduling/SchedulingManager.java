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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerDiscoveryThread;
import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerFactory;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.OptimizationType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AlertDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class SchedulingManager {
    private static Log log = LogFactory.getLog(SchedulingManager.class);
    /**
     * singleton
     */
    private static volatile SchedulingManager instance = null;

    public static SchedulingManager getInstance() {
        if (instance == null) {
            synchronized (SchedulingManager.class) {
                if (instance == null) {
                    instance = new SchedulingManager();
                    enableLoadBalancer();
                    enableHaReservationCheck();
                }
            }
        }
        return instance;
    }

    private static final String HIGH_UTILIZATION = "HighUtilization";
    private static final String LOW_UTILIZATION = "LowUtilization";

    /**
     * <policy id, policy> map
     */
    private final ConcurrentHashMap<Guid, ClusterPolicy> policyMap;
    /**
     * <policy unit id, policy unit> map
     */
    private volatile ConcurrentHashMap<Guid, PolicyUnitImpl> policyUnits;

    private final Object policyUnitsLock = new Object();

    private final ConcurrentHashMap<Guid, Semaphore> clusterLockMap = new ConcurrentHashMap<Guid, Semaphore>();

    private final VdsFreeMemoryChecker noWaitingMemoryChecker = new VdsFreeMemoryChecker(new NonWaitingDelayer());
    private MigrationHandler migrationHandler;

    private SchedulingManager() {
        policyMap = new ConcurrentHashMap<Guid, ClusterPolicy>();
        policyUnits = new ConcurrentHashMap<Guid, PolicyUnitImpl>();
    }

    public void init() {
        log.info("Initializing Scheduling manager");
        loadPolicyUnits();
        loadClusterPolicies();
        ExternalSchedulerDiscoveryThread discoveryThread = new ExternalSchedulerDiscoveryThread();
        if(Config.<Boolean> getValue(ConfigValues.ExternalSchedulerEnabled)) {
            log.info("Starting external scheduler dicovery thread");
            discoveryThread.start();
        } else {
            discoveryThread.markAllExternalPoliciesAsDisabled();
            log.info("External scheduler disabled, discovery skipped");
        }
        log.info("Initialized Scheduling manager");
    }

    public void reloadPolicyUnits() {
        synchronized (policyUnitsLock) {
            policyUnits = new ConcurrentHashMap<Guid, PolicyUnitImpl>();
            loadPolicyUnits();
        }
    }

    public List<ClusterPolicy> getClusterPolicies() {
        return new ArrayList<ClusterPolicy>(policyMap.values());
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

    public List<VDSGroup> getClustersByClusterPolicyId(Guid clusterPolicyId) {
        return getVdsGroupDao().getClustersByClusterPolicyId(clusterPolicyId);
    }

    public Map<Guid, PolicyUnitImpl> getPolicyUnitsMap() {
        synchronized (policyUnitsLock) {
            return policyUnits;
        }
    }

    protected void loadClusterPolicies() {
        List<ClusterPolicy> allClusterPolicies = getClusterPolicyDao().getAll();
        for (ClusterPolicy clusterPolicy : allClusterPolicies) {
            policyMap.put(clusterPolicy.getId(), clusterPolicy);
        }
    }

    public void setMigrationHandler(MigrationHandler migrationHandler) {
        if (this.migrationHandler != null) {
            throw new RuntimeException("Load balance migration handler should be set only once");
        }
        this.migrationHandler = migrationHandler;
    }

    protected void loadPolicyUnits() {
        List<PolicyUnit> allPolicyUnits = getPolicyUnitDao().getAll();
        for (PolicyUnit policyUnit : allPolicyUnits) {
            if (policyUnit.isInternal()) {
                policyUnits.put(policyUnit.getId(), PolicyUnitImpl.getPolicyUnitImpl(policyUnit));
            } else {
                policyUnits.put(policyUnit.getId(), new PolicyUnitImpl(policyUnit));
            }
        }
    }

    private static class SchedulingResult {
        Map<Guid, Pair<VdcBllMessages, String>> filteredOutReasons;
        Map<Guid, String> hostNames;
        String message;
        Guid vdsSelected = null;

        public SchedulingResult() {
            filteredOutReasons = new HashMap<Guid, Pair<VdcBllMessages, String>>();
            hostNames = new HashMap<>();
        }

        public Guid getVdsSelected() {
            return vdsSelected;
        }

        public void setVdsSelected(Guid vdsSelected) {
            this.vdsSelected = vdsSelected;
        }

        public void addReason(Guid id, String hostName, VdcBllMessages filterType, String filterName) {
            filteredOutReasons.put(id, new Pair<VdcBllMessages, String>(filterType, filterName));
            hostNames.put(id, hostName);
        }

        public Set<Entry<Guid, Pair<VdcBllMessages, String>>> getReasons() {
            return filteredOutReasons.entrySet();
        }

        public Collection<String> getReasonMessages() {
            List<String> lines = new ArrayList<>();

            for (Entry<Guid, Pair<VdcBllMessages, String>> line: filteredOutReasons.entrySet()) {
                lines.add(line.getValue().getFirst().name());
                lines.add(String.format("$%1$s %2$s", "hostName", hostNames.get(line.getKey())));
                lines.add(String.format("$%1$s %2$s", "filterName", line.getValue().getSecond()));
                lines.add(VdcBllMessages.SCHEDULING_HOST_FILTERED_REASON.name());
            }

            return lines;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public Guid schedule(VDSGroup cluster,
            VM vm,
            List<Guid> hostBlackList,
            List<Guid> hostWhiteList,
            Guid destHostId,
            List<String> messages,
            VdsFreeMemoryChecker memoryChecker,
            String correlationId) {
        clusterLockMap.putIfAbsent(cluster.getId(), new Semaphore(1));
        try {
            log.debugFormat("scheduling started, correlation Id: {0}", correlationId);
            checkAllowOverbooking(cluster);
            clusterLockMap.get(cluster.getId()).acquire();
            List<VDS> vdsList = getVdsDAO()
                    .getAllForVdsGroupWithStatus(cluster.getId(), VDSStatus.Up);
            updateInitialHostList(vdsList, hostBlackList, true);
            updateInitialHostList(vdsList, hostWhiteList, false);
            ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
            Map<String, String> parameters = createClusterPolicyParameters(cluster);

            vdsList =
                    runFilters(policy.getFilters(),
                            vdsList,
                            vm,
                            parameters,
                            policy.getFilterPositionMap(),
                            messages,
                            memoryChecker,
                            true,
                            correlationId);

            if (vdsList == null || vdsList.size() == 0) {
                return null;
            }
            // in case a default destination host was specified, and
            // it passed filters return it
            if (destHostId != null) {
                for (VDS vds : vdsList) {
                    if (destHostId.equals(vds.getId())) {
                        return destHostId;
                    }
                }
            }
            if (policy.getFunctions() == null || policy.getFunctions().isEmpty()) {
                return vdsList.get(0).getId();
            }
            Guid bestHost = null;
            if (shouldWeighClusterHosts(cluster, vdsList)) {
                bestHost = runFunctions(policy.getFunctions(), vdsList, vm, parameters);
            }
            if (bestHost == null && vdsList.size() > 0) {
                bestHost = vdsList.get(0).getId();
            }
            if (bestHost != null) {
                getVdsDynamicDao().updatePartialVdsDynamicCalc(
                        bestHost,
                        1,
                        vm.getNumOfCpus(),
                        vm.getMinAllocatedMem(),
                        0,
                        0);
            }
            return bestHost;
        } catch (InterruptedException e) {
            log.error("interrupted", e);
            return null;
        } finally {
            // ensuring setting the semaphore permits to 1
            synchronized (clusterLockMap.get(cluster.getId())) {
                clusterLockMap.get(cluster.getId()).drainPermits();
                clusterLockMap.get(cluster.getId()).release();
            }
            log.debugFormat("Scheduling ended, correlation Id: {0}", correlationId);
        }
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
    protected void checkAllowOverbooking(VDSGroup cluster) {
        if (OptimizationType.ALLOW_OVERBOOKING == cluster.getOptimizationType()
                && Config.<Boolean> getValue(ConfigValues.SchedulerAllowOverBooking)
                && clusterLockMap.get(cluster.getId()).getQueueLength() >=
                Config.<Integer> getValue(ConfigValues.SchedulerOverBookingThreshold)) {
            log.infoFormat("scheduler: cluster ({0}) lock is skipped (cluster is allowed to overbook)",
                    cluster.getName());
            // release pending threads (requests) and current one (+1)
            clusterLockMap.get(cluster.getId())
                    .release(Config.<Integer> getValue(ConfigValues.SchedulerOverBookingThreshold) + 1);
        }
    }

    /**
     * Checks whether scheduler should weigh hosts/or skip weighing:
     * * More than one host (it's trivial to weigh a single host).
     * * optimize for speed is enabled for the cluster, and there are less than configurable requests pending (skip
     * weighing in a loaded setup).
     *
     * @param cluster
     * @param vdsList
     * @return
     */
    protected boolean shouldWeighClusterHosts(VDSGroup cluster, List<VDS> vdsList) {
        Integer threshold = Config.<Integer> getValue(ConfigValues.SpeedOptimizationSchedulingThreshold);
        // threshold is crossed only when cluster is configured for optimized for speed
        boolean crossedThreshold =
                OptimizationType.OPTIMIZE_FOR_SPEED == cluster.getOptimizationType()
                        && clusterLockMap.get(cluster.getId()).getQueueLength() >
                        threshold;
        if (crossedThreshold) {
            log.infoFormat("Scheduler: skipping whighing hosts in cluster {0}, since there are more than {1} parallel requests",
                    cluster.getName(),
                    threshold);
        }
        return vdsList.size() > 1
                && !crossedThreshold;
    }

    public boolean canSchedule(VDSGroup cluster,
            VM vm,
            List<Guid> vdsBlackList,
            List<Guid> vdsWhiteList,
            Guid destVdsId,
            List<String> messages) {
        List<VDS> vdsList = getVdsDAO()
                .getAllForVdsGroupWithStatus(cluster.getId(), VDSStatus.Up);
        updateInitialHostList(vdsList, vdsBlackList, true);
        updateInitialHostList(vdsList, vdsWhiteList, false);
        ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
        Map<String, String> parameters = createClusterPolicyParameters(cluster);

        vdsList =
                runFilters(policy.getFilters(),
                        vdsList,
                        vm,
                        parameters,
                        policy.getFilterPositionMap(),
                        messages,
                        noWaitingMemoryChecker,
                        false,
                        null);

        if (vdsList == null || vdsList.size() == 0) {
            return false;
        }
        return true;
    }

    static List<Guid> getEntityIds(List<? extends BusinessEntity<Guid>> entities) {
        ArrayList<Guid> ids = new ArrayList<>();
        for (BusinessEntity<Guid> entity : entities) {
            ids.add(entity.getId());
        }
        return ids;
    }

    protected Map<String, String> createClusterPolicyParameters(VDSGroup cluster) {
        Map<String, String> parameters = new HashMap<String, String>();
        if (cluster.getClusterPolicyProperties() != null) {
            parameters.putAll(cluster.getClusterPolicyProperties());
        }
        return parameters;
    }

    protected void updateInitialHostList(List<VDS> vdsList, List<Guid> list, boolean contains) {
        if (list != null && list.size() > 0) {
            List<VDS> toRemoveList = new ArrayList<VDS>();
            Set<Guid> listSet = new HashSet<Guid>(list);
            for (VDS vds : vdsList) {
                if (listSet.contains(vds.getId()) == contains) {
                    toRemoveList.add(vds);
                }
            }
            vdsList.removeAll(toRemoveList);
        }
    }

    private List<VDS> runFilters(ArrayList<Guid> filters,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            Map<Guid, Integer> filterPositionMap,
            List<String> messages,
            VdsFreeMemoryChecker memoryChecker,
            boolean shouldRunExternalFilters,
            String correlationId) {
        SchedulingResult result = new SchedulingResult();
        ArrayList<PolicyUnitImpl> internalFilters = new ArrayList<PolicyUnitImpl>();
        ArrayList<PolicyUnitImpl> externalFilters = new ArrayList<PolicyUnitImpl>();
        sortFilters(filters, filterPositionMap);
        if (filters != null) {
            for (Guid filter : filters) {
                PolicyUnitImpl filterPolicyUnit = policyUnits.get(filter);
                if (filterPolicyUnit.isInternal()){
                    internalFilters.add(filterPolicyUnit);
                } else {
                    if (filterPolicyUnit.isEnabled()) {
                        externalFilters.add(filterPolicyUnit);
                    }
                }
            }
        }

        hostList =
                runInternalFilters(internalFilters, hostList, vm, parameters, filterPositionMap, messages,
                        memoryChecker, correlationId, result);

        if (shouldRunExternalFilters
                && Config.<Boolean> getValue(ConfigValues.ExternalSchedulerEnabled)
                && externalFilters.size() > 0
                && hostList != null
                && hostList.size() > 0) {
            hostList = runExternalFilters(externalFilters, hostList, vm, parameters, messages, correlationId, result);
        }

        if (hostList == null || hostList.size() == 0) {
            messages.add(VdcBllMessages.SCHEDULING_ALL_HOSTS_FILTERED_OUT.name());
            messages.addAll(result.getReasonMessages());
        }
        return hostList;
    }

    private List<VDS> runInternalFilters(ArrayList<PolicyUnitImpl> filters,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            Map<Guid, Integer> filterPositionMap,
            List<String> messages, VdsFreeMemoryChecker memoryChecker,
            String correlationId, SchedulingResult result) {
        if (filters != null) {
            for (PolicyUnitImpl filterPolicyUnit : filters) {
                if (hostList == null || hostList.isEmpty()) {
                    break;
                }
                filterPolicyUnit.setMemoryChecker(memoryChecker);
                List<VDS> currentHostList = new ArrayList<VDS>(hostList);
                hostList = filterPolicyUnit.filter(hostList, vm, parameters, messages);
                logFilterActions(currentHostList,
                        toIdSet(hostList),
                        VdcBllMessages.VAR__FILTERTYPE__INTERNAL,
                        filterPolicyUnit.getName(),
                        result,
                        correlationId);
            }
        }
        return hostList;
    }

    private Set<Guid> toIdSet(List<VDS> hostList) {
        Set<Guid> set = new HashSet<Guid>();
        if (hostList != null) {
            for (VDS vds : hostList) {
                set.add(vds.getId());
            }
        }
        return set;
    }

    private void logFilterActions(List<VDS> oldList,
            Set<Guid> newSet,
            VdcBllMessages actionName,
            String filterName,
            SchedulingResult result,
            String correlationId) {
        for (VDS host: oldList) {
            if (!newSet.contains(host.getId())) {
                String reason =
                        String.format("Candidate host %s (%s) was filtered out by %s filter %s",
                                host.getName(),
                                host.getId().toString(),
                                actionName.name(),
                                filterName);
                if (!StringUtils.isEmpty(correlationId)) {
                    reason = String.format("%s (correlation id: %s)", reason, correlationId);
                }
                log.info(reason);
                result.addReason(host.getId(), host.getName(), actionName, filterName);
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
            List<String> filterNames = new ArrayList<String>();
            for (PolicyUnitImpl filter : filters) {
                filterNames.add(filter.getName());
            }
            List<Guid> hostIDs = new ArrayList<Guid>();
            for (VDS host : hostList) {
                hostIDs.add(host.getId());
            }

            filteredIDs =
                    ExternalSchedulerFactory.getInstance().runFilters(filterNames, hostIDs, vm.getId(), parameters);
            if (filteredIDs != null) {
                logFilterActions(hostList,
                        new HashSet<Guid>(filteredIDs),
                        VdcBllMessages.VAR__FILTERTYPE__EXTERNAL,
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
        List<VDS> retList = new ArrayList<VDS>();
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

    protected Guid runFunctions(ArrayList<Pair<Guid, Integer>> functions,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters) {
        ArrayList<Pair<PolicyUnitImpl, Integer>> internalScoreFunctions =
                new ArrayList<Pair<PolicyUnitImpl, Integer>>();
        ArrayList<Pair<PolicyUnitImpl, Integer>> externalScoreFunctions =
                new ArrayList<Pair<PolicyUnitImpl, Integer>>();

        for (Pair<Guid, Integer> pair : functions) {
            PolicyUnitImpl currentPolicy = policyUnits.get(pair.getFirst());
            if(currentPolicy.isInternal()){
                internalScoreFunctions.add(new Pair<PolicyUnitImpl, Integer>(currentPolicy, pair.getSecond()));
            } else {
                if (currentPolicy.isEnabled()) {
                    externalScoreFunctions.add(new Pair<PolicyUnitImpl, Integer>(currentPolicy, pair.getSecond()));
                }
            }
        }

        Map<Guid, Integer> hostCostTable = runInternalFunctions(internalScoreFunctions, hostList, vm, parameters);

        if (Config.<Boolean> getValue(ConfigValues.ExternalSchedulerEnabled) && externalScoreFunctions.size() > 0) {
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

    private Map<Guid, Integer> runInternalFunctions(ArrayList<Pair<PolicyUnitImpl, Integer>> functions,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters) {
        Map<Guid, Integer> hostCostTable = new HashMap<Guid, Integer>();
        for (Pair<PolicyUnitImpl, Integer> pair : functions) {
            List<Pair<Guid, Integer>> scoreResult = pair.getFirst().score(hostList, vm, parameters);
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

    private void runExternalFunctions(ArrayList<Pair<PolicyUnitImpl, Integer>> functions,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            Map<Guid, Integer> hostCostTable) {
        List<Pair<String, Integer>> scoreNameAndWeight = new ArrayList<Pair<String, Integer>>();
        for (Pair<PolicyUnitImpl, Integer> pair : functions) {
            scoreNameAndWeight.add(new Pair<String, Integer>(pair.getFirst().getName(), pair.getSecond()));
        }

        List<Guid> hostIDs = new ArrayList<Guid>();
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
        Set<Guid> usedPolicyUnits = new HashSet<Guid>();
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
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (Guid policyUnitId : usedPolicyUnits) {
            map.putAll(policyUnits.get(policyUnitId).getParameterRegExMap());
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

    protected VdsDAO getVdsDAO() {
        return DbFacade.getInstance().getVdsDao();
    }

    protected VdsGroupDAO getVdsGroupDao() {
        return DbFacade.getInstance().getVdsGroupDao();
    }

    protected VdsDynamicDAO getVdsDynamicDao() {
        return DbFacade.getInstance().getVdsDynamicDao();
    }

    protected PolicyUnitDao getPolicyUnitDao() {
        return DbFacade.getInstance().getPolicyUnitDao();
    }

    protected ClusterPolicyDao getClusterPolicyDao() {
        return DbFacade.getInstance().getClusterPolicyDao();
    }

    public static void enableLoadBalancer() {
        if (Config.<Boolean> getValue(ConfigValues.EnableVdsLoadBalancing)) {
            log.info("Start scheduling to enable vds load balancer");
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(instance,
                    "performLoadBalancing",
                    new Class[] {},
                    new Object[] {},
                    Config.<Integer> getValue(ConfigValues.VdsLoadBalancingeIntervalInMinutes),
                    Config.<Integer> getValue(ConfigValues.VdsLoadBalancingeIntervalInMinutes),
                    TimeUnit.MINUTES);
            log.info("Finished scheduling to enable vds load balancer");
        }
    }

    public static void enableHaReservationCheck() {

        if (Config.<Boolean> getValue(ConfigValues.EnableVdsLoadBalancing)) {
            log.info("Start HA Reservation check");
            Integer interval = Config.<Integer> getValue(ConfigValues.VdsHaReservationIntervalInMinutes);
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(instance,
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
        List<VDSGroup> clusters = DbFacade.getInstance().getVdsGroupDao().getAll();
        if (clusters != null) {
            HaReservationHandling haReservationHandling = new HaReservationHandling();
            for (VDSGroup cluster : clusters) {
                if (cluster.supportsHaReservation()) {
                    List<VDS> returnedFailedHosts = new ArrayList<VDS>();
                    boolean status =
                            haReservationHandling.checkHaReservationStatusForCluster(cluster, returnedFailedHosts);
                    if (!status) {
                        // create Alert using returnedFailedHosts
                        AuditLogableBase logable = new AuditLogableBase();
                        logable.setVdsGroupId(cluster.getId());
                        logable.addCustomValue("ClusterName", cluster.getName());

                        String failedHostsStr = StringUtils.join(returnedFailedHosts, ", ");
                        logable.addCustomValue("Hosts", failedHostsStr);
                        AlertDirector.Alert(logable, AuditLogType.CLUSTER_ALERT_HA_RESERVATION);
                        log.infoFormat("Cluster: {0} fail to pass HA reservation check.", cluster.getName());
                    }
                }
            }
        }
        log.debug("HA Reservation check timer finished.");
    }


    @OnTimerMethodAnnotation("performLoadBalancing")
    public void performLoadBalancing() {
        log.debugFormat("Load Balancer timer entered.");
        List<VDSGroup> clusters = DbFacade.getInstance().getVdsGroupDao().getAll();
        for (VDSGroup cluster : clusters) {
            ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
            PolicyUnitImpl policyUnit = policyUnits.get(policy.getBalance());
            Pair<List<Guid>, Guid> balanceResult = null;
            if (policyUnit.isEnabled()) {
                List<VDS> hosts = getVdsDAO().getAllForVdsGroupWithoutMigrating(cluster.getId());
                if (policyUnit.isInternal()) {
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

    private Pair<List<Guid>, Guid> internalRunBalance(PolicyUnitImpl policyUnit, VDSGroup cluster, List<VDS> hosts) {
        return policyUnit.balance(cluster,
                hosts,
                cluster.getClusterPolicyProperties(),
                new ArrayList<String>());
    }

    private Pair<List<Guid>, Guid> externalRunBalance(PolicyUnitImpl policyUnit, VDSGroup cluster, List<VDS> hosts) {
        List<Guid> hostIDs = new ArrayList<Guid>();
        for (VDS vds : hosts) {
            hostIDs.add(vds.getId());
        }
        return ExternalSchedulerFactory.getInstance()
                .runBalance(policyUnit.getName(), hostIDs, cluster.getClusterPolicyProperties());
    }

    /**
     * returns all cluster policies names containing the specific policy unit.
     * @param policyUnitId
     * @return
     */
    public List<String> getClusterPoliciesNamesByPolicyUnitId(Guid policyUnitId) {
        List<String> list = new ArrayList<String>();
        PolicyUnit policyUnit = policyUnits.get(policyUnitId);
        if (policyUnit != null) {
            for (ClusterPolicy clusterPolicy : policyMap.values()) {
                switch (policyUnit.getPolicyUnitType()) {
                case Filter:
                    if (clusterPolicy.getFilters().contains(policyUnitId)) {
                        list.add(clusterPolicy.getName());
                    }
                    break;
                case Weight:
                    for (Pair<Guid, Integer> pair : clusterPolicy.getFunctions()) {
                        if (pair.getFirst().equals(policyUnitId)) {
                            list.add(clusterPolicy.getName());
                            break;
                        }
                    }
                    break;
                case LoadBalancing:
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
     * @param vds
     */
    public void updateHostSchedulingStats(VDS vds) {
        if (vds.getUsageCpuPercent() != null) {
            VDSGroup vdsGroup = getVdsGroupDao().get(vds.getVdsGroupId());
            if (vds.getUsageCpuPercent() >= NumberUtils.toInt(vdsGroup.getClusterPolicyProperties()
                    .get(HIGH_UTILIZATION),
                    Config.<Integer> getValue(ConfigValues.HighUtilizationForEvenlyDistribute))
                    || vds.getUsageCpuPercent() <= NumberUtils.toInt(vdsGroup.getClusterPolicyProperties()
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

}
