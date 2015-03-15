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
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
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
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Mock
    private SnapshotDao snapshotDAO;

    @Mock
    OsRepository osRepository;

    /** The command to test */
    private RemoveImageCommand<RemoveImageParameters> cmd;

    @SuppressWarnings("serial")
    @Before
    public void setUp() {
        RemoveImageParameters params = new RemoveImageParameters(Guid.newGuid());
        cmd = spy(new RemoveImageCommand<RemoveImageParameters>(params, null) {
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

        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
    }

    @Test
    public void testRemoveImageFromSnapshotConfiguration() throws OvfReaderException {
        Guid vmId = Guid.newGuid();
        VM vm = new VM();
        vm.setId(vmId);
        vm.setStoragePoolId(Guid.newGuid());
        vm.setVmtName(RandomUtils.instance().nextString(10));
        vm.setOrigin(OriginType.OVIRT);
        vm.setDbGeneration(1L);
        Guid vmSnapshotId = Guid.newGuid();

        DiskImage disk1 = addTestDisk(vm, vmSnapshotId);
        DiskImage disk2 = addTestDisk(vm, vmSnapshotId);

        OvfManager ovfManager = new OvfManager();
        ArrayList<DiskImage> disks = new ArrayList<DiskImage>(Arrays.asList(disk1, disk2));
        String ovf = ovfManager.ExportVm(vm, disks, Version.v3_1);
        Snapshot snap = new Snapshot();
        snap.setVmConfiguration(ovf);
        snap.setId(vmSnapshotId);

        when(snapshotDAO.get(vmSnapshotId)).thenReturn(snap);
        doReturn(disk2).when(cmd).getDiskImage();
        doReturn(disk2).when(cmd).getImage();
        doReturn(disk2.getId()).when(cmd).getImageId();
        Snapshot actual = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snap, disk2.getImageId());
        String actualOvf = actual.getVmConfiguration();

        ArrayList<DiskImage> actualImages = new ArrayList<DiskImage>();
        ovfManager.ImportVm(actualOvf, new VM(), actualImages, new ArrayList<VmNetworkInterface>());
        assertEquals("Wrong number of disks", 1, actualImages.size());
        assertEquals("Wrong disk", disk1, actualImages.get(0));
    }

    @Test
    public void testRemoveImageFromSnapshotConfigurationBackwardCompatibility() throws OvfReaderException {
        Guid vmId = Guid.newGuid();
        VM vm = new VM();
        vm.setId(vmId);
        vm.setStoragePoolId(Guid.newGuid());
        vm.setVmtName(RandomUtils.instance().nextString(10));
        vm.setOrigin(OriginType.OVIRT);
        vm.setDbGeneration(1L);
        Guid vmSnapshotId = Guid.newGuid();

        DiskImage disk1 = addTestDisk(vm, vmSnapshotId);
        DiskImage disk2 = addTestDisk(vm, vmSnapshotId);

        OvfManager ovfManager = new OvfManager();
        ArrayList<DiskImage> disks = new ArrayList<DiskImage>(Arrays.asList(disk1, disk2));
        String ovf = ovfManager.ExportVm(vm, disks, Version.v3_0);
        Snapshot snap = new Snapshot();
        snap.setVmConfiguration(ovf);
        snap.setId(vmSnapshotId);

        when(snapshotDAO.get(vmSnapshotId)).thenReturn(snap);
        doReturn(disk2).when(cmd).getDiskImage();
        doReturn(disk2).when(cmd).getImage();
        doReturn(disk2.getId()).when(cmd).getImageId();
        Snapshot actual = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snap, disk2.getImageId());
        String actualOvf = actual.getVmConfiguration();

        ArrayList<DiskImage> actualImages = new ArrayList<DiskImage>();
        ovfManager.ImportVm(actualOvf, new VM(), actualImages, new ArrayList<VmNetworkInterface>());
        assertEquals("Wrong number of disks", 1, actualImages.size());
        assertEquals("Wrong disk", disk1, actualImages.get(0));
    }

    private static DiskImage addTestDisk(VM vm, Guid snapshotId) {
        Guid imageId = Guid.newGuid();
        DiskImage disk = new DiskImage();
        disk.setImageId(imageId);
        disk.setId(Guid.newGuid());
        disk.setVolumeType(VolumeType.Sparse);
        disk.setvolumeFormat(VolumeFormat.COW);
        disk.setDiskInterface(DiskInterface.VirtIO);
        disk.setStoragePoolId(vm.getStoragePoolId());
        disk.setActive(Boolean.TRUE);
        disk.setPlugged(Boolean.TRUE);
        disk.setReadOnly(Boolean.FALSE);
        disk.setVmSnapshotId(snapshotId);
        disk.setImageStatus(ImageStatus.OK);
        disk.setAppList("");
        disk.setDescription("");
        vm.getDiskList().add(disk);
        vm.getDiskMap().put(imageId, disk);
        return disk;
    }
}
