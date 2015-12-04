package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public void lunsMismatchListSize() {
        List<LUNs> lunsFromVgInfo = Collections.singletonList(new LUNs());
        List<LUNs> lunsFromDb = Collections.emptyList();

        boolean isMismatch = command.isLunsInfoMismatch(lunsFromVgInfo, lunsFromDb);
        assertTrue(isMismatch);
    }

    @Test
    public void lunsMismatchWrongId() {
        Guid pvID = Guid.newGuid();

        LUNs lunFromVG = new LUNs();
        lunFromVG.setLUNId(Guid.newGuid().toString());
        lunFromVG.setPhysicalVolumeId(pvID.toString());

        LUNs lunFromDB = new LUNs();
        lunFromDB.setLUNId(Guid.newGuid().toString());
        lunFromDB.setPhysicalVolumeId(pvID.toString());

        List<LUNs> lunsFromVgInfo = Collections.singletonList(lunFromVG);
        List<LUNs> lunsFromDb = Collections.singletonList(lunFromDB);

        boolean isMismatch = command.isLunsInfoMismatch(lunsFromVgInfo, lunsFromDb);
        assertTrue(isMismatch);
    }

    @Test
    public void lunsMatch() {
        LUNs lun = new LUNs();
        lun.setLUNId(Guid.newGuid().toString());
        lun.setPhysicalVolumeId(Guid.newGuid().toString());

        List<LUNs> lunsFromVgInfo = Arrays.asList(lun, lun);
        List<LUNs> lunsFromDb = Arrays.asList(lun, lun);

        boolean isMismatch = command.isLunsInfoMismatch(lunsFromVgInfo, lunsFromDb);
        assertFalse(isMismatch);
    }

    @Test
    public void lunsMismatchDeviceSize() {
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

        List<LUNs> lunsFromVgInfo = Collections.singletonList(lunFromVG);
        List<LUNs> lunsFromDb = Collections.singletonList(lunFromDB);

        assertTrue(command.isLunsInfoMismatch(lunsFromVgInfo, lunsFromDb));
    }
}
