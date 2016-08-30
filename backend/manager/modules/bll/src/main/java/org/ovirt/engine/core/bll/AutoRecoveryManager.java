package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AutoRecoverDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.vdsbroker.monitoring.NetworkMonitoringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs scheduled autorecovery jobs.
 * {@link http://www.ovirt.org/develop/release-management/features/sla/autorecovery/}
 */
@Singleton
public class AutoRecoveryManager implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(AutoRecoveryManager.class);

    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

    @Inject
    private BackendInternal backend;

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
        schedulerUtil.scheduleACronJob(this, "onTimer",
                new Class<?>[] {}, new Object[] {}, Config.getValue(ConfigValues.AutoRecoverySchedule));
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * Called by the scheduler in regular intervals.
     */
    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        check(vdsDao,
                VdcActionType.ActivateVds,
                arg -> {
                    final VdsActionParameters params = new VdsActionParameters(arg.getId());
                    params.setRunSilent(true);
                    return params;
                }, list -> {
                    List<VDS> filtered = new ArrayList<>(list.size());
                    List<VdsNetworkInterface> nics;

                    for (VDS vds : list) {
                        if (vds.getNonOperationalReason() == NonOperationalReason.NETWORK_INTERFACE_IS_DOWN) {
                            backend.getResourceManager().runVdsCommand(VDSCommandType.GetStats,
                                    new VdsIdAndVdsVDSCommandParametersBase(vds));
                            nics = vds.getInterfaces();
                        } else {
                            nics = interfaceDao.getAllInterfacesForVds(vds.getId());
                        }

                        Map<String, Set<String>> problematicNics =
                                NetworkMonitoringHelper.determineProblematicNics(nics,
                                        networkDao.getAllForCluster(vds.getClusterId()));
                        if (problematicNics.isEmpty()) {
                            filtered.add(vds);
                        }
                    }
                    return filtered;
        }, "hosts");
        check(storageDomainDao,
                VdcActionType.ConnectDomainToStorage,
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
            final VdcActionType actionType,
            final Function<T, VdcActionParametersBase> paramsCallback,
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
                final VdcActionParametersBase actionParams = paramsCallback.apply(fail);
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
