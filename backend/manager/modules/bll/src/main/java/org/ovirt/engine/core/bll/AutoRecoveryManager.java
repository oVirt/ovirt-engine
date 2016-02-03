package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AutoRecoverDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.vdsbroker.monitoring.NetworkMonitoringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs scheduled autorecovery jobs.
 * {@link http://www.ovirt.org/wiki/Features/Autorecovery}
 */
public class AutoRecoveryManager {
    private static final AutoRecoveryManager instance = new AutoRecoveryManager();
    private static final Logger log = LoggerFactory.getLogger(AutoRecoveryManager.class);

    static AutoRecoveryManager getInstance() {
        return instance;
    }

    private AutoRecoveryManager() {
        // intentionally empty
    }

    /**
     * Should be called by backend, schedules the execution as configured.
     */
    void initialize() {
        log.info("Start initializing {}", getClass().getSimpleName());
        Injector.get(SchedulerUtilQuartzImpl.class).scheduleACronJob(this, "onTimer",
                new Class<?>[] {}, new Object[] {}, Config.<String> getValue(ConfigValues.AutoRecoverySchedule));
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    /**
     * Called by the scheduler in regular intervals.
     */
    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        DbFacade dbFacade = getDbFacade();
        check(dbFacade.getVdsDao(),
                VdcActionType.ActivateVds,
                new DoWithClosure<VDS, VdcActionParametersBase>() {
            @Override
            public VdcActionParametersBase doWith(final VDS arg) {
                final VdsActionParameters params = new VdsActionParameters(arg.getId());
                params.setRunSilent(true);
                return params;
            }
        }, new FilterClosure<VDS>() {
            @Override
            public List<VDS> filter(List<VDS> list) {
                        List<VDS> filtered = new ArrayList<>(list.size());
                        List<VdsNetworkInterface> nics;

                        for (VDS vds : list) {
                            if (vds.getNonOperationalReason() == NonOperationalReason.NETWORK_INTERFACE_IS_DOWN) {
                                getBackend().getResourceManager().runVdsCommand(VDSCommandType.GetStats,
                                        new VdsIdAndVdsVDSCommandParametersBase(vds));
                                nics = vds.getInterfaces();
                            } else {
                                nics = getDbFacade().getInterfaceDao().getAllInterfacesForVds(vds.getId());
                            }

                            Map<String, Set<String>> problematicNics =
                                    NetworkMonitoringHelper.determineProblematicNics(nics, getDbFacade().getNetworkDao()
                                    .getAllForCluster(vds.getClusterId()));
                            if (problematicNics.isEmpty()) {
                                filtered.add(vds);
                            }
                        }
                        return filtered;
            }
        }, "hosts");
        check(dbFacade.getStorageDomainDao(),
                VdcActionType.ConnectDomainToStorage,
                new DoWithClosure<StorageDomain, VdcActionParametersBase>() {
            @Override
            public VdcActionParametersBase doWith(final StorageDomain arg) {
                final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(
                        arg.getId(), arg.getStoragePoolId());
                params.setRunSilent(true);
                return params;
            }
        }, new FilterClosure<StorageDomain>() {
            @Override
            public List<StorageDomain> filter(List<StorageDomain> list) {
                return list;
            }
        }, "storage domains");
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
            final DoWithClosure<T, VdcActionParametersBase> paramsCallback,
            final FilterClosure<T> filter,
            final String logMsg) {
        if (!shouldPerformRecoveryOnType(logMsg)) {
            log.info("Autorecovering {} is disabled, skipping", logMsg);
            return;
        }
        log.debug("Checking autorecoverable {}" , logMsg);
        final List<T> fails = filter.filter(dao.listFailedAutorecoverables());
        if (fails.size() > 0) {
            final BackendInternal backend = getBackend();
            log.info("Autorecovering {} {}", fails.size(), logMsg);
            for (final T fail : fails) {
                log.info("Autorecovering {} id: {} {}", logMsg, fail.getId(), getHostName(fail));
                final VdcActionParametersBase actionParams = paramsCallback.doWith(fail);
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

    protected DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    protected BackendInternal getBackend() {
        return Backend.getInstance();
    }

    private interface DoWithClosure<T, R> {
        R doWith(T arg);
    }

    private interface FilterClosure<T> {
        List<T> filter(List<T> list);
    }

    private static boolean shouldPerformRecoveryOnType(String type) {
        Map<String, String> allowedRecoveryTypesFromConfig =
                Config.<Map<String, String>> getValue(ConfigValues.AutoRecoveryAllowedTypes);
        String isAllowed = allowedRecoveryTypesFromConfig.get(type);
        if (isAllowed != null) {
            return Boolean.parseBoolean(isAllowed);
        }

        return true;
    }

}
