package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.network.NetworkVdsmNameMapper;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.EngineCronTrigger;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AutoRecoverDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.vdsbroker.monitoring.NetworkMonitoringHelper;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs scheduled autorecovery jobs.
 * @see <a href="http://www.ovirt.org/develop/release-management/features/sla/autorecovery/">The feature page</a>
 */
@Singleton
public class AutoRecoveryManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(AutoRecoveryManager.class);
    private static NetworkMonitoringHelper networkMonitoringHelper = new NetworkMonitoringHelper();

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @Inject
    private BackendInternal backend;

    @Inject
    private VDSBrokerFrontend resourceManager;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private NetworkVdsmNameMapper vdsmNameMapper;

    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    @Inject
    private VdsDao vdsDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private NetworkDao networkDao;

    /**
     * Should be called by backend, schedules the execution as configured.
     */
    @PostConstruct
    void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        executor.schedule(this::recover, new EngineCronTrigger(Config.getValue(ConfigValues.AutoRecoverySchedule)));
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * Called by the scheduler in regular intervals.
     */
    public void recover() {
        try {
            recoverImpl();
        } catch (Throwable t) {
            log.error("Exception in recover: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    public void recoverImpl() {
        check(vdsDao,
                ActionType.ActivateVds,
                arg -> {
                    final VdsActionParameters params = new VdsActionParameters(arg.getId());
                    params.setRunSilent(true);
                    return params;
                }, list -> {
                    List<VDS> filtered = new ArrayList<>(list.size());
                    List<VdsNetworkInterface> nics;
                    List<Network> clusterNetworks = new ArrayList<>();
                    Map<Guid, Network> networkMap = new HashMap<>();
                    Map<Guid, List<Network>> clusterNetworksMap = new HashMap<>();
                    for (VDS vds : list) {
                        if (vds.getNonOperationalReason() == NonOperationalReason.HE_HOST_IN_NON_HE_CLUSTER) {
                            // the host has been moved out of its original cluster for operational purpose. It must not
                            // and will not be activated so that HE VM never migrated out of its cluster
                            continue;
                        }
                        nics = interfaceDao.getAllInterfacesForVds(vds.getId());
                        Guid clusterId = vds.getClusterId();
                        if (!clusterNetworksMap.containsKey(clusterId)) {
                            clusterNetworks = networkDao.getAllForCluster(vds.getClusterId());
                            clusterNetworksMap.put(clusterId, clusterNetworks);
                            networkMap.putAll(
                                    clusterNetworks.stream().collect(Collectors.toMap(Network::getId, network -> network)));
                        }
                        Map<String, Set<String>> problematicNics =
                                networkMonitoringHelper.determineProblematicNics(nics,
                                        clusterNetworksMap.get(clusterId));

                        if (!problematicNics.isEmpty()) {
                            continue;
                        }
                        // here we check if the host networks match it's cluster networks
                        Set<String> attachedNetworkNames = networkAttachmentDao
                                .getAllForHost(vds.getId())
                                .stream()
                                .map(networkAttachment -> networkMap.get(networkAttachment.getNetworkId()).getName())
                                .collect(Collectors.toSet());
                        String missingOperationalClusterNetworks =
                                networkMonitoringHelper.getMissingOperationalClusterNetworks(attachedNetworkNames,
                                        clusterNetworks);
                        if (missingOperationalClusterNetworks.length() > 0) {
                            continue;
                        }
                        // Check that VM networks are implemented above a bridge.
                        String vmNetworksImplementedAsBridgeless =
                                networkMonitoringHelper.getVmNetworksImplementedAsBridgeless(vds, clusterNetworks);
                        if (vmNetworksImplementedAsBridgeless.length() > 0) {
                            continue;
                        }
                        filtered.add(vds);
                    }
                    return filtered;
        }, "hosts");
        check(storageDomainDao,
                ActionType.ConnectDomainToStorage,
                arg -> {
                    final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(
                            arg.getId(), arg.getStoragePoolId());
                    params.setRunSilent(true);
                    return params;
                }, list -> list, "storage domains");
    }

    /**
     * Check all the failing resources retrieved from the dao.
     * @param dao               the dao to get the list of failing resources from
     * @param actionType        autorecovery action
     * @param paramsCallback    a closure to create the parameters for the autorecovery action
     * @param filter            a filter to select the recoverable resources
     * @param logMsg            a user-readable name for the failing resource type
     */
    <T extends BusinessEntity<Guid>> void check(final AutoRecoverDao<T> dao,
            final ActionType actionType,
            final Function<T, ActionParametersBase> paramsCallback,
            final Function<List<T>, List<T>> filter,
            final String logMsg) {
        if (!shouldPerformRecoveryOnType(logMsg)) {
            log.info("Autorecovering {} is disabled, skipping", logMsg);
            return;
        }
        log.debug("Checking autorecoverable {}" , logMsg);
        final List<T> fails = filter.apply(dao.listFailedAutorecoverables());
        if (fails.size() > 0) {
            log.info("Autorecovering {} {}", fails.size(), logMsg);
            for (final T fail : fails) {
                log.info("Autorecovering {} id: {} {}", logMsg, fail.getId(), getHostName(fail));
                final ActionParametersBase actionParams = paramsCallback.apply(fail);
                actionParams.setShouldBeLogged(true);
                backend.runInternalAction(actionType, actionParams);
            }
        }
        log.debug("Checking autorecoverable {} done", logMsg);
    }

    private <T extends BusinessEntity<Guid>>  String getHostName(T entity) {
        if (entity instanceof VDS) {
            return ", name : "+((VDS) entity).getName();
        }
        return "";
    }

    private static boolean shouldPerformRecoveryOnType(String type) {
        Map<String, String> allowedRecoveryTypesFromConfig = Config.getValue(ConfigValues.AutoRecoveryAllowedTypes);
        String isAllowed = allowedRecoveryTypesFromConfig.get(type);
        return isAllowed == null || Boolean.parseBoolean(isAllowed);

    }

}
