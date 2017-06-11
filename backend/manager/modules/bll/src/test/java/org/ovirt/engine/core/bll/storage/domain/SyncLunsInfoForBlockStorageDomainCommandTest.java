package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.storage.domain.SyncLunsInfoForBlockStorageDomainCommand.LunHandler;
import org.ovirt.engine.core.common.action.SyncLunsInfoForBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class SyncLunsInfoForBlockStorageDomainCommandTest extends BaseCommandTest {

    private SyncLunsInfoForBlockStorageDomainParameters parameters = createParameters();

    @Spy
    @InjectMocks
    private SyncLunsInfoForBlockStorageDomainCommand<SyncLunsInfoForBlockStorageDomainParameters> command =
            new SyncLunsInfoForBlockStorageDomainCommand<>(parameters, null);
    private LUNs lunFromVg;
    private LUNs lunFromDb;

    @Before
    public void setUp() {
        lunFromVg = new LUNs();
        lunFromDb = new LUNs();
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
    public void testGetLunsToUpdateInDbDiffDiscardZeroesData() {
        lunFromVg.setDiscardZeroesData(true);
        lunFromDb.setDiscardZeroesData(false);
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
    public void testGetLunsToRemoveFromDb() {
        lunFromVg.setLUNId(Guid.newGuid().toString());
        lunFromDb.setLUNId(Guid.newGuid().toString());
        LUNs dummyLun = new LUNs();
        dummyLun.setId(BusinessEntitiesDefinitions.DUMMY_LUN_ID_PREFIX + Guid.newGuid().toString());
        List<LUNs> lunsFromVgInfo = Collections.singletonList(lunFromVg);
        List<LUNs> lunsFromDb = Arrays.asList(lunFromDb, dummyLun);
        assertEquals(command.getLunsToRemoveFromDb(lunsFromVgInfo, lunsFromDb),
                Collections.singletonList(lunFromDb));
    }

    private SyncLunsInfoForBlockStorageDomainParameters createParameters() {
        SyncLunsInfoForBlockStorageDomainParameters params =
                new SyncLunsInfoForBlockStorageDomainParameters(Guid.newGuid());
        params.setVdsId(Guid.newGuid());
        return params;
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
