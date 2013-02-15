package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MarkPoolInReconstructModeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ReconstructMarkAction;
import org.ovirt.engine.core.common.vdscommands.ReconstructMasterVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.RefreshStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@SuppressWarnings("serial")
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ReconstructMasterDomainCommand<T extends ReconstructMasterParameters> extends
        DeactivateStorageDomainCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ReconstructMasterDomainCommand(Guid commandId) {
        super(commandId);
    }

    public ReconstructMasterDomainCommand(T parameters) {
        super(parameters);
        _newMasterStorageDomainId = parameters.getNewMasterDomainId();
    }

    @Override
    protected boolean canDoAction() {
        List<StoragePoolIsoMap> poolDomains = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(getStoragePool().getId());
        for (StoragePoolIsoMap poolDomain : poolDomains) {
            if (poolDomain.getstatus() == StorageDomainStatus.Locked) {
                addInvalidSDStatusMessage(poolDomain.getstatus());
                return false;
            }
        }

        return InitializeVds();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECONSTRUCT_MASTER);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
    }

    protected void addInvalidSDStatusMessage(StorageDomainStatus status) {
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
        addCanDoActionMessage(String.format("$status %1$s", status));
    }


    protected boolean reconstructMaster() {
        ProceedStorageDomainTreatmentByDomainType(true);

        // To issue a reconstructMaster you need to set the domain inactive
        if (getParameters().isInactive()) {
            executeInNewTransaction(new TransactionMethod<Void>() {
                public Void runInTransaction() {
                    setStorageDomainStatus(StorageDomainStatus.InActive, getCompensationContext());
                    calcStoragePoolStatusByDomainsStatus();
                    getCompensationContext().stateChanged();
                    return null;
                }
            });
        }

        if (_isLastMaster) {
            return stopSpm();
        }

        // Pause the timers for the domain error handling
        runVdsCommand(VDSCommandType.MarkPoolInReconstructMode,
                new MarkPoolInReconstructModeVDSCommandParameters(
                        getStoragePoolId().getValue(), ReconstructMarkAction.ClearJobs));
        boolean commandSucceeded = stopSpm();

        final List<String> disconnectPoolFormats = Config.<List<String>> GetValue(
                ConfigValues.DisconnectPoolOnReconstruct);

        if (commandSucceeded && disconnectPoolFormats.contains(getNewMaster(true).getStorageFormat().getValue())) {
            commandSucceeded = runVdsCommand(
                    VDSCommandType.DisconnectStoragePool,
                    new DisconnectStoragePoolVDSCommandParameters(getVds().getId(),
                            getStoragePool().getId(), getVds().getVdsSpmId())
                    ).getSucceeded();
        }

        if (!commandSucceeded) {
            return false;
        }

        List<StoragePoolIsoMap> domains = getStoragePoolIsoMapDAO()
                .getAllForStoragePool(getStoragePool().getId());

        // set to true here in case of failure in executing/getting answer from the reconstruct vds command,
        // unless we know that the command failed we assume that it succeeded (use by RecoveryStoragePool command in
        // order to avoid detaching domain that is already part of the pool in vdsm).
        setActionReturnValue(true);
        return runVdsCommand(VDSCommandType.ReconstructMaster,
                new ReconstructMasterVDSCommandParameters(getVds().getId(),
                        getVds().getVdsSpmId(), getStoragePool().getId(),
                        getStoragePool().getname(), _newMasterStorageDomainId, domains,
                        getStoragePool().getmaster_domain_version())).getSucceeded();

    }

    @Override
    protected void executeCommand() {
        try {
            boolean reconstructOpSucceeded = reconstructMaster();
            setActionReturnValue(reconstructOpSucceeded);
            connectAndRefreshAllUpHosts(reconstructOpSucceeded);
            if (!_isLastMaster && reconstructOpSucceeded) {
                // all vms/templates metadata should be copied to the new master domain, so we need
                // to perform increment of the db version for all the vms in the storage pool.
                // currently this method is used for both templates and vms.
                getVmStaticDAO().incrementDbGenerationForAllInStoragePool(getStoragePoolId().getValue());
            }
            if(_isLastMaster) {
                getCompensationContext().resetCompensation();
            }
            setSucceeded(!_isLastMaster && reconstructOpSucceeded);
        } finally {
            // reset cache and mark reconstruct for pool as finished
            Backend.getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.MarkPoolInReconstructMode,
                            new MarkPoolInReconstructModeVDSCommandParameters(getStoragePoolId()
                                    .getValue(), ReconstructMarkAction.ClearCache));
        }
    }

    protected boolean stopSpm() {
        boolean commandSucceeded = true;
        if (getStoragePool().getspm_vds_id() != null) {
            // if spm host id is different from selected host get the spm
            // in order to try and perform stop spm
            VDS spm = null;
            if (getStoragePool().getspm_vds_id().equals(getVds().getId())) {
                spm = getVds();
            } else {
                spm = DbFacade.getInstance()
                        .getVdsDao()
                        .get(getStoragePool().getspm_vds_id());
            }
            if (spm != null) {
                ResetIrsVDSCommandParameters tempVar2 = new ResetIrsVDSCommandParameters(
                        getStoragePool().getId(), spm.getId());
                tempVar2.setIgnoreStopFailed(true);
                commandSucceeded = Backend.getInstance().getResourceManager()
                            .RunVdsCommand(VDSCommandType.ResetIrs, tempVar2).getSucceeded();

                // if spm host is up switch to use it in the following logic
                if (spm.getStatus() == VDSStatus.Up) {
                    setVdsId(spm.getId());
                    setVds(spm);
                }
            }
        }
        return commandSucceeded;
    }

    /**
     * performs any connect related operations if needed before attempting
     * to connect/refresh pool information.
     * @param vds
     * @return
     */
    private boolean connectVdsToNewMaster(VDS vds) {
        StorageDomain masterDomain = getNewMaster(false);
        if (vds.getId().equals(getVds().getId())
                || StorageHelperDirector.getInstance().getItem(masterDomain.getStorageType())
                        .connectStorageToDomainByVdsId(masterDomain, vds.getId())) {
            return true;
        }
        log.errorFormat("Error while trying connect host {0} to the needed storage server during the reinitialization of Data Center {1}",
                vds.getId(),
                getStoragePool().getId());
        return false;
    }

    private void connectAndRefreshAllUpHosts(final boolean commandSucceeded) {
        final boolean isPerformConnectOps = !_isLastMaster && commandSucceeded;
        final boolean isPerformDisconnect = !getParameters().isInactive();
        if (isPerformConnectOps || isPerformDisconnect) {
            List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
            for (final VDS vds : getAllRunningVdssInPool()) {
                tasks.add(new Callable<Void>() {

                    @Override
                    public Void call() {
                        try {
                            if (isPerformConnectOps && connectVdsToNewMaster(vds)) {
                                try {
                                    runVdsCommand(
                                            VDSCommandType.RefreshStoragePool,
                                            new RefreshStoragePoolVDSCommandParameters(vds.getId(),
                                                    getStoragePool().getId(),
                                                    _newMasterStorageDomainId,
                                                    getStoragePool().getmaster_domain_version()));
                                } catch (VdcBLLException ex) {
                                    if (VdcBllErrors.StoragePoolUnknown == ex.getVdsError().getCode()) {
                                        VDSReturnValue returnVal = runVdsCommand(
                                                VDSCommandType.ConnectStoragePool,
                                                new ConnectStoragePoolVDSCommandParameters(vds.getId(),
                                                        getStoragePool().getId(), vds.getVdsSpmId(),
                                                        _newMasterStorageDomainId, getStoragePool()
                                                                .getmaster_domain_version()));
                                        if (!returnVal.getSucceeded()) {
                                            log.errorFormat("Post reconstruct actions (connectPool) did not complete on host {0} in the pool. error {1}",
                                                    vds.getId(),
                                                    returnVal.getVdsError().getMessage());
                                        }
                                    } else {
                                        log.errorFormat("Post reconstruct actions (refreshPool)"
                                                + " did not complete on host {0} in the pool. error {1}",
                                                vds.getId(),
                                                ex.getMessage());
                                    }
                                }
                            }
                            // only if we deactivate the storage domain we want to disconnect from it.
                            if (isPerformDisconnect) {
                                StorageHelperDirector.getInstance()
                                        .getItem(getStorageDomain().getStorageType())
                                        .disconnectStorageFromDomainByVdsId(getStorageDomain(), vds.getId());
                            }

                        } catch (Exception e) {
                            log.errorFormat("Post reconstruct actions (connectPool,refreshPool,disconnect storage)"
                                    + " did not complete on host {0} in the pool. error {1}",
                                    vds.getId(),
                                    e.getMessage());
                        }
                        return null;
                    }
                });
            }
            ThreadPoolUtil.invokeAll(tasks);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? _isLastMaster ? AuditLogType.RECONSTRUCT_MASTER_FAILED_NO_MASTER
                : AuditLogType.RECONSTRUCT_MASTER_DONE : AuditLogType.RECONSTRUCT_MASTER_FAILED;
    }
}
