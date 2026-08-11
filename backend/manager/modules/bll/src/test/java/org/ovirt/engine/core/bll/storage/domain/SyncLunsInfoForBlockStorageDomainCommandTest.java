package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.ValidateTestUtils.runAndAssertValidateFailure;
import static org.ovirt.engine.core.bll.ValidateTestUtils.runAndAssertValidateSuccess;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.connection.ISCSIStorageHelper;
import org.ovirt.engine.core.bll.storage.domain.SyncLunsInfoForBlockStorageDomainCommand.LunHandler;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class SyncLunsInfoForBlockStorageDomainCommandTest extends BaseCommandTest {

    private SyncLunsInfoForBlockStorageDomainParameters parameters = createParameters();

    @Spy
    @InjectMocks
    private SyncLunsInfoForBlockStorageDomainCommand<SyncLunsInfoForBlockStorageDomainParameters> command =
            new SyncLunsInfoForBlockStorageDomainCommand<>(parameters, null);

    @Mock
    private HostValidator hostValidator;

    @Mock
    private LunDao lunDao;

    @Mock
    private ISCSIStorageHelper iscsiStorageHelper;

    @Mock
    private StorageServerConnectionLunMapDao storageServerConnectionLunMapDao;

    @Mock
    private StorageServerConnectionDao storageServerConnectionDao;

    @Mock
    private BlockStorageDomainHelper blockStorageDomainHelper;

    private LUNs lunFromVg;
    private LUNs lunFromDb;

    @BeforeEach
    public void setUp() {
        doReturn(hostValidator).when(command).getHostValidator();
        StorageDomain domain = new StorageDomain();
        domain.setStorage("vgId");
        doReturn(domain).when(command).getStorageDomain();
        lunFromVg = new LUNs();
        lunFromDb = new LUNs();
    }

    @Test
    public void validationSucceeds() {
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp()).thenReturn(ValidationResult.VALID);
        doReturn(true).when(command).checkStorageDomain();
        runAndAssertValidateSuccess(command);
    }

    @Test
    public void validateInvalidHost() {
        when(hostValidator.hostExists()).thenReturn(new ValidationResult(EngineMessage.VDS_INVALID_SERVER_ID));
        runAndAssertValidateFailure(command, EngineMessage.VDS_INVALID_SERVER_ID);
    }

    @Test
    public void validateHostIsNotUp() {
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL));
        runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VDS_STATUS_ILLEGAL);
    }

    @Test
    public void validateInvalidStorageDomain() {
        when(hostValidator.hostExists()).thenReturn(ValidationResult.VALID);
        when(hostValidator.isUp()).thenReturn(ValidationResult.VALID);
        doReturn(null).when(command).getStorageDomain();
        runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
    }

    @Test
    public void testGetLunsToUpdateInDbDiffLunIdDiffPvId() {
        Guid lunFromVgLunId = Guid.newGuid();
        Map<LunHandler, List<LUNs>> lunsToUpdateInDb =
                getLunsToUpdateInDb(lunFromVgLunId, Guid.newGuid(), Guid.newGuid(), Guid.newGuid());
        List<LUNs> newLunsToSaveInDb = lunsToUpdateInDb.get(command.saveLunsHandler);

        assertEquals(Stream.of(command.saveLunsHandler, command.removeLunsHandler).collect(Collectors.toSet()),
                lunsToUpdateInDb.keySet());
        assertLunIdInList(newLunsToSaveInDb, lunFromVgLunId);
    }

    @Test
    public void testGetLunsToUpdateInDbSameLunIdDiffPvId() {
        Guid lunId = Guid.newGuid();
        Map<LunHandler, List<LUNs>> lunsToUpdateInDb =
                getLunsToUpdateInDb(lunId, lunId, Guid.newGuid(), Guid.newGuid());
        List<LUNs> existingLunsToUpdateInDb = lunsToUpdateInDb.get(command.updateLunsHandler);

        assertEquals(Collections.singleton(command.updateLunsHandler), lunsToUpdateInDb.keySet());
        assertLunIdInList(existingLunsToUpdateInDb, lunId);
    }

    @Test
    public void testGetLunsToUpdateInDbDiffLunIdSamePvId() {
        Guid pvID = Guid.newGuid();
        Guid lunFromVgLunId = Guid.newGuid();
        Map<LunHandler, List<LUNs>> lunsToUpdateInDb = getLunsToUpdateInDb(lunFromVgLunId, Guid.newGuid(), pvID, pvID);
        List<LUNs> newLunsToSaveInDb = lunsToUpdateInDb.get(command.saveLunsHandler);

        assertEquals(Stream.of(command.saveLunsHandler, command.removeLunsHandler).collect(Collectors.toSet()),
                lunsToUpdateInDb.keySet());
        assertLunIdInList(newLunsToSaveInDb, lunFromVgLunId);
    }

    private Map<LunHandler, List<LUNs>> getLunsToUpdateInDb(Guid lunFromVgLunId, Guid lunFromDbLunId,
            Guid lunFromVgPvId, Guid lunFromDbPvId) {
        setLunsIds(lunFromVgLunId, lunFromDbLunId, lunFromVgPvId, lunFromDbPvId);
        return getLunsToUpdateInDb();
    }

    private void setLunsIds(Guid lunFromVgLunId, Guid lunFromDbLunId, Guid lunFromVgPvId, Guid lunFromDbPvId) {
        lunFromVg.setLUNId(lunFromVgLunId.toString());
        lunFromVg.setPhysicalVolumeId(lunFromVgPvId.toString());
        lunFromDb.setLUNId(lunFromDbLunId.toString());
        lunFromDb.setPhysicalVolumeId(lunFromDbPvId.toString());
    }

    @Test
    public void testGetLunsToUpdateInDbForSameLun() {
        setLunsSameLunAndPvIds();
        List<LUNs> upToDateLuns = getLunsToUpdateInDb().get(command.noOp);

        assertLunIdInList(upToDateLuns, lunFromVg.getLUNId());
    }

    @Test
    public void testGetLunsToUpdateInDbDiffDeviceSize() {
        lunFromVg.setDeviceSize(20);
        lunFromDb.setDeviceSize(10);
        assertLunShouldBeUpdatedDueToFieldChange();
    }

    @Test
    public void testGetLunsToUpdateInDbDiffDiscardMaxSize() {
        lunFromVg.setDiscardMaxSize(2048L);
        lunFromDb.setDiscardMaxSize(1024L);
        assertLunShouldBeUpdatedDueToFieldChange();
    }

    @Test
    public void testGetLunsToUpdateInDbLunExistsInDbButNotInVgInfo() {
        Guid lunFromDbId = Guid.newGuid();
        Map<LunHandler, List<LUNs>> lunsToUpdateInDb =
                getLunsToUpdateInDb(Guid.newGuid(), lunFromDbId, Guid.newGuid(), Guid.newGuid());
        List<LUNs> lunsToRemoveFromDb = lunsToUpdateInDb.get(command.removeLunsHandler);

        assertEquals(Stream.of(command.saveLunsHandler, command.removeLunsHandler).collect(Collectors.toSet()),
                lunsToUpdateInDb.keySet());
        assertLunIdInList(lunsToRemoveFromDb, lunFromDbId);
    }

    @Test
    public void refreshLunsConnectionsMapsAllPathsSharingTheSameIqn() {
        LUNs lun = new LUNs();
        lun.setLUNId("lun1");

        StorageServerConnections c1 = createConnection("conn1", "1.1.1.1", "iqn.shared");
        StorageServerConnections c2 = createConnection("conn2", "1.1.1.2", "iqn.shared");
        StorageServerConnections c3 = createConnection("conn3", "1.1.1.3", "iqn.shared");
        StorageServerConnections c4 = createConnection("conn4", "1.1.1.4", "iqn.shared");
        lun.setLunConnections(Arrays.asList(c1, c2, c3, c4));

        when(iscsiStorageHelper.findConnectionWithSameDetails(c1)).thenReturn(c1);
        when(iscsiStorageHelper.findConnectionWithSameDetails(c2)).thenReturn(c2);
        when(iscsiStorageHelper.findConnectionWithSameDetails(c3)).thenReturn(c3);
        when(iscsiStorageHelper.findConnectionWithSameDetails(c4)).thenReturn(c4);
        when(storageServerConnectionLunMapDao.get(any())).thenReturn(null);

        command.refreshLunsConnections(Collections.singletonList(lun));

        verify(storageServerConnectionLunMapDao, times(4)).save(any(LUNStorageServerConnectionMap.class));
    }

    @Test
    public void refreshLunsConnectionsSkipsAndLogsUnmatchedPath() {
        LUNs lun = new LUNs();
        lun.setLUNId("lun1");
        StorageServerConnections conn = createConnection("conn1", "1.1.1.1", "iqn.shared");
        lun.setLunConnections(Collections.singletonList(conn));

        when(iscsiStorageHelper.findConnectionWithSameDetails(conn)).thenReturn(null);

        command.refreshLunsConnections(Collections.singletonList(lun));

        // Flag defaults to false: the unmatched path is skipped, no row created and no map saved.
        verify(storageServerConnectionDao, never()).save(any(StorageServerConnections.class));
        verify(storageServerConnectionLunMapDao, never()).save(any(LUNStorageServerConnectionMap.class));
    }

    @Test
    public void refreshLunsConnectionsPersistsNewConnectionsWhenFlagSet() {
        parameters.setPersistNewConnections(true);
        LUNs lun = new LUNs();
        lun.setLUNId("lun1");
        StorageServerConnections conn = createConnection(null, "1.1.1.1", "iqn.shared");
        lun.setLunConnections(Collections.singletonList(conn));

        when(iscsiStorageHelper.findConnectionWithSameDetails(conn)).thenReturn(null);
        when(storageServerConnectionLunMapDao.get(any())).thenReturn(null);

        command.refreshLunsConnections(Collections.singletonList(lun));

        // The missing connection row is created (mirroring LunHelper#proceedLUNInDb) ...
        verify(storageServerConnectionDao, times(1)).save(any(StorageServerConnections.class));
        // ... assigned a fresh id ...
        assertNotNull(conn.getId());
        // ... and mapped to the LUN.
        verify(storageServerConnectionLunMapDao, times(1)).save(any(LUNStorageServerConnectionMap.class));
    }

    @Test
    public void refreshLunsConnectionsSkipsFcpDomains() {
        command.getStorageDomain().setStorageType(StorageType.FCP);
        LUNs lun = new LUNs();
        lun.setLUNId("lun1");
        StorageServerConnections conn = createConnection("conn1", "1.1.1.1", "iqn.shared");
        lun.setLunConnections(Collections.singletonList(conn));

        command.refreshLunsConnections(Collections.singletonList(lun));

        verify(iscsiStorageHelper, never()).findConnectionWithSameDetails(any());
        verify(storageServerConnectionDao, never()).save(any(StorageServerConnections.class));
        verify(storageServerConnectionLunMapDao, never()).save(any(LUNStorageServerConnectionMap.class));
    }

    @Test
    public void executeCommandRefreshesConnectionsEvenWhenLunsAreUpToDate() {
        LUNs lun = new LUNs();
        lun.setLUNId("lun1");
        lun.setPhysicalVolumeId("pv1");
        StorageServerConnections conn = createConnection("conn1", "1.1.1.1", "iqn.shared");
        lun.setLunConnections(Collections.singletonList(conn));

        doReturn(Collections.singletonList(lun)).when(command).getVgInfo();
        when(lunDao.getAllForVolumeGroup(any())).thenReturn(Collections.singletonList(lun));
        // The LUN reported by vdsm is identical to the one in the DB -> every LUN classifies as noOp,
        // so dbShouldBeUpdated is false. The connection refresh must still run regardless.
        when(iscsiStorageHelper.findConnectionWithSameDetails(conn)).thenReturn(conn);
        when(storageServerConnectionLunMapDao.get(any())).thenReturn(null);

        command.executeCommand();

        verify(storageServerConnectionLunMapDao).save(any(LUNStorageServerConnectionMap.class));
    }

    private StorageServerConnections createConnection(String id, String address, String iqn) {
        StorageServerConnections connection = new StorageServerConnections();
        connection.setId(id);
        connection.setConnection(address);
        connection.setIqn(iqn);
        return connection;
    }

    private SyncLunsInfoForBlockStorageDomainParameters createParameters() {
        return new SyncLunsInfoForBlockStorageDomainParameters(Guid.newGuid(), Guid.newGuid());
    }

    /**
     * Sets lunFromVg and lunFromDb the same Lun Ids and the same PV Ids and asserts the lun will
     * be updated assuming that another field (which is not its Lun Id or PV Id) has changed.
     */
    private void assertLunShouldBeUpdatedDueToFieldChange() {
        setLunsSameLunAndPvIds();
        List<LUNs> existingLunsToUpdateInDb = getLunsToUpdateInDb().get(command.updateLunsHandler);
        assertLunIdInList(existingLunsToUpdateInDb, lunFromVg.getLUNId());
    }

    private void setLunsSameLunAndPvIds() {
        Guid lunId = Guid.newGuid();
        Guid pvId = Guid.newGuid();
        setLunsIds(lunId, lunId, pvId, pvId);
    }

    private void assertLunIdInList(List<LUNs> luns, Guid requestedLunId) {
        assertLunIdInList(luns, requestedLunId.toString());
    }

    private void assertLunIdInList(List<LUNs> luns, String requestedLunId) {
        assertEquals(luns.stream().map(LUNs::getLUNId).findAny().orElse(null), requestedLunId);
    }

    private Map<LunHandler, List<LUNs>> getLunsToUpdateInDb() {
        List<LUNs> lunsFromVgInfo = Collections.singletonList(lunFromVg);
        List<LUNs> lunsFromDb = Collections.singletonList(lunFromDb);
        return command.getLunsToUpdateInDb(lunsFromVgInfo, lunsFromDb);
    }
}
