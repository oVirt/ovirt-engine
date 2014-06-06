package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
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

    protected StorageDomain newMasterStorageDomain;
    protected Guid newMasterStorageDomainId;

    protected boolean isLastMaster;

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected ReconstructMasterDomainCommand(Guid commandId) {
        super(commandId);
    }

    public ReconstructMasterDomainCommand(T parameters) {
        this(parameters, null);
    }

    public ReconstructMasterDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    protected StorageDomain getNewMasterStorageDomain() {
        if (newMasterStorageDomainId == null) {
            getNewMasterStorageDomainId();
        }
        return newMasterStorageDomain;
    }

    protected Guid getNewMasterStorageDomainId() {
        if (newMasterStorageDomainId == null) {
            newMasterStorageDomain = electNewMaster(true, getParameters().isCanChooseInactiveDomainAsMaster(),
                    getParameters().isCanChooseCurrentMasterAsNewMaster());
            newMasterStorageDomainId = newMasterStorageDomain != null ? newMasterStorageDomain.getId() : Guid.Empty;
        }
        return newMasterStorageDomainId;
    }

    protected StorageDomainValidator createStorageDomainValidator() {
        return new StorageDomainValidator(getStorageDomain());
    }

    protected StoragePoolValidator createStoragePoolValidator() {
        return new StoragePoolValidator(getStoragePool());
    }

    @Override
    protected boolean canDoAction() {
        // This check is done here to handle a race in which the returned domain from
        // getStorageDomain() is with LOCKED status. Having this domain with LOCKED status might
        // cause to the command to apply the compensation data and leave the domain as LOCKED.
        if (!validate(createStorageDomainValidator().isInProcess())) {
            return false;
        }

        if (!validate(createStoragePoolValidator().isAnyDomainInProcess())) {
            return false;
        }

        return initializeVds();
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECONSTRUCT_MASTER);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
    }

    protected boolean reconstructMaster() {
        isLastMaster = proceedStorageDomainTreatmentByDomainType(getNewMasterStorageDomain(), false);

        // To issue a reconstructMaster you need to set the domain inactive unless the selected domain is the current master
        if (getParameters().isInactive() && !getStorageDomain().getId().equals(getNewMasterStorageDomainId())) {
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

        if (isLastMaster) {
            return stopSpm();
        }

        boolean commandSucceeded = stopSpm();

        final List<String> disconnectPoolFormats = Config.<List<String>> getValue(
                ConfigValues.DisconnectPoolOnReconstruct);

        if (commandSucceeded && disconnectPoolFormats.contains(getNewMasterStorageDomain().getStorageFormat().getValue())) {
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
                        getStoragePool().getName(), getNewMasterStorageDomainId(), domains,
                        getStoragePool().getMasterDomainVersion())).getSucceeded();

    }

    @Override
    protected void executeCommand() {
        boolean reconstructOpSucceeded = reconstructMaster();
        setActionReturnValue(reconstructOpSucceeded);
        connectAndRefreshAllUpHosts(reconstructOpSucceeded);
        if (!isLastMaster && reconstructOpSucceeded && !FeatureSupported.ovfStoreOnAnyDomain(getStoragePool().getCompatibilityVersion())) {
            // all vms/templates metadata should be copied to the new master domain, so we need
            // to perform increment of the db version for all the vms in the storage pool.
            // currently this method is used for both templates and vms.
            getVmStaticDAO().incrementDbGenerationForAllInStoragePool(getStoragePoolId());
        }
        if (isLastMaster) {
            getCompensationContext().resetCompensation();
        }
        setSucceeded(!isLastMaster && reconstructOpSucceeded);

        if (getSucceeded()) {
            runVdsCommand(VDSCommandType.MarkPoolInReconstructMode,
                            new IrsBaseVDSCommandParameters(getStoragePoolId()));
        }
    }

    protected boolean stopSpm() {
        boolean commandSucceeded = true;
        if (getStoragePool().getSpmVdsId() != null) {
            // if spm host id is different from selected host get the spm
            // in order to try and perform stop spm
            VDS spm = null;
            if (getStoragePool().getSpmVdsId().equals(getVds().getId())) {
                spm = getVds();
            } else {
                spm = DbFacade.getInstance()
                        .getVdsDao()
                        .get(getStoragePool().getSpmVdsId());
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
        StorageDomain masterDomain = getNewMasterStorageDomain();
        if (vds.getId().equals(getVds().getId())
                || StorageHelperDirector.getInstance().getItem(masterDomain.getStorageType())
                        .connectStorageToDomainByVdsId(masterDomain, vds.getId())) {
            return true;
        }
        log.error("Error while trying connect host {} to the needed storage server during the reinitialization"
                        + " of Data Center '{}'",
                vds.getId(),
                getStoragePool().getId());
        return false;
    }

    private void connectAndRefreshAllUpHosts(final boolean commandSucceeded) {
        if (isLastMaster || !commandSucceeded) {
            log.warn("skipping connect and refresh for all hosts, last master '{}', command status '{}'",
                    isLastMaster, commandSucceeded);
            return;
        }

        List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
        for (final VDS vds : getAllRunningVdssInPool()) {
            tasks.add(new Callable<Void>() {
                @Override
                public Void call() {
                    try {
                        if (!connectVdsToNewMaster(vds)) {
                            log.warn("failed to connect vds '{}' to the new master '{}'",
                                    vds.getId(), getNewMasterStorageDomainId());
                            return null;
                        }

                        List<StoragePoolIsoMap> storagePoolIsoMap = getStoragePoolIsoMapDAO()
                                .getAllForStoragePool(getStoragePool().getId());
                        try {
                            runVdsCommand(
                                    VDSCommandType.ConnectStoragePool,
                                    new ConnectStoragePoolVDSCommandParameters(vds, getStoragePool(),
                                            getNewMasterStorageDomainId(), storagePoolIsoMap, true));
                        } catch (VdcBLLException ex) {
                            if (VdcBllErrors.StoragePoolUnknown == ex.getVdsError().getCode()) {
                                VDSReturnValue returnVal = runVdsCommand(
                                        VDSCommandType.ConnectStoragePool,
                                        new ConnectStoragePoolVDSCommandParameters(vds, getStoragePool(),
                                                getNewMasterStorageDomainId(), storagePoolIsoMap));
                                if (!returnVal.getSucceeded()) {
                                    log.error("Post reconstruct actions (connectPool) did not complete on host '{}' in the pool. error {}",
                                            vds.getId(),
                                            returnVal.getVdsError().getMessage());
                                }
                            } else {
                                log.error("Post reconstruct actions (refreshPool)"
                                        + " did not complete on host '{}' in the pool. error {}",
                                        vds.getId(),
                                        ex.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Post reconstruct actions (connectPool,refreshPool,disconnect storage)"
                                + " did not complete on host '{}' in the pool: {}",
                                vds.getId(),
                                e.getMessage());
                        log.debug("Exception", e);
                    }
                    return null;
                }
            });
        }
        ThreadPoolUtil.invokeAll(tasks);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? isLastMaster ? AuditLogType.RECONSTRUCT_MASTER_FAILED_NO_MASTER
                : AuditLogType.RECONSTRUCT_MASTER_DONE : AuditLogType.RECONSTRUCT_MASTER_FAILED;
    }
}
