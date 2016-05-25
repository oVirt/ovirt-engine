package org.ovirt.engine.core.utils.ovf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

@RunWith(MockitoJUnitRunner.class)
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

    @Mock
    private OsRepository osRepository;

    @Mock
    private OvfVmIconDefaultsProvider iconDefaultsProvider;

    @ClassRule
    public static MockConfigRule mockConfigRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.VdcVersion, "1.0.0.0"));

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    private OvfManager manager;

    @Before
    public void setUp() throws Exception {
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);
        SimpleDependencyInjector.getInstance().bind(OvfVmIconDefaultsProvider.class, iconDefaultsProvider);
        manager = new OvfManager();
        final HashMap<Integer, String> osIdsToNames = new HashMap<>();
        osIdsToNames.put(DEFAULT_OS_ID, "os_name_a");
        osIdsToNames.put(EXISTING_OS_ID, "os_name_b");
        final List<Pair<GraphicsType, DisplayType>> gndDefaultOs = new ArrayList<>();
        gndDefaultOs.add(new Pair<>(GraphicsType.SPICE, DisplayType.cirrus));
        gndDefaultOs.add(new Pair<>(GraphicsType.VNC, DisplayType.cirrus));
        final List<Pair<GraphicsType, DisplayType>> gndExistingOs = new ArrayList<>();
        gndExistingOs.add(new Pair<>(GraphicsType.SPICE, DisplayType.cirrus));

        when(osRepository.getArchitectureFromOS(any(Integer.class))).thenReturn(ArchitectureType.x86_64);
        when(osRepository.getUniqueOsNames()).thenReturn(osIdsToNames);
        when(osRepository.getOsIdByUniqueName(anyString())).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                for (Map.Entry<Integer, String> entry : osIdsToNames.entrySet()) {
                    if (invocation.getArguments()[0].equals(entry.getValue())) {
                        return entry.getKey();
                    }
                }
                return 0;
            }
        });
        when(osRepository.getGraphicsAndDisplays(eq(DEFAULT_OS_ID), any(Version.class))).thenReturn(gndDefaultOs);
        when(osRepository.getGraphicsAndDisplays(eq(EXISTING_OS_ID), any(Version.class))).thenReturn(gndExistingOs);

        when(iconDefaultsProvider.getVmIconDefaults()).thenReturn(new HashMap<Integer, VmIconIdSizePair>(){{
            put(DEFAULT_OS_ID, new VmIconIdSizePair(SMALL_DEFAULT_ICON_ID, LARGE_DEFAULT_ICON_ID));
            put(EXISTING_OS_ID, new VmIconIdSizePair(SMALL_ICON_ID, LARGE_ICON_ID));
        }});

    }

    private static void assertVm(VM vm, VM newVm, long expectedDbGeneration) {
        assertEquals("imported vm is different than expected", vm, newVm);
        assertEquals("imported db generation is different than expected", expectedDbGeneration, newVm.getDbGeneration());

        // Icons are actually not stored in snapshots, so they are excluded from comparison
        newVm.getStaticData().setSmallIconId(vm.getStaticData().getSmallIconId());
        newVm.getStaticData().setLargeIconId(vm.getStaticData().getLargeIconId());

        assertEquals(vm.getStaticData(), newVm.getStaticData());
    }

    @Test
    public void testVmOvfCreationDefaultGraphicsDevice() throws Exception {
        VM vm = createVM();
        vm.setDefaultDisplayType(DisplayType.cirrus);
        vm.setVmOs(DEFAULT_OS_ID);
        String xml = manager.exportVm(vm, new ArrayList<>(), Version.getLast());
        assertNotNull(xml);
        final VM newVm = new VM();
        manager.importVm(xml, newVm, new ArrayList<>(), new ArrayList<>());
        int graphicsDeviceCount = 0;
        for (VmDevice device : newVm.getManagedVmDeviceMap().values()) {
            if (device.getType() == VmDeviceGeneralType.GRAPHICS) {
                graphicsDeviceCount++;
                assertEquals(device.getDevice(), VmDeviceType.VNC.getName());
            }
        }
        assertEquals(graphicsDeviceCount, 1);
    }

    @Test
    public void testVmOvfCreationDefaultGraphicsDeviceFallbackToSupported() throws Exception {
        VM vm = createVM();
        vm.setDefaultDisplayType(DisplayType.cirrus);
        vm.setVmOs(EXISTING_OS_ID);
        String xml = manager.exportVm(vm, new ArrayList<>(), Version.getLast());
        assertNotNull(xml);
        final VM newVm = new VM();
        manager.importVm(xml, newVm, new ArrayList<>(), new ArrayList<>());
        int graphicsDeviceCount = 0;
        for (VmDevice device : newVm.getManagedVmDeviceMap().values()) {
            if (device.getType() == VmDeviceGeneralType.GRAPHICS) {
                graphicsDeviceCount++;
                assertEquals(device.getDevice(), VmDeviceType.SPICE.getName());
            }
        }
        assertEquals(graphicsDeviceCount, 1);
    }

    @Test
    public void testVmOvfImportWithoutDbGeneration() throws Exception {
        VM vm = createVM();
        String xml = manager.exportVm(vm, new ArrayList<>(), Version.v3_6);
        assertNotNull(xml);
        final VM newVm = new VM();
        assertTrue(xml.contains("Generation"));
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        manager.importVm(replacedXml, newVm, new ArrayList<>(), new ArrayList<>());
        assertVm(vm, newVm, 1);
    }

    @Test
    public void testTemplateOvfCreation() throws Exception {
        VmTemplate template = createVmTemplate();
        String xml = manager.exportTemplate(template, new ArrayList<>(), Version.v3_6);
        assertNotNull(xml);
        final VmTemplate newtemplate = new VmTemplate();
        manager.importTemplate(xml, newtemplate, new ArrayList<>(), new ArrayList<>());
        assertEquals("imported template is different than expected", template, newtemplate);
        assertEquals("imported db generation is different than expected", template.getDbGeneration(), newtemplate.getDbGeneration());
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
        String xml = manager.exportVm(vm, disks, Version.v4_0);
        assertNotNull(xml);

        VM newVm = new VM();
        ArrayList<DiskImage> newDisks = new ArrayList<>();
        manager.importVm(xml, newVm, newDisks, new ArrayList<>());
        String newXml = manager.exportVm(vm, disks, Version.v4_0);

        assertEquals(deleteExportDateValueFromXml(xml), deleteExportDateValueFromXml(newXml));
    }

    @Test
    public void testVmExportAndImportIdentical() throws Exception {
        VM vm = createVM();
        ArrayList<DiskImage> disks = createDisksAndDiskVmElements(vm);
        String xml = manager.exportVm(vm, disks, Version.v4_0);
        assertNotNull(xml);

        VM newVm = new VM();
        ArrayList<DiskImage> newDisks = new ArrayList<>();
        ArrayList<VmNetworkInterface> newInterfaces = new ArrayList<>();
        manager.importVm(xml, newVm, newDisks, newInterfaces);

        assertVm(vm, newVm, vm.getDbGeneration());
        assertCollection(vm.getInterfaces(), newInterfaces);
        assertCollection(disks, newDisks,
                diskPair -> diskPair.getFirst().getDiskVmElementForVm(vm.getId()).
                        equals(diskPair.getSecond().getDiskVmElementForVm(vm.getId())));
    }

    private <T extends BusinessEntity> void assertCollection(List<T> colA, List<T> colB) {
        assertCollection(colA, colB, null);
    }

    private <T extends BusinessEntity> void assertCollection(List<T> colA, List<T> colB, Function<Pair<T, T>, Boolean> function) {
        assertEquals(colA.size(), colB.size());
        assertEquals(CollectionUtils.disjunction(colA, colB).size(), 0);

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
    private String deleteExportDateValueFromXml(String xml) throws Exception{
        String xmlNoExportDate = xml.replaceFirst("<ExportDate>[\\s\\S]*?<\\/ExportDate>", "");
        return xmlNoExportDate;
    }

    private VM serializeAndDeserialize(VM inputVm) throws OvfReaderException {
        String xml = manager.exportVm(inputVm, new ArrayList<>(), Version.v3_6);
        assertNotNull(xml);
        final VM resultVm = new VM();
        assertTrue(xml.contains("Generation"));
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        manager.importVm(replacedXml, resultVm, new ArrayList<>(), new ArrayList<>());
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
        vm.setSingleQxlPci(false);
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setVmOs(EXISTING_OS_ID);
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
        disk.setStoragePoolId(Guid.newGuid());
        disk.setPlugged(true);
        disk.setReadOnly(false);
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
        return template;
    }
}
