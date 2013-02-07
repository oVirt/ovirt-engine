package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

@RunWith(MockitoJUnitRunner.class)
public class RemoveImageCommandTest {
    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.VdcVersion, "3.1"));

    @Rule
    public static RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Mock
    private SnapshotDao snapshotDAO;

    /** The command to test */
    private RemoveImageCommand<RemoveImageParameters> cmd;

    @SuppressWarnings("serial")
    @Before
    public void setUp() {
        RemoveImageParameters params = new RemoveImageParameters(Guid.NewGuid());
        cmd = spy(new RemoveImageCommand<RemoveImageParameters>(params) {
            @Override
            protected void initImage() {
                // Stub implementation for testing
            }

            @Override
            protected void initStoragePoolId() {
                // Stub implementation for testing
            }

            @Override
            protected void initStorageDomainId() {
                // Stub implementation for testing
            }

            @Override
            protected void initContainerDetails(ImagesContainterParametersBase parameters) {
                // Stub implementation for testing
            }

        });
        doReturn(snapshotDAO).when(cmd).getSnapshotDao();
    }

    @Test
    public void testRemoveImageFromSnapshotConfiguration() throws OvfReaderException {
        Guid vmId = Guid.NewGuid();
        VM vm = new VM();
        vm.setId(vmId);
        vm.setStoragePoolId(Guid.NewGuid());
        vm.setVmtName(RandomUtils.instance().nextString(10));
        vm.setOrigin(OriginType.OVIRT);
        vm.setDbGeneration(1L);
        Guid vmSnapshotId = Guid.NewGuid();

        DiskImage disk1 = addTestDisk(vm, vmSnapshotId);
        DiskImage disk2 = addTestDisk(vm, vmSnapshotId);

        OvfManager ovfManager = new OvfManager();
        ArrayList<DiskImage> disks = new ArrayList<DiskImage>(Arrays.asList(disk1, disk2));
        String ovf = ovfManager.ExportVm(vm, disks);
        Snapshot snap = new Snapshot();
        snap.setVmConfiguration(ovf);
        snap.setId(vmSnapshotId);

        when(snapshotDAO.get(vmSnapshotId)).thenReturn(snap);
        doReturn(disk2).when(cmd).getDiskImage();
        doReturn(disk2).when(cmd).getImage();
        doReturn(disk2.getId()).when(cmd).getImageId();
        Snapshot actual = cmd.prepareSnapshotConfigWithoutImageSingleImage(vmSnapshotId, disk2.getImageId());
        String actualOvf = actual.getVmConfiguration();

        ArrayList<DiskImage> actualImages = new ArrayList<DiskImage>();
        ovfManager.ImportVm(actualOvf, new VM(), actualImages, new ArrayList<VmNetworkInterface>());
        assertEquals("Wrong number of disks", 1, actualImages.size());
        assertEquals("Wrong disk", disk1, actualImages.get(0));
    }

    private static DiskImage addTestDisk(VM vm, Guid snapshotId) {
        Guid imageId = Guid.NewGuid();
        DiskImage disk = new DiskImage();
        disk.setImageId(imageId);
        disk.setId(Guid.NewGuid());
        disk.setvolume_type(VolumeType.Sparse);
        disk.setvolume_format(VolumeFormat.COW);
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setstorage_pool_id(vm.getStoragePoolId());
        disk.setactive(Boolean.TRUE);
        disk.setvm_snapshot_id(snapshotId);
        disk.setImageStatus(ImageStatus.OK);
        disk.setappList("");
        disk.setdescription("");
        vm.getDiskList().add(disk);
        vm.getDiskMap().put(imageId, disk);
        return disk;
    }
}
