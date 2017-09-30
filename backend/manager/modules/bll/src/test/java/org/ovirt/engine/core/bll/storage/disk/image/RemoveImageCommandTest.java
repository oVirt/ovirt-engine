package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.utils.ClusterUtils;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.ovf.OvfVmIconDefaultsProvider;

public class RemoveImageCommandTest extends BaseCommandTest {
    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.VdcVersion, "3.0.0.0"));

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @Mock
    OsRepository osRepository;

    @Mock
    private OvfVmIconDefaultsProvider iconDefaultsProvider;

    @Spy
    @InjectMocks
    private OvfManager ovfManager = new OvfManager();

    @Spy
    private ClusterUtils clusterUtils;

    @Spy
    @InjectMocks
    private ImagesHandler imagesHandler;

    /** The command to test */
    @Spy
    private RemoveImageCommand<RemoveImageParameters> cmd =
            new RemoveImageCommand<>(new RemoveImageParameters(Guid.newGuid()), null);

    @SuppressWarnings("serial")
    @Before
    public void setUp() {
        when(iconDefaultsProvider.getVmIconDefaults()).thenReturn(new HashMap<Integer, VmIconIdSizePair>(){{
            put(0, new VmIconIdSizePair(
                    Guid.createGuidFromString("00000000-0000-0000-0000-00000000000a"),
                    Guid.createGuidFromString("00000000-0000-0000-0000-00000000000b")));
        }});

        doNothing().when(ovfManager).updateBootOrderOnDevices(any(), anyBoolean());
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

        mcr.mockConfigValue(ConfigValues.PassDiscardSupported, Version.getLast(), true);
        mcr.mockConfigValue(ConfigValues.PassDiscardSupported, Version.ALL.get(0), true);
        mcr.mockConfigValue(ConfigValues.MaxNumOfVmSockets, Version.getLast(), 16);
        mcr.mockConfigValue(ConfigValues.MaxNumOfVmSockets, Version.ALL.get(0), 16);
        mcr.mockConfigValue(ConfigValues.MaxNumOfVmCpus, Version.getLast(), 16);
        mcr.mockConfigValue(ConfigValues.MaxNumOfVmCpus, Version.ALL.get(0), 16);

        ArrayList<DiskImage> disks = new ArrayList<>(Arrays.asList(disk1, disk2));
        FullEntityOvfData fullEntityOvfDataForExport = new FullEntityOvfData(vm);
        fullEntityOvfDataForExport.setDiskImages(disks);
        String ovf = ovfManager.exportVm(vm, fullEntityOvfDataForExport, Version.getLast());
        Snapshot snap = new Snapshot();
        snap.setVmConfiguration(ovf);
        snap.setId(vmSnapshotId);

        doReturn(disk2).when(cmd).getDiskImage();
        doReturn(disk2).when(cmd).getImage();
        doReturn(disk2.getId()).when(cmd).getImageId();
        Snapshot actual = imagesHandler.prepareSnapshotConfigWithAlternateImage(snap, disk2.getImageId(), null, ovfManager);
        String actualOvf = actual.getVmConfiguration();

        VM emptyVm = new VM();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(emptyVm);
        ovfManager.importVm(actualOvf, emptyVm, fullEntityOvfData);
        assertEquals("Wrong number of disks", 1, fullEntityOvfData.getDiskImages().size());
        assertEquals("Wrong disk", disk1, fullEntityOvfData.getDiskImages().get(0));
    }

    private static DiskImage addTestDisk(VM vm, Guid snapshotId) {
        Guid imageId = Guid.newGuid();
        DiskImage disk = new DiskImage();
        disk.setImageId(imageId);
        disk.setId(Guid.newGuid());
        disk.setVolumeType(VolumeType.Sparse);
        disk.setVolumeFormat(VolumeFormat.COW);
        disk.setQcowCompat(QcowCompat.QCOW2_V3);
        disk.setStoragePoolId(vm.getStoragePoolId());
        disk.setActive(Boolean.TRUE);
        disk.setPlugged(Boolean.TRUE);
        disk.setVmSnapshotId(snapshotId);
        disk.setImageStatus(ImageStatus.OK);
        disk.setAppList("");
        disk.setDescription("");
        vm.getDiskList().add(disk);
        vm.getDiskMap().put(imageId, disk);
        return disk;
    }
}
