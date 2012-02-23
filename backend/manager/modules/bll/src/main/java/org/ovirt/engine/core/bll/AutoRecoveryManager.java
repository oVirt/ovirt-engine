package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AutoRecoverDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

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
        SchedulerUtilQuartzImpl.getInstance().scheduleACronJob(this, "onTimer",
                new Class<?>[] {}, new Object[] {}, Config.<String> GetValue(ConfigValues.AutoRecoverySchedule));
    }

    /**
     * Called by the scheduler in regular intervals.
     */
    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        DbFacade dbFacade = DbFacade.getInstance();
        check(dbFacade.getVdsDAO(),
                VdcActionType.ActivateVds,
                new DoWithClosure<VDS, VdcActionParametersBase>() {
                    @Override
                    public VdcActionParametersBase doWith(final VDS arg) {
                        final VdsActionParameters params = new VdsActionParameters(arg.getId());
                        params.setRunSilent(true);
                        return params;
                    }
                }, "hosts");
        check(dbFacade.getStorageDomainDAO(),
                VdcActionType.ActivateStorageDomain,
                new DoWithClosure<storage_domains, VdcActionParametersBase>() {
                    @Override
                    public VdcActionParametersBase doWith(final storage_domains arg) {
                        final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(
                                arg.getId(), new Guid(arg.getstorage_pool_id().getUuid()));
                        params.setRunSilent(true);
                        return params;
                    }
                }, "storage domains");
    }

    /**
     * Check all the failing resources retrieved from the dao.
     * @param dao               the dao to get the list of failing resources from
     * @param actionType        autorecovery action
     * @param paramsCallback    a closure to create the parameters for the autorecovery action
     * @param logMsg            a user-readable name for the failing resource type
     * @param counters          counters for the failing resources to avoid too frequent auditlog
     */
    static <T extends BusinessEntity<Guid>> void check(final AutoRecoverDAO<T> dao,
            final VdcActionType actionType,
            final DoWithClosure<T, VdcActionParametersBase> paramsCallback,
            final String logMsg) {
        log.info("Checking autorecoverable " + logMsg);
        final List<T> fails = dao.listFailedAutorecoverables();
        final BackendInternal backend = Backend.getInstance();
        for (final T fail : fails) {
            final VdcActionParametersBase actionParams = paramsCallback.doWith(fail);
            actionParams.setShouldBeLogged(true);
            backend.runInternalAction(actionType, actionParams);
        }
        log.info("Checking autorecoverable " + logMsg + " done");
    }

    private interface DoWithClosure<T, R> {
        R doWith(T arg);
    }

}
