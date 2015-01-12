package org.ovirt.engine.core.bll;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvfDataUpdater {
    private static final Logger log = LoggerFactory.getLogger(OvfDataUpdater.class);
    private static final OvfDataUpdater INSTANCE = new OvfDataUpdater();

    private OvfDataUpdater() {
    }

    public static OvfDataUpdater getInstance() {
        return INSTANCE;
    }

    protected StoragePoolDAO getStoragePoolDao() {
        return DbFacade.getInstance().getStoragePoolDao();
    }

    public void initOvfDataUpdater() {
        SchedulerUtil scheduler = SchedulerUtilQuartzImpl.getInstance();
        scheduler.scheduleAFixedDelayJob(this, "ovfUpdate_timer", new Class[] {},
                new Object[] {}, Config.<Integer> getValue(ConfigValues.OvfUpdateIntervalInMinutes),
                Config.<Integer> getValue(ConfigValues.OvfUpdateIntervalInMinutes), TimeUnit.MINUTES);
        log.info("Initialization of OvfDataUpdater completed successfully.");
    }

    protected void performOvfUpdateForDomain(Guid storagePoolId, Guid domainId) {
        Backend.getInstance().runInternalAction(VdcActionType.ProcessOvfUpdateForStorageDomain,
                new ProcessOvfUpdateForStorageDomainCommandParameters(storagePoolId, domainId));
    }

    protected VdcReturnValueBase performOvfUpdateForStoragePool(Guid storagePoolId) {
        return Backend.getInstance().runInternalAction(VdcActionType.ProcessOvfUpdateForStoragePool, new StoragePoolParametersBase(storagePoolId));
    }

    @OnTimerMethodAnnotation("ovfUpdate_timer")
    public void ovfUpdate_timer() {
        List<StoragePool> storagePools = getStoragePoolDao().getAllByStatus(StoragePoolStatus.Up);
        updateOvfData(storagePools);
    }


    public void updateOvfData(List<StoragePool> storagePools) {
        log.info("Attempting to update VMs/Templates Ovf.");
        for (StoragePool pool : storagePools) {
            VdcReturnValueBase returnValueBase = performOvfUpdateForStoragePool(pool.getId());
            if (!returnValueBase.getSucceeded()) {
                log.error("Exception while trying to update or remove VMs/Templates ovf in Data Center '{}'.", pool.getName());
            }

            if (ovfOnAnyDomainSupported(pool)) {
                log.debug("Attempting to update ovfs in domain in Data Center '{}'",
                        pool.getName());

                Set<Guid> domainsToUpdate = (Set<Guid>) returnValueBase.getActionReturnValue();
                if (domainsToUpdate != null) {
                    for (Guid id : domainsToUpdate) {
                        performOvfUpdateForDomain(pool.getId(), id);
                    }
                } else {
                    log.error("Data Center '{}' domains list for OVF update returned as NULL");
                }
            }
        }
    }

    protected boolean ovfOnAnyDomainSupported(StoragePool pool) {
        return FeatureSupported.ovfStoreOnAnyDomain(pool.getCompatibilityVersion());
    }
}
