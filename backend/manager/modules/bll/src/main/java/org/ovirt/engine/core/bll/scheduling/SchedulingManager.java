package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerDiscoveryThread;
import org.ovirt.engine.core.bll.scheduling.external.ExternalSchedulerFactory;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
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
    private static SchedulingManager instance = null;

    public static SchedulingManager getInstance() {
        if (instance == null) {
            synchronized (SchedulingManager.class) {
                if (instance == null) {
                    instance = new SchedulingManager();
                    EnableLoadBalancer();
                }
            }
        }
        return instance;
    }

    /**
     * <policy id, policy> map
     */
    private final ConcurrentHashMap<Guid, ClusterPolicy> policyMap;
    /**
     * <policy unit id, policy unit> map
     */
    private volatile ConcurrentHashMap<Guid, PolicyUnitImpl> policyUnits;

    private final Object policyUnitsLock = new Object();

    private final ConcurrentHashMap<Guid, Object> clusterLockMap = new ConcurrentHashMap<Guid, Object>();

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
        if(Config.GetValue(ConfigValues.ExternalSchedulerEnabled)) {
            log.info("Starting external scheduler dicovery thread");
            ExternalSchedulerDiscoveryThread discoveryThread = new ExternalSchedulerDiscoveryThread();
            discoveryThread.start();
        } else {
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

    public Guid schedule(VDSGroup cluster,
            VM vm,
            List<Guid> hostBlackList,
            List<Guid> hostWhiteList,
            Guid destHostId,
            List<String> messages,
            VdsFreeMemoryChecker memoryChecker) {
        clusterLockMap.putIfAbsent(cluster.getId(), new Object());
        synchronized (clusterLockMap.get(cluster.getId())) {
            List<VDS> vdsList = getVdsDAO()
                    .getAllOfTypes(new VDSType[] { VDSType.VDS, VDSType.oVirtNode });
            updateInitialHostList(vdsList, hostBlackList, true);
            updateInitialHostList(vdsList, hostWhiteList, false);
            ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
            Map<String, String> parameters = createClusterPolicyParameters(cluster);
            if (destHostId != null) {
                if (checkDestinationHost(vm,
                        vdsList,
                        destHostId,
                        messages,
                        policy,
                        parameters,
                        memoryChecker)) {
                    return destHostId;
                } else if (vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
                    return null;
                }
            }
            vdsList =
                    runFilters(policy.getFilters(),
                            vdsList,
                            vm,
                            parameters,
                            policy.getFilterPositionMap(),
                            messages,
                            memoryChecker);

            if (vdsList == null || vdsList.size() == 0) {
                return null;
            }
            if (policy.getFunctions() == null || policy.getFunctions().isEmpty()) {
                return vdsList.get(0).getId();
            }
            Guid bestHost = runFunctions(policy.getFunctions(), vdsList, vm, parameters);
            if (bestHost != null) {
                getVdsDynamicDao().updatePartialVdsDynamicCalc(
                        bestHost,
                        1,
                        vm.getNumOfCpus(),
                        vm.getMinAllocatedMem(),
                        vm.getVmMemSizeMb(),
                        vm.getNumOfCpus());
            }
            return bestHost;
        }
    }

    public boolean canSchedule(VDSGroup cluster,
            VM vm,
            List<Guid> vdsBlackList,
            List<Guid> vdsWhiteList,
            Guid destVdsId,
            List<String> messages) {
        List<VDS> vdsList = getVdsDAO()
                .getAllOfTypes(new VDSType[] { VDSType.VDS, VDSType.oVirtNode });
        updateInitialHostList(vdsList, vdsBlackList, true);
        updateInitialHostList(vdsList, vdsWhiteList, false);
        ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
        Map<String, String> parameters = createClusterPolicyParameters(cluster);
        if (destVdsId != null) {
            if (checkDestinationHost(vm,
                    vdsList,
                    destVdsId,
                    messages,
                    policy,
                    parameters,
                    noWaitingMemoryChecker)) {
                return true;
            } else if (vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
                return false;
            }
        }
        vdsList =
                runFilters(policy.getFilters(),
                        vdsList,
                        vm,
                        parameters,
                        policy.getFilterPositionMap(),
                        messages,
                        noWaitingMemoryChecker);

        if (vdsList == null || vdsList.size() == 0) {
            return false;
        }
        return true;
    }

    protected boolean checkDestinationHost(VM vm,
            List<VDS> vdsList,
            Guid destVdsId,
            List<String> messages,
            ClusterPolicy policy,
            Map<String, String> parameters,
            VdsFreeMemoryChecker memoryChecker) {
        List<VDS> destVdsList = new ArrayList<VDS>();
        for (VDS vds : vdsList) {
            if (vds.getId().equals(destVdsId)) {
                destVdsList.add(vds);
                break;
            }
        }
        destVdsList =
                runFilters(policy.getFilters(),
                        destVdsList,
                        vm,
                        parameters,
                        policy.getFilterPositionMap(),
                        messages,
                        memoryChecker);

        return destVdsList != null && destVdsList.size() == 1;
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
            List<String> messages, VdsFreeMemoryChecker memoryChecker) {
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
                runInternalFilters(internalFilters, hostList, vm, parameters, filterPositionMap, messages, memoryChecker);

        if (Config.<Boolean> GetValue(ConfigValues.ExternalSchedulerEnabled) && externalFilters.size() > 0
                && hostList != null && hostList.size() > 0) {
            hostList = runExternalFilters(externalFilters, hostList, vm, parameters, messages);
        }

        return hostList;
    }

    private List<VDS> runInternalFilters(ArrayList<PolicyUnitImpl> filters,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            Map<Guid, Integer> filterPositionMap,
            List<String> messages, VdsFreeMemoryChecker memoryChecker) {
        if (filters != null) {
            for (PolicyUnitImpl filterPolicyUnit : filters) {
                if (hostList == null || hostList.isEmpty()) {
                    break;
                }
                filterPolicyUnit.setMemoryChecker(memoryChecker);
                hostList = filterPolicyUnit.filter(hostList, vm, parameters, messages);
            }
        }
        return hostList;
    }

    private List<VDS> runExternalFilters(ArrayList<PolicyUnitImpl> filters,
            List<VDS> hostList,
            VM vm,
            Map<String, String> parameters,
            List<String> messages) {
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

        if (Config.<Boolean> GetValue(ConfigValues.ExternalSchedulerEnabled) && externalScoreFunctions.size() > 0) {
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

    public static void EnableLoadBalancer() {
        if (Config.<Boolean> GetValue(ConfigValues.EnableVdsLoadBalancing)) {
            log.info("Start scheduling to enable vds load balancer");
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(instance,
                    "PerformLoadBalancing",
                    new Class[] {},
                    new Object[] {},
                    Config.<Integer> GetValue(ConfigValues.VdsLoadBalancingeIntervalInMinutes),
                    Config.<Integer> GetValue(ConfigValues.VdsLoadBalancingeIntervalInMinutes),
                    TimeUnit.MINUTES);
            log.info("Finished scheduling to enable vds load balancer");
        }
    }

    @OnTimerMethodAnnotation("PerformLoadBalancing")
    public void PerformLoadBalancing() {
        log.debugFormat("Load Balancer timer entered.");
        List<VDSGroup> clusters = DbFacade.getInstance().getVdsGroupDao().getAll();
        for (VDSGroup cluster : clusters) {
            ClusterPolicy policy = policyMap.get(cluster.getClusterPolicyId());
            PolicyUnitImpl policyUnit = policyUnits.get(policy.getBalance());
            Pair<List<Guid>, Guid> balanceResult = null;
            if (policyUnit.isInternal()){
                balanceResult = internalRunBalance(policyUnit, cluster);
            } else if (Config.GetValue(ConfigValues.ExternalSchedulerEnabled)) {
                if (policyUnit.isEnabled()) {
                    balanceResult = externalRunBalance(policyUnit, cluster);
                }
            }

            if (balanceResult != null && balanceResult.getSecond() != null) {
                migrationHandler.migrateVM((ArrayList<Guid>) balanceResult.getFirst(), balanceResult.getSecond());
            }
        }
    }

    private Pair<List<Guid>, Guid> internalRunBalance(PolicyUnitImpl policyUnit, VDSGroup cluster) {
        List<VDS> hosts = getVdsDAO()
                .getAllOfTypes(new VDSType[] { VDSType.VDS, VDSType.oVirtNode });
        return policyUnit.balance(cluster,
                hosts,
                cluster.getClusterPolicyProperties(),
                new ArrayList<String>());
    }

    private Pair<List<Guid>, Guid> externalRunBalance(PolicyUnitImpl policyUnit, VDSGroup cluster){
        List<VDS> hosts = getVdsDAO()
                .getAllOfTypes(new VDSType[] { VDSType.VDS, VDSType.oVirtNode });
        List<Guid> hostIDs = new ArrayList<Guid>();
        for (VDS vds : hosts) {
            hostIDs.add(vds.getId());
        }
        return ExternalSchedulerFactory.getInstance()
                .runBalance(policyUnit.getName(), hostIDs, cluster.getClusterPolicyProperties());
    }

}
