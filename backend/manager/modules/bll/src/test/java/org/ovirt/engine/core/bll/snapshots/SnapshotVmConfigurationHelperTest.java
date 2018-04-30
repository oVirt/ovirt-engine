package org.ovirt.engine.core.bll.snapshots;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;

@ExtendWith(MockitoExtension.class)
public class SnapshotVmConfigurationHelperTest {

    @Mock
    private DiskImageDao diskImageDaoMock;

    private Guid existingSnapshotId = Guid.newGuid();
    private Guid existingVmId = Guid.newGuid();
    private Guid existingImageId = Guid.newGuid();
    private Guid existingImageGroupId = Guid.newGuid();
    private Snapshot existingSnapshot;
    private VM existingVm = null;
    private DiskImage existingDiskImage;

    @InjectMocks
    @Spy
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    private static final String EXISTING_VM_NAME = "Dummy configuration";

    @BeforeEach
    public void setUp() {
        existingSnapshot = createSnapshot(existingSnapshotId);
        existingVm = createVm(existingVmId);
        existingSnapshot.setVmConfiguration(EXISTING_VM_NAME); // Dummy configuration
        existingDiskImage = createDiskImage(existingImageId, existingImageGroupId);
    }

    private VM createVm(Guid existingVmId) {
        VM vm = new VM();
        vm.setId(existingVmId);
        return vm;
    }

    private Snapshot createSnapshot(Guid existingSnapshotId) {
        Snapshot snapshot = new Snapshot();
        snapshot.setId(existingSnapshotId);
        snapshot.setVmId(existingVmId);
        snapshot.setVmConfiguration(EXISTING_VM_NAME);
        return snapshot;
    }

    private DiskImage createDiskImage(Guid diskImageId, Guid imageGroupId) {
        DiskImage diskImage = new DiskImage();
        diskImage.setImageId(diskImageId);
        diskImage.setId(imageGroupId);
        return diskImage;
    }

    @Test
    public void testIllegalImageReturnedByQuery() {
        existingVm.getDiskMap().put(existingDiskImage.getId(), existingDiskImage);
        existingVm.getImages().add(existingDiskImage);
        snapshotVmConfigurationHelper.markImagesIllegalIfNotInDb(existingVm, existingSnapshotId);

        for (Disk diskImage : existingVm.getDiskMap().values()) {
            assertEquals(ImageStatus.ILLEGAL, ((DiskImage)diskImage).getImageStatus());
        }
    }
}
