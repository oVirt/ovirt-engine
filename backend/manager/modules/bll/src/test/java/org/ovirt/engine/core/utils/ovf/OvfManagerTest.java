package org.ovirt.engine.core.utils.ovf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class, RandomUtilsSeedingExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class OvfManagerTest {

    private static final Guid SMALL_DEFAULT_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000a");
    private static final Guid LARGE_DEFAULT_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000b");
    private static final Guid SMALL_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000c");
    private static final Guid LARGE_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000d");

    private static final int DEFAULT_OS_ID = OsRepository.DEFAULT_X86_OS;
    private static final int EXISTING_OS_ID = 1;
    private static final int NONEXISTING_OS_ID = 2;

    private static final int MIN_ENTITY_NAME_LENGTH = 3;
    private static final int MAX_ENTITY_NAME_LENGTH = 30;

    private static Map<String, Integer> createMaxNumberOfVmCpusMap() {
        Map<String, Integer> maxVmCpusMap = new HashMap<>();
        maxVmCpusMap.put("s390x", 384);
        maxVmCpusMap.put("x86", 16);
        maxVmCpusMap.put("ppc", 384);
        return maxVmCpusMap;
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.VdcVersion, "3.0.0.0"),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets, Version.v4_2, 16),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, Version.v4_2, createMaxNumberOfVmCpusMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets, Version.v4_3, 16),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, Version.v4_3, createMaxNumberOfVmCpusMap()),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmSockets, Version.getLast(), 16),
                MockConfigDescriptor.of(ConfigValues.MaxNumOfVmCpus, Version.getLast(), createMaxNumberOfVmCpusMap())
        );
    }

    @InjectMocks
    @Spy
    private OvfManager manager = new OvfManager();
    @Mock
    private OvfVmIconDefaultsProvider iconDefaultsProvider;
    @Mock
    private OsRepository osRepository;
    @Spy
    @InjectMocks
    private ImagesHandler imagesHandler;

    @BeforeEach
    public void setUp() {
        final Map<Integer, String> osIdsToNames = new HashMap<>();
        osIdsToNames.put(DEFAULT_OS_ID, "os_name_a");
        osIdsToNames.put(EXISTING_OS_ID, "os_name_b");
        final List<Pair<GraphicsType, DisplayType>> gndDefaultOs = new ArrayList<>();
        gndDefaultOs.add(new Pair<>(GraphicsType.SPICE, DisplayType.vga));
        gndDefaultOs.add(new Pair<>(GraphicsType.VNC, DisplayType.vga));
        final List<Pair<GraphicsType, DisplayType>> gndExistingOs = new ArrayList<>();
        gndExistingOs.add(new Pair<>(GraphicsType.SPICE, DisplayType.qxl));
        when(osRepository.getArchitectureFromOS(anyInt())).thenReturn(ArchitectureType.x86_64);
        when(osRepository.getUniqueOsNames()).thenReturn(osIdsToNames);
        when(osRepository.getOsIdByUniqueName(any())).thenAnswer(
                invocation-> osIdsToNames.entrySet()
                        .stream()
                        .filter(k -> invocation.getArguments()[0].equals(k.getValue()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(0));
        when(osRepository.getGraphicsAndDisplays(eq(DEFAULT_OS_ID), any())).thenReturn(gndDefaultOs);
        when(osRepository.getGraphicsAndDisplays(eq(EXISTING_OS_ID), any())).thenReturn(gndExistingOs);

        doNothing().when(manager).updateBootOrderOnDevices(any(), anyBoolean());

        Map<Integer, VmIconIdSizePair> iconDefaults = new HashMap<>();
        iconDefaults.put(DEFAULT_OS_ID, new VmIconIdSizePair(SMALL_DEFAULT_ICON_ID, LARGE_DEFAULT_ICON_ID));
        iconDefaults.put(EXISTING_OS_ID, new VmIconIdSizePair(SMALL_ICON_ID, LARGE_ICON_ID));
        when(iconDefaultsProvider.getVmIconDefaults()).thenReturn(iconDefaults);
    }

    private static void assertVm(VM vm, VM newVm, long expectedDbGeneration) {
        assertEquals(vm, newVm, "imported vm is different than expected");
        assertEquals(expectedDbGeneration, newVm.getDbGeneration(), "imported db generation is different than expected");

        // Icons are actually not stored in snapshots, so they are excluded from comparison
        newVm.getStaticData().setSmallIconId(vm.getStaticData().getSmallIconId());
        newVm.getStaticData().setLargeIconId(vm.getStaticData().getLargeIconId());

        assertEquals(vm.getStaticData(), newVm.getStaticData());
    }

    @Test
    public void testVmOvfCreationDefaultGraphicsDevice() throws Exception {
        VM vm = createVM();
        vm.setDefaultDisplayType(DisplayType.vga);
        vm.setVmOs(DEFAULT_OS_ID);
        String xml = manager.exportVm(vm, new FullEntityOvfData(vm), Version.getLast());
        assertNotNull(xml);
        final VM newVm = new VM();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(newVm);
        manager.importVm(xml, newVm, fullEntityOvfData);
        int graphicsDeviceCount = 0;
        for (VmDevice device : newVm.getManagedVmDeviceMap().values()) {
            if (device.getType() == VmDeviceGeneralType.GRAPHICS) {
                graphicsDeviceCount++;
                assertEquals(device.getDevice(), VmDeviceType.VNC.getName());
            }
        }
        assertEquals(1, graphicsDeviceCount);
    }

    @Test
    public void testVmOvfCreationDefaultGraphicsDeviceFallbackToSupported() throws Exception {
        VM vm = createVM();
        vm.setDefaultDisplayType(DisplayType.qxl);
        vm.setVmOs(EXISTING_OS_ID);
        String xml = manager.exportVm(vm,  new FullEntityOvfData(vm), Version.getLast());
        assertNotNull(xml);
        final VM newVm = new VM();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(newVm);
        manager.importVm(xml, newVm, fullEntityOvfData);
        int graphicsDeviceCount = 0;
        for (VmDevice device : newVm.getManagedVmDeviceMap().values()) {
            if (device.getType() == VmDeviceGeneralType.GRAPHICS) {
                graphicsDeviceCount++;
                assertEquals(device.getDevice(), VmDeviceType.SPICE.getName());
            }
        }
        assertEquals(1, graphicsDeviceCount);
    }

    @Test
    public void testVmOvfImportWithoutDbGeneration() throws Exception {
        VM vm = createVM();
        String xml = manager.exportVm(vm, new FullEntityOvfData(vm), Version.v4_2);
        assertNotNull(xml);
        final VM newVm = new VM();
        assertTrue(xml.contains("Generation"));
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(newVm);
        manager.importVm(replacedXml, newVm, fullEntityOvfData);
        assertVm(vm, newVm, 1);
    }

    @Test
    public void testTemplateOvfCreation() throws Exception {
        VmTemplate template = createVmTemplate();
        String xml = manager.exportTemplate(new FullEntityOvfData(template), Version.v4_2);
        assertNotNull(xml);
        final VmTemplate newtemplate = new VmTemplate();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(newtemplate);
        manager.importTemplate(xml, fullEntityOvfData);
        assertEquals(newtemplate, template, "imported template is different than expected");
        assertEquals(template.getDbGeneration(), newtemplate.getDbGeneration(), "imported db generation is different than expected");
    }


    @Test
    public void testIconsSetForKnownOs() throws Exception {
        VM vm = createVM();
        vm.setVmOs(EXISTING_OS_ID);
        final VM newVm = serializeAndDeserialize(vm);
        assertEquals(SMALL_ICON_ID, newVm.getStaticData().getSmallIconId());
        assertEquals(LARGE_ICON_ID, newVm.getStaticData().getLargeIconId());
    }

    @Test
    public void testIconsSetForUnknownOs() throws Exception {
        VM vm = createVM();
        vm.setVmOs(NONEXISTING_OS_ID);
        final VM newVm = serializeAndDeserialize(vm);
        assertEquals(SMALL_DEFAULT_ICON_ID, newVm.getStaticData().getSmallIconId());
        assertEquals(LARGE_DEFAULT_ICON_ID, newVm.getStaticData().getLargeIconId());
    }

    @Test
    public void testVmExportAndImportAndExportAgainSymmetrical() throws Exception {
        VM vm = createVM();
        ArrayList<DiskImage> disks = createDisksAndDiskVmElements(vm);
        FullEntityOvfData fullEntityOvfDataForExport = new FullEntityOvfData(vm);
        fullEntityOvfDataForExport.setDiskImages(disks);
        String xml = manager.exportVm(vm, fullEntityOvfDataForExport, Version.v4_3);
        assertNotNull(xml);

        VM newVm = new VM();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(newVm);
        manager.importVm(xml, newVm, fullEntityOvfData);
        FullEntityOvfData fullEntityOvfDataForExportResult = new FullEntityOvfData(vm);
        fullEntityOvfDataForExportResult.setDiskImages(disks);
        String newXml = manager.exportVm(vm, fullEntityOvfDataForExportResult, Version.v4_3);

        assertEquals(deleteExportDateValueFromXml(xml), deleteExportDateValueFromXml(newXml));
    }

    @Test
    public void testVmExportAndImportIdentical() throws Exception {
        VM vm = createVM();
        ArrayList<DiskImage> disks = createDisksAndDiskVmElements(vm);
        FullEntityOvfData fullEntityOvfDataForExport = new FullEntityOvfData(vm);
        fullEntityOvfDataForExport.setDiskImages(disks);
        String xml = manager.exportVm(vm, fullEntityOvfDataForExport, Version.v4_3);
        assertNotNull(xml);

        VM newVm = new VM();
        newVm.setClusterBiosType(BiosType.Q35_SEA_BIOS);
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(newVm);
        manager.importVm(xml, newVm, fullEntityOvfData);

        assertVm(vm, newVm, vm.getDbGeneration());
        assertCollection(vm.getInterfaces(), fullEntityOvfData.getInterfaces());
        assertCollection(disks, fullEntityOvfData.getDiskImages(),
                diskPair -> diskPair.getFirst().getDiskVmElementForVm(vm.getId()).
                        equals(diskPair.getSecond().getDiskVmElementForVm(vm.getId())));
    }

    private <T extends BusinessEntity<?>> void assertCollection(List<T> colA, List<T> colB) {
        assertCollection(colA, colB, null);
    }

    private <T extends BusinessEntity<?>> void assertCollection(List<T> colA, List<T> colB, Function<Pair<T, T>, Boolean> function) {
        assertEquals(colA.size(), colB.size());
        assertEquals(0, CollectionUtils.disjunction(colA, colB).size());

        // Might look a bit overkill to check equals as well but disjunction is based on the hash so double checking is good
        for (T itemA : colA) {
            T itemB = colB.stream().filter(t -> t.getId().equals(itemA.getId())).findFirst().get();
            assertEquals(itemA, itemB);
            if (function != null) {
                assertTrue(function.apply(new Pair<>(itemA, itemB)));
            }
        }

    }

    // TODO: An ugly hack since the writer writes the export date with the current time of the export which is
    //       different obviously given the time passed between the two exports.
    //       for now the export date will just be removed but in the future it's better to inject the date to the
    //       writer.
    private String deleteExportDateValueFromXml(String xml) {
        return xml.replaceFirst("<ExportDate>[\\s\\S]*?<\\/ExportDate>", "");
    }

    private VM serializeAndDeserialize(VM inputVm) throws OvfReaderException {
        String xml = manager.exportVm(inputVm, new FullEntityOvfData(inputVm), Version.v4_2);
        assertNotNull(xml);
        final VM resultVm = new VM();
        assertTrue(xml.contains("Generation"));
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(resultVm);
        manager.importVm(replacedXml, resultVm, fullEntityOvfData);
        return resultVm;
    }

    private static VM createVM() {
        VM vm = new VM();
        vm.setName("test-vm");
        vm.setOrigin(OriginType.OVIRT);
        vm.setId(Guid.newGuid());
        vm.setVmDescription("test-description");
        vm.setTimeZone("Israel Standard Time");
        vm.setDbGeneration(2L);
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setVmOs(EXISTING_OS_ID);
        vm.setClusterBiosType(BiosType.Q35_SEA_BIOS);
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        initInterfaces(vm);
        return vm;
    }

    private static void initInterfaces(VM vm) {
        List<VmNetworkInterface> ifaces = new ArrayList<>();
        RandomUtils rnd = RandomUtils.instance();
        for (int i = 0; i < rnd.nextInt(2, 10); i++) {
            VmNetworkInterface vmInterface = new VmNetworkInterface();
            vmInterface.setStatistics(new VmNetworkStatistics());
            vmInterface.setId(Guid.newGuid());
            vmInterface.setVmId(vm.getId());
            vmInterface.setName(generateRandomName());
            vmInterface.setVnicProfileName(generateRandomName());
            vmInterface.setNetworkName(generateRandomName());
            vmInterface.setLinked(rnd.nextBoolean());
            vmInterface.setSpeed(rnd.nextInt(1000));
            vmInterface.setType(rnd.nextInt(10));
            vmInterface.setMacAddress(rnd.nextMacAddress());

            ifaces.add(vmInterface);
        }
        vm.setInterfaces(ifaces);
    }

    private static ArrayList<DiskImage> createDisksAndDiskVmElements(VM vm) {
        ArrayList<DiskImage> disks = new ArrayList<>();
        RandomUtils rnd = RandomUtils.instance();
        for (int i = 0; i < rnd.nextInt(3, 10); i++) {
            DiskImage disk = createVmDisk(vm);
            disks.add(disk);
        }
        return disks;
    }

    private static DiskImage createVmDisk(VM vm) {
        RandomUtils rnd = RandomUtils.instance();
        DiskImage disk = new DiskImage();
        disk.setId(Guid.newGuid());
        disk.setVmSnapshotId(Guid.newGuid());
        disk.setSize(rnd.nextLong(1000));
        disk.setActualSize(rnd.nextLong(1000));
        disk.setVolumeFormat(rnd.nextEnum(VolumeFormat.class));
        disk.setVolumeType(rnd.nextEnum(VolumeType.class));
        disk.setWipeAfterDelete(rnd.nextBoolean());
        disk.setDiskAlias(generateRandomName());
        disk.setDescription(generateRandomName());
        disk.setImageId(Guid.newGuid());
        disk.setStorageIds(Arrays.asList(Guid.newGuid(), Guid.newGuid()));
        disk.setStoragePoolId(Guid.newGuid());
        disk.setPlugged(true);
        disk.setAppList(rnd.nextPropertyString(100));

        Image image = new Image();
        image.setActive(true);
        image.setVolumeFormat(rnd.nextEnum(VolumeFormat.class));
        image.setId(disk.getImageId());
        image.setSnapshotId(disk.getSnapshotId());
        image.setStatus(ImageStatus.OK);
        disk.setImage(image);

        DiskVmElement diskVmElement = new DiskVmElement(disk.getId(), vm.getId());
        diskVmElement.setBoot(rnd.nextBoolean());
        diskVmElement.setDiskInterface(rnd.nextEnum(DiskInterface.class));
        diskVmElement.setReadOnly(false);
        diskVmElement.setPlugged(true);
        disk.setDiskVmElements(Collections.singletonList(diskVmElement));

        return disk;
    }

    private static String generateRandomName() {
        RandomUtils rnd = RandomUtils.instance();
        return rnd.nextPropertyString(rnd.nextInt(MIN_ENTITY_NAME_LENGTH, MAX_ENTITY_NAME_LENGTH));
    }

    private static VmTemplate createVmTemplate() {
        VmTemplate template = new VmTemplate();
        template.setName("test-template");
        template.setOrigin(OriginType.OVIRT);
        template.setId(Guid.newGuid());
        template.setDescription("test-description");
        template.setDbGeneration(2L);
        template.setClusterArch(ArchitectureType.x86_64);
        template.setOsId(EXISTING_OS_ID);
        template.setClusterBiosType(BiosType.Q35_SEA_BIOS);
        template.setBiosType(BiosType.Q35_SEA_BIOS);
        return template;
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
        vm.setClusterBiosType(BiosType.Q35_SEA_BIOS);
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setBiosType(BiosType.Q35_SEA_BIOS);
        Guid vmSnapshotId = Guid.newGuid();

        DiskImage disk1 = addTestDisk(vm, vmSnapshotId);
        DiskVmElement dve1 = new DiskVmElement(disk1.getId(), vm.getId());
        dve1.setDiskInterface(DiskInterface.VirtIO);
        disk1.setDiskVmElements(Collections.singletonList(dve1));

        DiskImage disk2 = addTestDisk(vm, vmSnapshotId);
        DiskVmElement dve2 = new DiskVmElement(disk2.getId(), vm.getId());
        dve2.setDiskInterface(DiskInterface.IDE);
        disk2.setDiskVmElements(Collections.singletonList(dve2));

        ArrayList<DiskImage> disks = new ArrayList<>(Arrays.asList(disk1, disk2));
        FullEntityOvfData fullEntityOvfDataForExport = new FullEntityOvfData(vm);
        fullEntityOvfDataForExport.setDiskImages(disks);
        String ovf = manager.exportVm(vm, fullEntityOvfDataForExport, Version.v4_3);
        Snapshot snap = new Snapshot();
        snap.setVmConfiguration(ovf);
        snap.setId(vmSnapshotId);

        Snapshot actual = imagesHandler.prepareSnapshotConfigWithAlternateImage(snap, disk2.getImageId(), null, manager);
        String actualOvf = actual.getVmConfiguration();

        VM emptyVm = new VM();
        FullEntityOvfData fullEntityOvfData = new FullEntityOvfData(emptyVm);
        manager.importVm(actualOvf, emptyVm, fullEntityOvfData);
        assertEquals(1, fullEntityOvfData.getDiskImages().size(), "Wrong number of disks");
        assertEquals(disk1, fullEntityOvfData.getDiskImages().get(0), "Wrong disk");
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
