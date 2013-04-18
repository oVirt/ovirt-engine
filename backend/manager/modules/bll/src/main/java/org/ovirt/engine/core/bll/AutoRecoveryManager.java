package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AutoRecoverDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.vdsbroker.VdsUpdateRunTimeInfo;

/**
 * Runs scheduled autorecovery jobs.
 * {@link http://www.ovirt.org/wiki/Features/Autorecovery}
 */
public class AutoRecoveryManager {
    private final static AutoRecoveryManager instance = new AutoRecoveryManager();
    private final static Log log = LogFactory.getLog(AutoRecoveryManager.class);

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
        log.info("Start initializing " + getClass().getSimpleName());
        SchedulerUtilQuartzImpl.getInstance().scheduleACronJob(this, "onTimer",
                new Class<?>[] {}, new Object[] {}, Config.<String> GetValue(ConfigValues.AutoRecoverySchedule));
        log.info("Finished initializing " + getClass().getSimpleName());
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

                        for (VDS vds : list) {
                            Pair<List<String>, List<String>> problematicNics =
                                    VdsUpdateRunTimeInfo.determineProblematicNics(getDbFacade().getInterfaceDao()
                                            .getAllInterfacesForVds(vds.getId()), getDbFacade().getNetworkDao()
                                            .getAllForCluster(vds.getVdsGroupId()));
                            if (problematicNics.getFirst().isEmpty()) {
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
                        arg.getId(), new Guid(arg.getStoragePoolId().getUuid()));
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
    <T extends BusinessEntity<Guid>> void check(final AutoRecoverDAO<T> dao,
            final VdcActionType actionType,
            final DoWithClosure<T, VdcActionParametersBase> paramsCallback,
            final FilterClosure<T> filter,
            final String logMsg) {
        if (!shouldPerformRecoveryOnType(logMsg)) {
            log.info("Autorecovering " + logMsg + " is disabled, skipping");
            return;
        }
        log.debugFormat("Checking autorecoverable {0}" , logMsg);
        final List<T> fails = filter.filter(dao.listFailedAutorecoverables());
        if (fails.size() > 0) {
            final BackendInternal backend = getBackend();
            log.info("Autorecovering " + fails.size() + " " + logMsg);
            for (final T fail : fails) {
                log.info("Autorecovering " + logMsg + " id: " + fail.getId() + getHostName(fail));
                final VdcActionParametersBase actionParams = paramsCallback.doWith(fail);
                actionParams.setShouldBeLogged(true);
                backend.runInternalAction(actionType, actionParams);
            }
        }
        log.debugFormat("Checking autorecoverable {0} done",logMsg);
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
                Config.<Map<String, String>> GetValue(ConfigValues.AutoRecoveryAllowedTypes);
        String isAllowed = allowedRecoveryTypesFromConfig.get(type);
        if (isAllowed != null) {
            return Boolean.parseBoolean(isAllowed);
        }

        return true;
    }

}
