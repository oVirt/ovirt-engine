package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ReconstructMasterVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

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
        super(parameters, null);
    }

    public ReconstructMasterDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        _newMasterStorageDomainId = parameters.getNewMasterDomainId();
        canChooseInactiveDomainAsMaster = parameters.isCanChooseInactiveDomainAsMaster();
        canChooseCurrentMasterAsNewMaster = parameters.isCanChooseCurrentMasterAsNewMaster();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    private boolean checkIsDomainLocked(StoragePoolIsoMap domainMap) {
        if (domainMap.getStatus() != null && domainMap.getStatus().isStorageDomainInProcess()) {
            addInvalidSDStatusMessage(StorageDomainStatus.Locked);
            return true;
        }

        return false;
    }

    @Override
    protected boolean canDoAction() {
        // This check is done here to handle a race in which the returned domain from
        // getStorageDomain() is with LOCKED status. Having this domain with LOCKED status might
        // cause to the command to apply the compensation data and leave the domain as LOCKED.
        if (checkIsDomainLocked(getStorageDomain().getStoragePoolIsoMapData())) {
            return false;
        }

        List<StoragePoolIsoMap> poolDomains = DbFacade.getInstance()
                .getStoragePoolIsoMapDao().getAllForStoragePool(getStoragePool().getId());
        for (StoragePoolIsoMap poolDomain : poolDomains) {
            if (checkIsDomainLocked(poolDomain)) {
                return false;
            }
        }

        return initializeVds();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECONSTRUCT_MASTER);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
    }

    protected void addInvalidSDStatusMessage(StorageDomainStatus status) {
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);
        addCanDoActionMessageVariable("status", status);
    }


    protected boolean reconstructMaster() {
        proceedStorageDomainTreatmentByDomainType(true);

        // To issue a reconstructMaster you need to set the domain inactive unless the selected domain is the current master
        if (getParameters().isInactive() && !getStorageDomain().getId().equals(_newMasterStorageDomainId)) {
            executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    setStorageDomainStatus(StorageDomainStatus.Inactive, getCompensationContext());
                    calcStoragePoolStatusByDomainsStatus();
                    getCompensationContext().stateChanged();
                    return null;
                }
            });
        }

        if (_isLastMaster) {
            return stopSpm();
        }

        boolean commandSucceeded = stopSpm();

        final List<String> disconnectPoolFormats = Config.<List<String>> getValue(
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
                        getStoragePool().getName(), _newMasterStorageDomainId, domains,
                        getStoragePool().getmaster_domain_version())).getSucceeded();

    }

    @Override
    protected void executeCommand() {
        boolean reconstructOpSucceeded = reconstructMaster();
        setActionReturnValue(reconstructOpSucceeded);
        connectAndRefreshAllUpHosts(reconstructOpSucceeded);
        if (!_isLastMaster && reconstructOpSucceeded && !FeatureSupported.ovfStoreOnAnyDomain(getStoragePool().getcompatibility_version())) {
            // all vms/templates metadata should be copied to the new master domain, so we need
            // to perform increment of the db version for all the vms in the storage pool.
            // currently this method is used for both templates and vms.
            getVmStaticDAO().incrementDbGenerationForAllInStoragePool(getStoragePoolId());
        }
        if (_isLastMaster) {
            getCompensationContext().resetCompensation();
        }
        setSucceeded(!_isLastMaster && reconstructOpSucceeded);

        if (getSucceeded()) {
            runVdsCommand(VDSCommandType.MarkPoolInReconstructMode,
                            new IrsBaseVDSCommandParameters(getStoragePoolId()));
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
                commandSucceeded = runVdsCommand(VDSCommandType.ResetIrs, tempVar2).getSucceeded();

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
        if (isPerformConnectOps) {
            List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
            for (final VDS vds : getAllRunningVdssInPool()) {
                tasks.add(new Callable<Void>() {

                    @Override
                    public Void call() {
                        try {
                            if (isPerformConnectOps && connectVdsToNewMaster(vds)) {
                                List<StoragePoolIsoMap> storagePoolIsoMap = getStoragePoolIsoMapDAO()
                                        .getAllForStoragePool(getStoragePool().getId());
                                try {
                                    runVdsCommand(
                                            VDSCommandType.ConnectStoragePool,
                                            new ConnectStoragePoolVDSCommandParameters(vds, getStoragePool(),
                                                    _newMasterStorageDomainId, storagePoolIsoMap, true));
                                } catch (VdcBLLException ex) {
                                    if (VdcBllErrors.StoragePoolUnknown == ex.getVdsError().getCode()) {
                                        VDSReturnValue returnVal = runVdsCommand(
                                                VDSCommandType.ConnectStoragePool,
                                                new ConnectStoragePoolVDSCommandParameters(vds, getStoragePool(),
                                                        _newMasterStorageDomainId, storagePoolIsoMap));
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
