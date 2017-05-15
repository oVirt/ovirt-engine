package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.SyncAttachedDirectLunsParameters;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class SyncAttachedDirectLunsCommandTest {

    private Guid storagePoolId = Guid.newGuid();
    private SyncAttachedDirectLunsParameters parameters =
            new SyncAttachedDirectLunsParameters(storagePoolId);
    private LUNs lun1;
    private LUNs lun2;
    private LUNs lun3;

    @Spy
    @InjectMocks
    private SyncAttachedDirectLunsCommand<SyncAttachedDirectLunsParameters> command =
            new SyncAttachedDirectLunsCommand<>(parameters, null);

    @Before
    public void setUp() {
        lun1 = new LUNs();
        lun1.setLUNId("lun1");
        lun1.setDiskId(Guid.newGuid());

        lun2 = new LUNs();
        lun2.setLUNId("lun2");
        lun2.setDiskId(Guid.newGuid());

        lun3 = new LUNs();
        lun3.setLUNId("lun3");
        lun3.setDiskId(Guid.newGuid());
    }

    @Test
    public void testGetLunsToUpdateInDb() {
        command.getParameters().setDeviceList(Arrays.asList(lun1, lun2, lun3));
        mockDiskToLunIdsOfDirectLunsAttachedToVmsInPool(lun1, lun2);
        assertEquals(Arrays.asList(lun1, lun2), command.getLunsToUpdateInDb());
    }

    @Test
    public void validateAttachedDirectLunsNoLuns() {
        assertTrue(command.validateAttachedDirectLuns());
    }

    @Test
    public void validateAttachedDirectLunsAllLunsAttachedToVmsInPool() {
        command.getParameters().setAttachedDirectLunDisksIds(
                new HashSet<>(Arrays.asList(lun1.getDiskId(), lun3.getDiskId())));
        mockDiskToLunIdsOfDirectLunsAttachedToVmsInPool(lun1, lun2, lun3);
        assertTrue(command.validateAttachedDirectLuns());
    }

    @Test
    public void validateAttachedDirectLunsWithLunsThatAreNotAttachedToVmInPool() {
        command.getParameters().setAttachedDirectLunDisksIds(
                new HashSet<>(Arrays.asList(lun1.getDiskId(), lun3.getDiskId())));
        mockDiskToLunIdsOfDirectLunsAttachedToVmsInPool(lun2, lun3);

        assertFalse(command.validateAttachedDirectLuns());
        ValidateTestUtils.assertValidationMessages("lun2 is not attached to a vm in the datacenter.", command,
                EngineMessage.ACTION_TYPE_FAILED_CANNOT_SYNC_DIRECT_LUN_DISKS_NOT_ATTACHED_TO_VM_IN_POOL);
    }

    private void mockDiskToLunIdsOfDirectLunsAttachedToVmsInPool(LUNs... luns) {
        doReturn(Arrays.asList(luns).stream().collect(Collectors.toMap(LUNs::getDiskId, LUNs::getLUNId)))
                .when(command).getDiskToLunIdsOfDirectLunsAttachedToVmsInPool();
    }
}
