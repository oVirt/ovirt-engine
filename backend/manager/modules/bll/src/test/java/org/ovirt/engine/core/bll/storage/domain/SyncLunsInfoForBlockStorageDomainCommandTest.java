package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;

public class SyncLunsInfoForBlockStorageDomainCommandTest extends BaseCommandTest {

    private SyncLunsInfoForBlockStorageDomainCommand<StorageDomainParametersBase> command;
    private StorageDomainParametersBase parameters;

    @Before
    public void setUp() {
        parameters = new StorageDomainParametersBase(Guid.newGuid());
        parameters.setVdsId(Guid.newGuid());

        command = spy(new SyncLunsInfoForBlockStorageDomainCommand<>(parameters, null));
    }

    @Test
    public void testGetLunsToUpdateInDbDiffLunIdDiffPvId() {
        Guid lunFromVgLunId = Guid.newGuid();
        List<LUNs> newLunsToSaveInDb =
                getLunsToUpdateInDb(lunFromVgLunId, Guid.newGuid(), Guid.newGuid(), Guid.newGuid()).
                        get(command.saveNewLuns);

        assertLunIdInList(newLunsToSaveInDb, lunFromVgLunId);
    }

    @Test
    public void testGetLunsToUpdateInDbSameLunIdDiffPvId() {
        Guid lunId = Guid.newGuid();
        List<LUNs> existingLunsToUpdateInDb = getLunsToUpdateInDb(lunId, lunId, Guid.newGuid(), Guid.newGuid()).
                get(command.updateExistingLuns);

        assertLunIdInList(existingLunsToUpdateInDb, lunId);
    }

    @Test
    public void testGetLunsToUpdateInDbDiffLunIdSamePvId() {
        Guid pvID = Guid.newGuid();
        Guid lunFromVgLunId = Guid.newGuid();
        List<LUNs> newLunsToSaveInDb = getLunsToUpdateInDb(lunFromVgLunId, Guid.newGuid(), pvID, pvID).
                get(command.saveNewLuns);

        assertLunIdInList(newLunsToSaveInDb, lunFromVgLunId);
    }

    private Map<Consumer<List<LUNs>>, List<LUNs>> getLunsToUpdateInDb(Guid lunFromVgLunId, Guid lunFromDbLunId,
            Guid lunFromVgPvId, Guid lunFromDbPvId) {
        LUNs lunFromVG = new LUNs();
        lunFromVG.setLUNId(lunFromVgLunId.toString());
        lunFromVG.setPhysicalVolumeId(lunFromVgPvId.toString());

        LUNs lunFromDB = new LUNs();
        lunFromDB.setLUNId(lunFromDbLunId.toString());
        lunFromDB.setPhysicalVolumeId(lunFromDbPvId.toString());

        return getLunsToUpdateInDb(lunFromVG, lunFromDB);
    }

    @Test
    public void testGetLunsToUpdateInDbForSameLun() {
        LUNs lun = new LUNs();
        Guid lunId = Guid.newGuid();
        lun.setLUNId(lunId.toString());
        lun.setPhysicalVolumeId(Guid.newGuid().toString());
        List<LUNs> upToDateLuns = getLunsToUpdateInDb(lun, lun).get(command.noOp);

        assertLunIdInList(upToDateLuns, lunId);
    }

    @Test
    public void testGetLunsToUpdateInDbDiffDeviceSize() {
        Guid pvID = Guid.newGuid();
        Guid lunID = Guid.newGuid();

        LUNs lunFromVG = new LUNs();
        lunFromVG.setLUNId(lunID.toString());
        lunFromVG.setPhysicalVolumeId(pvID.toString());
        lunFromVG.setDeviceSize(20);

        LUNs lunFromDB = new LUNs();
        lunFromDB.setLUNId(lunID.toString());
        lunFromDB.setPhysicalVolumeId(pvID.toString());
        lunFromDB.setDeviceSize(10);

        List<LUNs> existingLunsToUpdateInDb =
                getLunsToUpdateInDb(lunFromVG, lunFromDB).get(command.updateExistingLuns);

        assertLunIdInList(existingLunsToUpdateInDb, lunID);
    }

    private void assertLunIdInList(List<LUNs> luns, Guid requestedLunId) {
        assertEquals(luns.stream().map(LUNs::getLUNId).findAny().orElse(null), requestedLunId.toString());
    }

    private Map<Consumer<List<LUNs>>, List<LUNs>> getLunsToUpdateInDb(LUNs lunFromVG, LUNs lunFromDB) {
        List<LUNs> lunsFromVgInfo = Collections.singletonList(lunFromVG);
        List<LUNs> lunsFromDb = Collections.singletonList(lunFromDB);
        return command.getLunsToUpdateInDb(lunsFromVgInfo, lunsFromDb);
    }
}
