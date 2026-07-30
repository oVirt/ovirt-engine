package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.DeactivateStorageDomainCommand;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.SpmStatus;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DisconnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ReconstructMasterVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ResetIrsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ReconstructMasterDomainCommand<T extends ReconstructMasterParameters> extends
        DeactivateStorageDomainCommand<T> {

    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private ResourceManager resourceManager;

    protected StorageDomain newMasterStorageDomain;
    protected Guid newMasterStorageDomainId;
    protected boolean isLastMaster;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public ReconstructMasterDomainCommand(Guid commandId) {
        super(commandId);
    }

    public ReconstructMasterDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean initializeVds() {
        return super.initializeVds();
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

    @Override
    protected StoragePoolValidator createStoragePoolValidator() {
        return super.createStoragePoolValidator();
    }

    @Override
    protected boolean validate() {
        // This check is done here to handle a race in which the returned domain from
        // getStorageDomains() is with LOCKED status. Having this domain with LOCKED status might
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
        addValidationMessage(EngineMessage.VAR__ACTION__RECONSTRUCT_MASTER);
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
    }

    protected boolean reconstructMaster() {
        updateStoragePoolMasterDomainVersionInDiffTransaction();
        isLastMaster = proceedStorageDomainTreatmentByDomainType(getNewMasterStorageDomain(), false);

        // To issue a reconstructMaster you need to set the domain inactive unless the selected domain is the current master
        if (getParameters().isInactive() && !getStorageDomain().getId().equals(getNewMasterStorageDomainId())) {
            executeInNewTransaction(() -> {
                setStorageDomainStatus(StorageDomainStatus.Inactive, getCompensationContext());
                calcStoragePoolStatusByDomainsStatus();
                getCompensationContext().stateChanged();
                return null;
            });
        }

        if (isLastMaster) {
            return stopSpm();
        }

        boolean commandSucceeded = stopSpm();

        if (commandSucceeded) {
            try {
                commandSucceeded = runVdsCommand(
                        VDSCommandType.DisconnectStoragePool,
                        new DisconnectStoragePoolVDSCommandParameters(getVds().getId(),
                                getStoragePool().getId(), getVds().getVdsSpmId())
                ).getSucceeded();
            } catch (EngineException e) {
                // In case SpmVdsId is null, its value should be set.
                if (e.getErrorCode() == EngineError.IsSpm) {
                    setSpmFromVdsm();
                }
                throw e;
            }
        }

        if (!commandSucceeded) {
            return false;
        }

        List<StoragePoolIsoMap> domains = storagePoolIsoMapDao.getAllForStoragePool(getStoragePool().getId());

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
        if (isLastMaster) {
            getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
        }
        setSucceeded(!isLastMaster && reconstructOpSucceeded);

        if (getSucceeded()) {
            runVdsCommand(VDSCommandType.MarkPoolInReconstructMode,
                            new IrsBaseVDSCommandParameters(getStoragePoolId()));
        }
    }

    protected boolean stopSpm() {
        boolean commandSucceeded = true;
        StoragePool pool = getStoragePool();
        if (pool.getSpmVdsId() != null) {
            // if spm host id is different from selected host get the spm
            // in order to try and perform stop spm
            VDS spm;
            if (pool.getSpmVdsId().equals(getVds().getId())) {
                spm = getVds();
            } else {
                spm = vdsDao.get(pool.getSpmVdsId());
            }
            if (spm != null) {
                ResetIrsVDSCommandParameters resetIrsParams = new ResetIrsVDSCommandParameters(
                        pool.getId(), spm.getId());
                resetIrsParams.setIgnoreStopFailed(true);
                commandSucceeded = runVdsCommand(VDSCommandType.ResetIrs, resetIrsParams).getSucceeded();
                if (commandSucceeded) {
                    commandSucceeded = verifyLeaseCleared(spm);
                }

                if (commandSucceeded && spm.getStatus() == VDSStatus.Up) {
                    setVdsId(spm.getId());
                    setVds(spm);
                }
            }
        }
        return commandSucceeded;
    }

    /**
     * Verify the recorded SPM's on-disk lease is cleared before trusting the
     * ResetIrs success (ignoreStopFailed can report success without the SPM
     * actually stopping). Returns true if a replacement may be elected; logs
     * and audits the refusal (HELD / UNVERIFIED) and returns false otherwise.
     */
    private boolean verifyLeaseCleared(VDS spm) {
        SpmStatusResult lease = readRecordedSpmLease(getStoragePool().getId(), spm.getId());
        RecordedSpmLeaseState leaseState = classifyLease(lease, spm.getVdsSpmId());
        if (leaseState == RecordedSpmLeaseState.CLEARED) {
            auditLeaseOwnedByOtherHost(spm, lease);
            return true;
        }
        AuditLogable alb = createAuditLogableForHost(spm);
        if (leaseState == RecordedSpmLeaseState.HELD) {
            log.warn("Recorded SPM host '{}' (vdsSpmId={}) on-disk lease "
                    + "still held during reconstruct (owner spmId={}); "
                    + "refusing to elect a replacement SPM to avoid a dual-writer.",
                    spm.getName(), spm.getVdsSpmId(), lease.getSpmId());
            alb.addCustomValue("OwnerSpmId", String.valueOf(lease.getSpmId()));
            auditLog(alb, AuditLogType.VDS_ALERT_SPM_RECONSTRUCT_REFUSED_LEASE_HELD);
        } else {
            log.warn("Recorded SPM host '{}' (vdsSpmId={}) on-disk lease "
                    + "could not be confirmed cleared during reconstruct "
                    + "(no reachable peer or inconclusive read); refusing "
                    + "to elect a replacement SPM to avoid a dual-writer.",
                    spm.getName(), spm.getVdsSpmId());
            auditLog(alb, AuditLogType.VDS_ALERT_SPM_RECONSTRUCT_REFUSED_LEASE_UNVERIFIED);
        }
        return false;
    }

    /**
     * When the classification is CLEARED only because the on-disk lease is
     * owned by a different host than the recorded SPM (generation mismatch),
     * proceeding is safe for this guard's purpose (the recorded SPM holds no
     * lease), but the on-disk owner id is exactly the evidence an operator
     * needs to spot a live rival SPM on the old master domain -- so log and
     * audit it distinctly. A positively-free lease ({@code spmId == -1})
     * stays silent.
     */
    private void auditLeaseOwnedByOtherHost(VDS spm, SpmStatusResult lease) {
        if (lease == null || lease.getSpmId() <= 0) {
            return;
        }
        log.info("Recorded SPM host '{}' (vdsSpmId={}) on-disk lease is owned "
                + "by a different host (owner spmId={}) during reconstruct; "
                + "the recorded SPM is not the lease holder, proceeding to "
                + "elect a replacement SPM.",
                spm.getName(), spm.getVdsSpmId(), lease.getSpmId());
        AuditLogable alb = createAuditLogableForHost(spm);
        alb.addCustomValue("OwnerSpmId", String.valueOf(lease.getSpmId()));
        auditLog(alb, AuditLogType.VDS_SPM_RECONSTRUCT_LEASE_OWNED_BY_OTHER);
    }

    /**
     * Best-effort on-disk SPM lease read via a reachable Up peer, excluding the
     * host identified by {@code excludeHostId} (the recorded SPM under
     * inspection -- whose own report is exactly what cannot be trusted right
     * now). Returns the first successful read, or {@code null} if no peer is
     * reachable or every read fails; callers must treat {@code null} as
     * "no evidence either way", not as "free".
     *
     * <p>Only the on-disk {@code spmId} field of {@link SpmStatusResult} is
     * authoritative. A peer's in-memory {@code spmStatus} is always
     * {@code Free} (only the SPM host itself ever reports {@code SPM}) and
     * must never be used to answer this question.
     */
    SpmStatusResult readRecordedSpmLease(Guid poolId, Guid excludeHostId) {
        List<VDS> peers = vdsDao.getAllForStoragePoolAndStatuses(poolId, EnumSet.of(VDSStatus.Up));
        for (VDS peer : peers) {
            if (peer.getId().equals(excludeHostId)) {
                continue;
            }
            try {
                VDSReturnValue ret = resourceManager.runVdsCommand(
                        VDSCommandType.SpmStatus,
                        new SpmStatusVDSCommandParameters(peer.getId(), poolId));
                if (ret != null && ret.getSucceeded()
                        && ret.getReturnValue() instanceof SpmStatusResult) {
                    return (SpmStatusResult) ret.getReturnValue();
                }
            } catch (Exception e) {
                log.debug("SpmStatus on-disk read on peer '{}' failed: {}",
                        peer.getName(), e.getMessage());
            }
        }
        return null;
    }

    /**
     * Outcome of reading the recorded SPM's on-disk lease, used to decide
     * whether a replacement SPM may be elected:
     * <ul>
     *   <li>{@link #CLEARED} -- positively free ({@code spmId == -1}), or the
     *       on-disk lease is owned by a different host than the recorded SPM.
     *       The mismatch case is still safe for this guard's purpose: the
     *       dual-writer hazard it prevents is the recorded SPM specifically
     *       (the host ResetIrs just failed to stop), and a lease owned by a
     *       different positive id proves that host holds no lease to write
     *       through. An owner that is itself a live rival SPM on the old
     *       master domain would be a pre-existing split-brain that unpatched
     *       reconstructs never guarded either; the owner id is audited
     *       distinctly so operators can detect it (see
     *       {@code verifyLeaseCleared});</li>
     *   <li>{@link #HELD} -- owned by the recorded SPM, or by an unknown host
     *       when the recorded id is itself unknown; a replacement must not be
     *       elected;</li>
     *   <li>{@link #UNVERIFIED} -- no peer could read the lease, or the read was
     *       inconclusive (vdsm's {@code -5} sentinel, returned when the peer is
     *       not connected to the pool's master domain); a replacement must not
     *       be elected on the mere absence of evidence.</li>
     * </ul>
     */
    enum RecordedSpmLeaseState {
        CLEARED, HELD, UNVERIFIED
    }

    /**
     * Classify the recorded SPM's on-disk lease read. A {@code null} lease
     * (no reachable peer) is {@link RecordedSpmLeaseState#UNVERIFIED}.
     */
    static RecordedSpmLeaseState classifyLease(SpmStatusResult onDisk, Integer recordedSpmHostId) {
        if (onDisk == null) {
            return RecordedSpmLeaseState.UNVERIFIED;
        }
        int spmId = onDisk.getSpmId();
        if (spmId == -1) {
            return RecordedSpmLeaseState.CLEARED;
        }
        if (spmId <= 0) {
            return RecordedSpmLeaseState.UNVERIFIED;
        }
        if (recordedSpmHostId != null && spmId != recordedSpmHostId) {
            return RecordedSpmLeaseState.CLEARED;
        }
        return RecordedSpmLeaseState.HELD;
    }

    /**
     * Like AuditLogableImpl.createHostEvent, but repeatable: the refusal and
     * mismatch audits must be re-emitted on every reconstruct attempt that
     * hits them, instead of being deduplicated, since each attempt is a
     * fresh decision an operator may need to react to.
     */
    private AuditLogable createAuditLogableForHost(VDS vds) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        logable.setClusterId(vds.getClusterId());
        logable.setClusterName(vds.getClusterName());
        logable.setRepeatable(true);
        return logable;
    }

    private void setSpmFromVdsm() {
        // In case the ReconstructMaster command fails a few times, the SpmVdsId value will become null,
        // and the SPM host is no longer marked as SPM.
        // In that case, the ResetIrs and SpmStop VDS command aren't being called,
        // which causes an IsSpm error on DisconnectStoragePool.
        // TODO improve this further by parallelizing (https://bugzilla.redhat.com/1905244)
        for (VDS vds : getAllRunningVdssInPool()) {
            try {
                VDSReturnValue statusResult = resourceManager.runVdsCommand(VDSCommandType.SpmStatus,
                        new SpmStatusVDSCommandParameters(vds.getId(), getStoragePoolId()));
                if (statusResult != null && statusResult.getSucceeded() &&
                        ((SpmStatusResult) statusResult.getReturnValue()).getSpmStatus() == SpmStatus.SPM) {
                    getStoragePool().setSpmVdsId(vds.getId());
                    storagePoolDao.update(getStoragePool());
                    break;
                }
            } catch (Exception e) {
                log.error("Could not get spm status on host '{}' for reconstructMaster: {}",
                        vds.getId(), e.getMessage());
            }
        }
    }

    /**
     * performs any connect related operations if needed before attempting
     * to connect/refresh pool information.
     */
    private boolean connectVdsToNewMaster(VDS vds) {
        StorageDomain masterDomain = getNewMasterStorageDomain();
        if (vds.getId().equals(getVds().getId())
                || storageHelperDirector.getItem(masterDomain.getStorageType())
                        .connectStorageToDomainByVdsId(masterDomain, vds.getId())) {
            return true;
        }
        log.error("Error while trying connect host {} to the needed storage server during the reinitialization"
                        + " of Data Center '{}'",
                vds.getId(),
                getStoragePool().getId());
        return false;
    }

    @Override
    protected List<VDS> getAllRunningVdssInPool() {
        return vdsDao.getAllForStoragePoolAndStatus(getStoragePool().getId(), VDSStatus.Up);
    }

    private void connectAndRefreshAllUpHosts(final boolean commandSucceeded) {
        if (isLastMaster || !commandSucceeded) {
            log.warn("skipping connect and refresh for all hosts, last master '{}', command status '{}'",
                    isLastMaster, commandSucceeded);
            return;
        }

        List<Callable<Void>> tasks = new ArrayList<>();
        for (final VDS vds : getAllRunningVdssInPool()) {
            tasks.add(() -> {
                try {
                    if (!connectVdsToNewMaster(vds)) {
                        log.warn("failed to connect vds '{}' to the new master '{}'",
                                vds.getId(), getNewMasterStorageDomainId());
                        return null;
                    }

                    List<StoragePoolIsoMap> storagePoolIsoMap =
                            storagePoolIsoMapDao.getAllForStoragePool(getStoragePool().getId());
                    try {
                        runVdsCommand(
                                VDSCommandType.ConnectStoragePool,
                                new ConnectStoragePoolVDSCommandParameters(vds, getStoragePool(),
                                        getNewMasterStorageDomainId(), storagePoolIsoMap, true));
                    } catch (EngineException ex) {
                        if (EngineError.StoragePoolUnknown == ex.getVdsError().getCode()) {
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
