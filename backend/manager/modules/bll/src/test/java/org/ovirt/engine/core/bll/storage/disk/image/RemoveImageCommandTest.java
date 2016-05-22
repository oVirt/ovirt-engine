package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.ovf.OvfVmIconDefaultsProvider;

public class RemoveImageCommandTest extends BaseCommandTest {
    @ClassRule
    public static MockConfigRule mcr =
        new MockConfigRule(mockConfig(ConfigValues.VdcVersion, Version.getLast().toString()));

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    OsRepository osRepository;

    @Mock
    private OvfVmIconDefaultsProvider iconDefaultsProvider;

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
        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        when(iconDefaultsProvider.getVmIconDefaults()).thenReturn(new HashMap<Integer, VmIconIdSizePair>(){{
            put(0, new VmIconIdSizePair(
                    Guid.createGuidFromString("00000000-0000-0000-0000-00000000000a"),
                    Guid.createGuidFromString("00000000-0000-0000-0000-00000000000b")));
        }});

        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        SimpleDependencyInjector.getInstance().bind(OvfVmIconDefaultsProvider.class, iconDefaultsProvider);
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
        DiskVmElement dve1 = new DiskVmElement(disk1.getId(), vm.getId());
        dve1.setDiskInterface(DiskInterface.VirtIO);
        disk1.setDiskVmElements(Collections.singletonList(dve1));

        DiskImage disk2 = addTestDisk(vm, vmSnapshotId);
        DiskVmElement dve2 = new DiskVmElement(disk2.getId(), vm.getId());
        dve2.setDiskInterface(DiskInterface.IDE);
        disk2.setDiskVmElements(Collections.singletonList(dve2));

        OvfManager ovfManager = new OvfManager();
        ArrayList<DiskImage> disks = new ArrayList<>(Arrays.asList(disk1, disk2));
        String ovf = ovfManager.exportVm(vm, disks, Version.getLast());
        Snapshot snap = new Snapshot();
        snap.setVmConfiguration(ovf);
        snap.setId(vmSnapshotId);

        when(snapshotDao.get(vmSnapshotId)).thenReturn(snap);
        doReturn(disk2).when(cmd).getDiskImage();
        doReturn(disk2).when(cmd).getImage();
        doReturn(disk2.getId()).when(cmd).getImageId();
        Snapshot actual = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snap, disk2.getImageId());
        String actualOvf = actual.getVmConfiguration();

        ArrayList<DiskImage> actualImages = new ArrayList<>();
        ovfManager.importVm(actualOvf, new VM(), actualImages, new ArrayList<>());
        assertEquals("Wrong number of disks", 1, actualImages.size());
        assertEquals("Wrong disk", disk1, actualImages.get(0));
    }

    private static DiskImage addTestDisk(VM vm, Guid snapshotId) {
        Guid imageId = Guid.newGuid();
        DiskImage disk = new DiskImage();
        disk.setImageId(imageId);
        disk.setId(Guid.newGuid());
        disk.setVolumeType(VolumeType.Sparse);
        disk.setVolumeFormat(VolumeFormat.COW);
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
