package org.ovirt.engine.core.bll.snapshots;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDAO;

@RunWith(MockitoJUnitRunner.class)
public class SnapshotVmConfigurationHelperTest {
    private SnapshotDao snapshotDaoMock;
    private DiskImageDAO diskImageDaoMock;
    private VmDAO vmDaoMock;
    private Guid existingSnapshotId = Guid.newGuid();
    private Guid existingVmId = Guid.newGuid();
    private Guid existingImageId = Guid.newGuid();
    private Guid existingImageGroupId = Guid.newGuid();
    private Snapshot existingSnapshot;
    private VM existingVm = null;
    private SnapshotsManager snapshotsManager;
    private DiskImage existingDiskImage;
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;

    private static final String EXISTING_VM_NAME = "Dummy configuration";

    @Before
    public void setUp() throws Exception {
        existingSnapshot = createSnapshot(existingSnapshotId);
        existingVm = createVm(existingVmId);
        existingSnapshot.setVmConfiguration(EXISTING_VM_NAME); // Dummy configuration
        existingDiskImage = createDiskImage(existingImageId, existingImageGroupId);
        snapshotVmConfigurationHelper = spy(new SnapshotVmConfigurationHelper());
        snapshotsManager = mock(SnapshotsManager.class);
        when(snapshotVmConfigurationHelper.getSnapshotManager()).thenReturn(snapshotsManager);
        setUpDAOMocks();
    }

    private void setUpDAOMocks() {
        vmDaoMock = mock(VmDAO.class);
        doReturn(vmDaoMock).when(snapshotVmConfigurationHelper).getVmDao();

        snapshotDaoMock = mock(SnapshotDao.class);
        doReturn(snapshotDaoMock).when(snapshotVmConfigurationHelper).getSnapshotDao();

        diskImageDaoMock = mock(DiskImageDAO.class);
        doReturn(diskImageDaoMock).when(snapshotVmConfigurationHelper).getDiskImageDao();

        when(diskImageDaoMock.get(existingImageId)).thenReturn(existingDiskImage);
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
    public void testIllegalImageReturnedByQuery() throws Exception {
        existingVm.getDiskMap().put(existingDiskImage.getId(), existingDiskImage);
        existingVm.getImages().add(existingDiskImage);
        snapshotVmConfigurationHelper.markImagesIllegalIfNotInDb(existingVm, existingSnapshotId);

        for (Disk diskImage : existingVm.getDiskMap().values()) {
            assertEquals(((DiskImage)diskImage).getImageStatus(), ImageStatus.ILLEGAL);
        }
    }
}
