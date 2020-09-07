package org.ovirt.engine.core.bll.storage.disk.image;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.action.MeasureVolumeParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;

class MeasureVolumeCommandTest extends BaseCommandTest {

    private final Guid storagePoolID = Guid.newGuid();
    private final Guid storageDomainID = Guid.newGuid();
    private final Guid imageGroupID = Guid.newGuid();
    private final Guid imageID = Guid.newGuid();

    @Mock
    private VmDao vmDao;

    @Mock
    private StorageDomainDao storageDomainDao;

    @Mock
    private ImagesHandler imagesHandler;

    @Mock
    private VdsCommandsHelper vdsCommandsHelper;

    @Mock
    private DiskImageDao diskImageDao;

    @Spy
    @InjectMocks
    protected MeasureVolumeCommand<MeasureVolumeParameters> command =
            new MeasureVolumeCommand<>(createMeasureVolumeParameters(), null);

    private MeasureVolumeParameters createMeasureVolumeParameters() {
        return new MeasureVolumeParameters(storagePoolID,
                storageDomainID,
                imageGroupID,
                imageID,
                5);
    }

    @Test
    public void validateUsedDiskFailedTest() {
        Map<Boolean, List<VM>> m = new HashMap<>();
        m.put(Boolean.TRUE, List.of(createVM()));
        doReturn(m).when(vmDao).getForDisk(any(), eq(true));
        doReturn(createDisk()).when(diskImageDao).getSnapshotById(any());
        ValidateTestUtils.runAndAssertValidateFailure(command,
                EngineMessage.ACTION_TYPE_FAILED_DISK_IMAGE_CANNOT_BE_MEASURED_WHILE_USED);
    }

    @Test
    public void validateUnusedDiskSuccessTest() {
        Map<Boolean, List<VM>> m = new HashMap<>();
        doReturn(m).when(vmDao).getForDisk(any(), eq(true));
        doReturn(createDisk()).when(diskImageDao).getSnapshotById(any());
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    private VM createVM() {
        VM vm = new VM();
        vm.setStatus(VMStatus.Up);
        return vm;
    }

    private DiskImage createDisk() {
        DiskImage di = new DiskImage();
        di.setImageId(imageID);
        di.setId(imageGroupID);
        di.setStorageIds(List.of(storageDomainID));
        di.setStoragePoolId(storagePoolID);
        di.setStorageTypes(Collections.singletonList(StorageType.NFS));
        di.setActive(true);

        return di;
    }
}
