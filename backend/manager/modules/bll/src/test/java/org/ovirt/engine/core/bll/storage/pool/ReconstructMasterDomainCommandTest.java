package org.ovirt.engine.core.bll.storage.pool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.storage.pool.ReconstructMasterDomainCommand.RecordedSpmLeaseState;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.businessentities.SpmStatus;
import org.ovirt.engine.core.common.businessentities.SpmStatusResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.SpmStatusVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@MockitoSettings(strictness = Strictness.LENIENT)
public class ReconstructMasterDomainCommandTest extends BaseCommandTest {

    private static final int RECORDED_SPM_ID = 5;

    @Mock
    private StoragePoolIsoMapDao storagePoolIsoMapDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private VDSBrokerFrontend vdsBrokerFrontend;

    @Mock
    private ResourceManager resourceManager;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Spy
    @InjectMocks
    private ReconstructMasterDomainCommand<ReconstructMasterParameters> cmd =
            new ReconstructMasterDomainCommand<>(new ReconstructMasterParameters(), null);

    private Guid storagePoolId = Guid.newGuid();

    private Guid masterStorageDomainId = Guid.newGuid();

    private Guid regularStorageDomainId = Guid.newGuid();

    private StoragePool storagePool;

    private StoragePoolIsoMap masterDomainIsoMap;

    private StoragePoolIsoMap regularDomainIsoMap;

    // ---- stopSpm() fixtures (the recorded SPM differs from the acting host,
    //      as in the incident: acting host runs reconstruct, recorded SPM is
    //      unreachable). ----
    private Guid spmVdsId = Guid.newGuid();
    private Guid actingVdsId = Guid.newGuid();
    private VDS spm;
    private VDS actingVds;
    // a reachable Up peer (not the recorded SPM) used to read the on-disk lease
    private VDS peer;

    @BeforeEach
    public void setUp() {
        initializeStorageDomains();
        initializeStoragePool();
        initializeStoragePoolValidator();
        doReturn(true).when(cmd).initializeVds();
        // ResourceManager.runVdsCommand defaults to a failed return so that a
        // test which forgets to stub a peer does not silently pass; each test
        // overrides the stubs it needs.
        doReturn(failed()).when(resourceManager)
                .runVdsCommand(any(VDSCommandType.class), any(SpmStatusVDSCommandParameters.class));
    }

    private void initializeStorageDomains() {
        masterDomainIsoMap = new StoragePoolIsoMap(masterStorageDomainId, storagePoolId, StorageDomainStatus.Active);
        regularDomainIsoMap = new StoragePoolIsoMap(regularStorageDomainId, storagePoolId, StorageDomainStatus.Active);
        StorageDomain masterStorageDomain = new StorageDomain();
        masterStorageDomain.setStoragePoolIsoMapData(masterDomainIsoMap);
        doReturn(masterStorageDomain).when(cmd).getStorageDomain();
    }

    private void initializeStoragePool() {
        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        doReturn(Arrays.asList(masterDomainIsoMap, regularDomainIsoMap)).when(storagePoolIsoMapDao).getAllForStoragePool(storagePoolId);
        doReturn(storagePool).when(cmd).getStoragePool();
    }

    private void initializeStoragePoolValidator() {
        StoragePoolValidator storagePoolValidator = spy(new StoragePoolValidator(storagePool));
        doReturn(storagePoolIsoMapDao).when(storagePoolValidator).getStoragePoolIsoMapDao();
        doReturn(storagePoolValidator).when(cmd).createStoragePoolValidator();
    }

    private void validateDomainInProcess(StorageDomainStatus masterStatus, StorageDomainStatus regularStatus,
                                                    StorageDomainStatus expectedStatus) {
        masterDomainIsoMap.setStatus(masterStatus);
        regularDomainIsoMap.setStatus(regularStatus);

        ValidateTestUtils.runAndAssertValidateFailure(cmd,
                EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2);

        List<String> messages = cmd.getReturnValue().getValidationMessages();

        assertEquals(messages.get(0), EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
        assertEquals(messages.get(1), String.format("$status %1$s", expectedStatus));
    }

    // ---- validation tests (unchanged) ----

    @Test
    public void testValidateMasterLocked() {
        validateDomainInProcess(StorageDomainStatus.Locked, StorageDomainStatus.Active,
                StorageDomainStatus.Locked);
    }

    @Test
    public void testValidateMasterPreparingForMaintenance() {
        validateDomainInProcess(StorageDomainStatus.PreparingForMaintenance, StorageDomainStatus.Active,
                StorageDomainStatus.PreparingForMaintenance);
    }

    @Test
    public void testValidateRegularLocked() {
        validateDomainInProcess(StorageDomainStatus.Active, StorageDomainStatus.Locked,
                StorageDomainStatus.Locked);
    }

    @Test
    public void testValidateRegularPreparingForMaintenance() {
        validateDomainInProcess(StorageDomainStatus.Active, StorageDomainStatus.PreparingForMaintenance,
                StorageDomainStatus.PreparingForMaintenance);
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }

    // ---- stopSpm() lease-aware guard tests ----

    /**
     * ResetIrs with ignoreStopFailed=true reports success even though the
     * recorded SPM was unreachable and SpmStop never ran. The on-disk lease
     * read via a peer confirms the lease is still held by the recorded SPM,
     * so the reconstruct must be refused to avoid electing a dual SPM.
     */
    @Test
    public void testStopSpmRefusesWhenLeaseStillHeld() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(RECORDED_SPM_ID));

        assertFalse(cmd.stopSpm());
    }

    /**
     * Lease on disk is Free (spmId == -1): the recorded SPM has genuinely
     * released its lease, so the reconstruct may proceed.
     */
    @Test
    public void testStopSpmProceedsWhenLeaseFree() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(-1));

        assertTrue(cmd.stopSpm());
        // positively-free lease proceeds silently: no audit at all
        verify(auditLogDirector, never()).log(any(AuditLogable.class), any(AuditLogType.class));
    }

    /**
     * Lease on disk is owned by a different host (generation mismatch): the
     * recorded SPM's lease was overwritten, so the reconstruct may proceed --
     * and the on-disk owner id must be audited distinctly, so operators can
     * spot a possible live rival SPM on the old master domain.
     */
    @Test
    public void testStopSpmProceedsWhenLeaseOwnedByOther() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(9));

        assertTrue(cmd.stopSpm());

        ArgumentCaptor<AuditLogable> captor = ArgumentCaptor.forClass(AuditLogable.class);
        verify(auditLogDirector).log(captor.capture(),
                same(AuditLogType.VDS_SPM_RECONSTRUCT_LEASE_OWNED_BY_OTHER));
        assertThat(captor.getValue().getCustomValues(),
                hasEntry("ownerspmid", "9"));
        // ...and the mismatch proceed must not be mistaken for a refusal
        verify(auditLogDirector, never()).log(any(AuditLogable.class),
                same(AuditLogType.VDS_ALERT_SPM_RECONSTRUCT_REFUSED_LEASE_HELD));
    }

    /**
     * No peer could read the disk (no Up peer reachable): with no positive
     * evidence the lease is cleared, the reconstruct must refuse rather than
     * elect a replacement over a possibly-still-live SPM.
     */
    @Test
    public void testStopSpmRefusesWhenNoPeerReadable() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskReadUnreachable();

        assertFalse(cmd.stopSpm());
    }

    /**
     * On-disk read returns vdsm's -5 sentinel (peer not connected to the
     * master domain, so no real owner reported): inconclusive, so the
     * reconstruct must refuse.
     */
    @Test
    public void testStopSpmRefusesOnUncertainSentinel() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(-5));

        assertFalse(cmd.stopSpm());
    }

    /**
     * If ResetIrs genuinely failed (not the ignoreStopFailed success), the
     * command already failed; the lease check must not even run.
     */
    @Test
    public void testStopSpmSkipsLeaseCheckWhenResetIrsFails() {
        setupStopSpm();
        resetIrsFails();

        assertFalse(cmd.stopSpm());
        verify(vdsDao, never()).getAllForStoragePoolAndStatuses(any(), any());
    }

    /**
     * No recorded SPM in the pool: nothing to stop, nothing to guard; the
     * command succeeds trivially without invoking the broker or the lease read.
     */
    @Test
    public void testStopSpmNoopWhenNoRecordedSpm() {
        storagePool.setSpmVdsId(null);

        assertTrue(cmd.stopSpm());
        verify(vdsBrokerFrontend, never()).runVdsCommand(any(), any());
        verify(vdsDao, never()).getAllForStoragePoolAndStatuses(any(), any());
    }

    /**
     * Refusing because the lease is positively still held must emit the HELD
     * audit (9036) with the on-disk owner id attached, so an operator can see
     * exactly which host still owns the lease.
     */
    @Test
    public void testStopSpmRefusedHeldEmitsAudit() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(RECORDED_SPM_ID));

        assertFalse(cmd.stopSpm());

        ArgumentCaptor<AuditLogable> captor = ArgumentCaptor.forClass(AuditLogable.class);
        verify(auditLogDirector).log(captor.capture(),
                same(AuditLogType.VDS_ALERT_SPM_RECONSTRUCT_REFUSED_LEASE_HELD));
        assertThat(captor.getValue().getCustomValues(),
                hasEntry("ownerspmid", String.valueOf(RECORDED_SPM_ID)));
    }

    /**
     * Refusing because the read was inconclusive (no reachable peer / -5
     * sentinel) must emit the UNVERIFIED audit (9037), distinct from HELD.
     */
    @Test
    public void testStopSpmRefusedUnverifiedEmitsAudit() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(-5));

        assertFalse(cmd.stopSpm());

        verify(auditLogDirector).log(any(AuditLogable.class),
                same(AuditLogType.VDS_ALERT_SPM_RECONSTRUCT_REFUSED_LEASE_UNVERIFIED));
    }

    /**
     * A proceeding reconstruct repoints the acting host at the recorded SPM
     * only when it is Up, so DisconnectStoragePool/ReconstructMaster run there.
     */
    @Test
    public void testStopSpmSwitchesActingHostToUpSpmWhenProceeding() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(-1));
        spm.setStatus(VDSStatus.Up);

        assertTrue(cmd.stopSpm());
        assertEquals(spmVdsId, cmd.getVdsId());
    }

    /**
     * A refused reconstruct must NOT repoint the acting host at the recorded
     * SPM even when it is Up, since the reconstruct will abort before running
     * any command against it.
     */
    @Test
    public void testStopSpmDoesNotSwitchActingHostWhenRefused() {
        setupStopSpm();
        resetIrsSucceeds();
        onDiskRead(spmId(RECORDED_SPM_ID));
        spm.setStatus(VDSStatus.Up);

        assertFalse(cmd.stopSpm());
        assertEquals(actingVdsId, cmd.getVdsId());
    }

    // ---- readRecordedSpmLease: peer selection / fallback ----

    @Test
    public void testSinglePeerReturnsItsResult() {
        Guid poolId = Guid.newGuid();
        VDS peer = peerVds();
        SpmStatusResult expected = spmId(RECORDED_SPM_ID);
        doReturn(List.of(peer)).when(vdsDao).getAllForStoragePoolAndStatuses(eq(poolId), any());
        doReturn(success(expected)).when(resourceManager).runVdsCommand(
                eq(VDSCommandType.SpmStatus), paramsFor(peer.getId()));

        assertEquals(expected, cmd.readRecordedSpmLease(poolId, Guid.newGuid()));
    }

    /**
     * The excluded host (the recorded SPM under inspection) must never be
     * queried; the loop must move on to the next peer and return its result.
     */
    @Test
    public void testExcludedHostSkippedFallsToNextPeer() {
        Guid poolId = Guid.newGuid();
        VDS excluded = peerVds();
        VDS next = peerVds();
        SpmStatusResult nextResult = spmId(7);
        doReturn(List.of(excluded, next)).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(poolId), any());
        doReturn(success(nextResult)).when(resourceManager).runVdsCommand(
                eq(VDSCommandType.SpmStatus), paramsFor(next.getId()));

        assertEquals(nextResult, cmd.readRecordedSpmLease(poolId, excluded.getId()));

        verify(resourceManager, never()).runVdsCommand(
                eq(VDSCommandType.SpmStatus), paramsFor(excluded.getId()));
    }

    /**
     * If the excluded host is the only Up peer, there is nobody to ask;
     * return empty.
     */
    @Test
    public void testExcludedHostIsOnlyPeerReturnsEmpty() {
        Guid poolId = Guid.newGuid();
        VDS excluded = peerVds();
        doReturn(List.of(excluded)).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(poolId), any());

        assertNull(cmd.readRecordedSpmLease(poolId, excluded.getId()));

        verify(resourceManager, never()).runVdsCommand(
                any(VDSCommandType.class), any(SpmStatusVDSCommandParameters.class));
    }

    /**
     * The peer-fallback loop must try the next peer when one throws, not give
     * up on the first failure.
     */
    @Test
    public void testFirstPeerThrowsSecondPeerSucceeds() {
        Guid poolId = Guid.newGuid();
        VDS first = peerVds();
        VDS second = peerVds();
        SpmStatusResult secondResult = spmId(RECORDED_SPM_ID);
        doReturn(List.of(first, second)).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(poolId), any());

        doThrow(new RuntimeException("peer unreachable"))
                .doReturn(success(secondResult))
                .when(resourceManager).runVdsCommand(
                        any(VDSCommandType.class), any(SpmStatusVDSCommandParameters.class));

        assertEquals(secondResult, cmd.readRecordedSpmLease(poolId, Guid.newGuid()));
    }

    /**
     * The loop must also try the next peer when a peer's RPC completes but
     * reports failure (getSucceeded() == false), not just on an exception.
     */
    @Test
    public void testFirstPeerFailsSecondPeerSucceeds() {
        Guid poolId = Guid.newGuid();
        VDS first = peerVds();
        VDS second = peerVds();
        SpmStatusResult secondResult = spmId(-1);
        doReturn(List.of(first, second)).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(poolId), any());

        doReturn(failed())
                .doReturn(success(secondResult))
                .when(resourceManager).runVdsCommand(
                        any(VDSCommandType.class), any(SpmStatusVDSCommandParameters.class));

        assertEquals(secondResult, cmd.readRecordedSpmLease(poolId, Guid.newGuid()));
    }

    @Test
    public void testNoPeersReturnsEmpty() {
        Guid poolId = Guid.newGuid();
        doReturn(Collections.emptyList()).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(poolId), any());

        assertNull(cmd.readRecordedSpmLease(poolId, Guid.newGuid()));
    }

    @Test
    public void testAllPeersFailReturnsEmpty() {
        Guid poolId = Guid.newGuid();
        doReturn(List.of(peerVds(), peerVds())).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(poolId), any());

        assertNull(cmd.readRecordedSpmLease(poolId, Guid.newGuid()));
    }

    // ---- classifyLease: pure predicate ----

    @Test
    public void testFreeLeaseCleared() {
        assertEquals(RecordedSpmLeaseState.CLEARED,
                ReconstructMasterDomainCommand.classifyLease(spmId(-1), RECORDED_SPM_ID));
    }

    @Test
    public void testHeldByRecordedSpm() {
        assertEquals(RecordedSpmLeaseState.HELD,
                ReconstructMasterDomainCommand.classifyLease(spmId(RECORDED_SPM_ID), RECORDED_SPM_ID));
    }

    @Test
    public void testHeldByOtherHostCleared() {
        assertEquals(RecordedSpmLeaseState.CLEARED,
                ReconstructMasterDomainCommand.classifyLease(spmId(9), RECORDED_SPM_ID));
    }

    /**
     * Recorded SPM host id unknown but the on-disk lease has a valid owner:
     * be conservative, treat as held (avoid a false-positive dual-writer).
     */
    @Test
    public void testRecordedIdNullLeasePresentHeld() {
        assertEquals(RecordedSpmLeaseState.HELD,
                ReconstructMasterDomainCommand.classifyLease(spmId(RECORDED_SPM_ID), null));
    }

    @Test
    public void testRecordedIdNullLeaseFreeCleared() {
        assertEquals(RecordedSpmLeaseState.CLEARED,
                ReconstructMasterDomainCommand.classifyLease(spmId(-1), null));
    }

    /**
     * vdsm's -5 sentinel (peer not connected to the master domain, so no real
     * owner reported) is inconclusive -> unverified, not "cleared" (which
     * would let a reconstruct falsely proceed).
     */
    @Test
    public void testUncertainSentinelUnverified() {
        assertEquals(RecordedSpmLeaseState.UNVERIFIED,
                ReconstructMasterDomainCommand.classifyLease(spmId(-5), RECORDED_SPM_ID));
    }

    /**
     * Same sentinel with a null recorded id: still inconclusive -> unverified.
     */
    @Test
    public void testUncertainSentinelNullRecordedUnverified() {
        assertEquals(RecordedSpmLeaseState.UNVERIFIED,
                ReconstructMasterDomainCommand.classifyLease(spmId(-5), null));
    }

    /**
     * No peer could read the lease at all (readRecordedSpmLease returned
     * empty): inconclusive -> unverified.
     */
    @Test
    public void testNoLeaseReadUnverified() {
        assertEquals(RecordedSpmLeaseState.UNVERIFIED,
                ReconstructMasterDomainCommand.classifyLease(null, RECORDED_SPM_ID));
    }

    // ---- stopSpm helpers ----

    private void setupStopSpm() {
        actingVds = new VDS();
        actingVds.setId(actingVdsId);
        actingVds.setVdsName("acting-host");

        spm = new VDS();
        spm.setId(spmVdsId);
        spm.setVdsName("recorded-spm");
        spm.setStatus(VDSStatus.NonResponsive);
        spm.setVdsSpmId(RECORDED_SPM_ID);

        peer = new VDS();
        peer.setId(Guid.newGuid());
        peer.setVdsName("peer");
        peer.setStatus(VDSStatus.Up);

        storagePool.setSpmVdsId(spmVdsId);
        // getVds() is protected in AuditLogableBase, so drive it via its
        // public setVdsId + the vdsDao.get(getVdsId()) lazy load it performs.
        cmd.setVdsId(actingVdsId);
        doReturn(actingVds).when(vdsDao).get(eq(actingVdsId));
        doReturn(spm).when(vdsDao).get(eq(spmVdsId));
    }

    private void resetIrsSucceeds() {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(true);
        doReturn(ret).when(vdsBrokerFrontend).runVdsCommand(eq(VDSCommandType.ResetIrs), any());
    }

    private void resetIrsFails() {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(false);
        doReturn(ret).when(vdsBrokerFrontend).runVdsCommand(eq(VDSCommandType.ResetIrs), any());
    }

    /**
     * Stub a single reachable Up peer (not the recorded SPM) returning the
     * given on-disk lease read.
     */
    private void onDiskRead(SpmStatusResult result) {
        doReturn(List.of(peer)).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(storagePoolId), any());
        doReturn(success(result)).when(resourceManager).runVdsCommand(
                eq(VDSCommandType.SpmStatus), any(SpmStatusVDSCommandParameters.class));
    }

    /**
     * No Up peer can read the lease (cluster-wide blip): the loop returns
     * empty, so the reconstruct refuses on absent evidence.
     */
    private void onDiskReadUnreachable() {
        doReturn(Collections.emptyList()).when(vdsDao)
                .getAllForStoragePoolAndStatuses(eq(storagePoolId), any());
    }

    // ---- readRecordedSpmLease / predicate helpers ----

    private static VDS peerVds() {
        VDS peer = new VDS();
        peer.setId(Guid.newGuid());
        peer.setVdsName("peer");
        peer.setStatus(VDSStatus.Up);
        return peer;
    }

    private static SpmStatusResult spmId(int hostId) {
        SpmStatusResult r = new SpmStatusResult();
        // spmStatus deliberately Free (as a peer reports) to verify the guard
        // relies on spmId, not spmStatus.
        r.setSpmStatus(SpmStatus.Free);
        r.setSpmId(hostId);
        return r;
    }

    private static VDSReturnValue success(SpmStatusResult result) {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(true);
        ret.setReturnValue(result);
        return ret;
    }

    private static VDSReturnValue failed() {
        VDSReturnValue ret = new VDSReturnValue();
        ret.setSucceeded(false);
        return ret;
    }

    private static SpmStatusVDSCommandParameters paramsFor(Guid vdsId) {
        return argThat((ArgumentMatcher<SpmStatusVDSCommandParameters>) p ->
                vdsId.equals(p.getVdsId()));
    }
}
