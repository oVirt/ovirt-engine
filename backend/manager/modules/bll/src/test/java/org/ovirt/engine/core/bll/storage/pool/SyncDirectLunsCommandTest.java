package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.SyncDirectLunsParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SyncDirectLunsCommandTest {

    @Mock
    private DiskLunMapDao diskLunMapDao;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Mock
    private VdsDao vdsDao;

    @Mock
    private LunDao lunDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private StorageServerConnectionDao serverConnectionDao;

    private LUNs lun1;
    private LUNs lun2;
    private LUNs lun3;
    private DiskLunMap disk1Lun1Map;
    private Map<Boolean, List<VM>> lunsPerHost;

    @Spy
    @InjectMocks
    private SyncDirectLunsCommand<SyncDirectLunsParameters> command =
            new SyncDirectLunsCommand<>(new SyncDirectLunsParameters(), null);

    @BeforeEach
    public void setUp() {
        lun1 = new LUNs();
        lun1.setLUNId("lun1");
        lun1.setDiskId(Guid.newGuid());
        lun1.setVolumeGroupId("");
        disk1Lun1Map = new DiskLunMap(lun1.getDiskId(), lun1.getLUNId());

        lun2 = new LUNs();
        lun2.setLUNId("lun2");
        lun2.setDiskId(Guid.newGuid());
        lun2.setVolumeGroupId("");

        lun3 = new LUNs();
        lun3.setLUNId("lun3");
        lun3.setDiskId(Guid.newGuid());
        lun3.setVolumeGroupId("");

        mockLunDao();
        mockVmDao();
    }

    @Test
    public void testGetLunsToUpdateInDbWithoutDirectLunId() {
        command.getParameters().setDeviceList(Arrays.asList(lun1, lun2, lun3));
        mockLunToDiskIdsOfDirectLunsAttachedToVmsInPool(lun1, lun2);
        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(lun1, lun2), command.getLunsToUpdateInDb()));
    }

    @Test
    public void testGetLunsToUpdateInDbWithDirectLunId() {
        command.getParameters().setDeviceList(Arrays.asList(lun1, lun2, lun3));
        mockLunToDiskIdsOfDirectLunsAttachedToVmsInPool(lun1, lun2);
        command.getParameters().setDirectLunIds(Collections.singleton(lun1.getDiskId()));
        when(diskLunMapDao.getDiskLunMapByDiskId(lun1.getDiskId())).thenReturn(disk1Lun1Map);
        assertEquals(Collections.singleton(lun1), command.getLunsToUpdateInDb());
    }

    @Test
    public void testGetLunsToUpdateInDbLunIsPartOfStorageDomain() {
        lun1.setVolumeGroupId(Guid.newGuid().toString());
        command.getParameters().setDeviceList(Arrays.asList(lun1, lun2, lun3));
        mockLunToDiskIdsOfDirectLunsAttachedToVmsInPool(lun1);
        assertEquals(Collections.singleton(lun1), command.getLunsToUpdateInDb());
    }

    @Test
    public void validateWithDirectLunIdAndInvalidVds() {
        command.getParameters().setDirectLunIds(Collections.singleton(lun1.getDiskId()));
        when(vmDao.getForDisk(lun1.getDiskId(), false)).thenReturn(lunsPerHost);
        doReturn(false).when(command).validateVds();
        assertFalse(command.validate());
    }

    @Test
    public void validateWithRandomDirectLunId() {
        command.getParameters().setDirectLunIds(Collections.singleton(Guid.newGuid()));
        doReturn(true).when(command).validateVds();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
    }

    @Test
    public void validateWithValidDirectLunId() {
        command.getParameters().setDirectLunIds(Collections.singleton(lun1.getDiskId()));
        when(diskLunMapDao.getDiskLunMapByDiskId(any())).thenReturn(disk1Lun1Map);
        command.getParameters().setDirectLunIds(Collections.singleton(Guid.newGuid()));
        command.getParameters().setVdsId(Guid.newGuid());
        doReturn(true).when(command).validateVds();
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private void mockLunToDiskIdsOfDirectLunsAttachedToVmsInPool(LUNs... luns) {
        doReturn(Arrays.stream(luns).collect(Collectors.toMap(LUNs::getLUNId, LUNs::getDiskId)))
                .when(command).getLunToDiskIdsOfDirectLunsAttachedToVmsInPool();
    }

    private void mockLunDao() {
        Stream.of(lun1, lun2, lun3).forEach(lun -> when(lunDao.get(lun.getId())).thenReturn(lun));
    }

    private void mockVmDao() {
        lunsPerHost = new HashMap<>();
        List<VM> vms = new ArrayList<>();
        VM vm = new VM();
        vm.setRunOnVds(Guid.newGuid());
        vms.add(vm);
        lunsPerHost.put(Boolean.TRUE, vms);
    }
}
