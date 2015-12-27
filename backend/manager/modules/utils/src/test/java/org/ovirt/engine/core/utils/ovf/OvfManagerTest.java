package org.ovirt.engine.core.utils.ovf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
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
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.queries.VmIconIdSizePair;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class OvfManagerTest {

    private static final Guid SMALL_DEFAULT_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000a");
    private static final Guid LARGE_DEFAULT_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000b");
    private static final Guid SMALL_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000c");
    private static final Guid LARGE_ICON_ID = Guid.createGuidFromString("00000000-0000-0000-0000-00000000000d");

    private static final int DEFAULT_OS_ID = OsRepository.DEFAULT_X86_OS;
    private static final int EXISTING_OS_ID = 1;
    private static final int NONEXISTING_OS_ID = 2;

    @Mock
    private OsRepository osRepository;

    @Mock
    private OvfVmIconDefaultsProvider iconDefaultsProvider;

    @ClassRule
    public static MockConfigRule mockConfigRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.VdcVersion, "1.0.0.0"));

    private OvfManager manager;

    @Before
    public void setUp() throws Exception {
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
        SimpleDependecyInjector.getInstance().bind(OvfVmIconDefaultsProvider.class, iconDefaultsProvider);
        manager = new OvfManager();
        final HashMap<Integer, String> osIdsToNames = new HashMap<Integer, String>(){{
            put(DEFAULT_OS_ID, "os_name_a");
            put(EXISTING_OS_ID, "os_name_b");
        }};
        final List<Pair<GraphicsType, DisplayType>> gndDefaultOs = new ArrayList<Pair<GraphicsType, DisplayType>>();
        gndDefaultOs.add(new Pair<GraphicsType, DisplayType>(GraphicsType.SPICE, DisplayType.cirrus));
        gndDefaultOs.add(new Pair<GraphicsType, DisplayType>(GraphicsType.VNC, DisplayType.cirrus));
        final List<Pair<GraphicsType, DisplayType>> gndExistingOs = new ArrayList<Pair<GraphicsType, DisplayType>>();
        gndExistingOs.add(new Pair<GraphicsType, DisplayType>(GraphicsType.SPICE, DisplayType.cirrus));

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
    public void testVmOvfCreation() throws Exception {
        VM vm = createVM();
        String xml = manager.ExportVm(vm, new ArrayList<DiskImage>(), Version.v3_1);
        assertNotNull(xml);
        final VM newVm = new VM();
        manager.ImportVm(xml, newVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
        assertVm(vm, newVm, vm.getDbGeneration());
    }

    @Test
    public void testVmOvfCreationBackwardCompatilibily() throws Exception {
        VM vm = createVM();
        String xml = manager.ExportVm(vm, new ArrayList<DiskImage>(), Version.v3_0);
        assertNotNull(xml);
        final VM newVm = new VM();
        manager.ImportVm(xml, newVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
        assertVm(vm, newVm, vm.getDbGeneration());
    }

    @Test
    public void testVmOvfCreationDefaultGraphicsDevice() throws Exception {
        VM vm = createVM();
        vm.setDefaultDisplayType(DisplayType.cirrus);
        vm.setVmOs(DEFAULT_OS_ID);
        String xml = manager.ExportVm(vm, new ArrayList<DiskImage>(), Version.v3_5);
        assertNotNull(xml);
        final VM newVm = new VM();
        manager.ImportVm(xml, newVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
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
        String xml = manager.ExportVm(vm, new ArrayList<DiskImage>(), Version.v3_5);
        assertNotNull(xml);
        final VM newVm = new VM();
        manager.ImportVm(xml, newVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
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
        String xml = manager.ExportVm(vm, new ArrayList<DiskImage>(), Version.v3_1);
        assertNotNull(xml);
        final VM newVm = new VM();
        assertTrue(xml.contains("Generation"));
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        manager.ImportVm(replacedXml, newVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
        assertVm(vm, newVm, 1);
    }

    @Test
    public void testVmOvfImportWithoutDbGenerationBackwardCompatilibily() throws Exception {
        VM vm = createVM();
        String xml = manager.ExportVm(vm, new ArrayList<DiskImage>(), Version.v3_0);
        assertNotNull(xml);
        final VM newVm = new VM();
        assertTrue(xml.contains("Generation"));
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        manager.ImportVm(replacedXml, newVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
        assertVm(vm, newVm, 1);
    }

    @Test
    public void testTemplateOvfCreation() throws Exception {
        VmTemplate template = createVmTemplate();
        String xml = manager.ExportTemplate(template, new ArrayList<DiskImage>(), Version.v3_1);
        assertNotNull(xml);
        final VmTemplate newtemplate = new VmTemplate();
        manager.ImportTemplate(xml, newtemplate, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
        assertEquals("imported template is different than expected", template, newtemplate);
        assertEquals("imported db generation is different than expected", template.getDbGeneration(), newtemplate.getDbGeneration());
    }

    @Test
    public void testTemplateOvfCreationBackwardCompatiliblity() throws Exception {
        VmTemplate template = createVmTemplate();
        String xml = manager.ExportTemplate(template, new ArrayList<DiskImage>(), Version.v3_0);
        assertNotNull(xml);
        final VmTemplate newtemplate = new VmTemplate();
        manager.ImportTemplate(xml, newtemplate, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
        assertEquals("imported template is different than expected", template, newtemplate);
        assertEquals("imported db generation is different than expected", template.getDbGeneration(), newtemplate.getDbGeneration());
    }

    @Test
    public void testTemplateOvfImportWithoutDbGeneration() throws Exception {
        VmTemplate template = createVmTemplate();
        String xml = manager.ExportTemplate(template, new ArrayList<DiskImage>(), Version.v3_1);
        assertNotNull(xml);
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        final VmTemplate newtemplate = new VmTemplate();
        manager.ImportTemplate(replacedXml,
                newtemplate,
                new ArrayList<DiskImage>(),
                new ArrayList<VmNetworkInterface>());
        assertEquals("imported template is different than expected", template, newtemplate);
        assertTrue("imported db generation is different than expected", newtemplate.getDbGeneration() == 1);
    }

    @Test
    public void testTemplateOvfImportWithoutDbGenerationBackwardCompatiliblity() throws Exception {
        VmTemplate template = createVmTemplate();
        String xml = manager.ExportTemplate(template, new ArrayList<DiskImage>(), Version.v3_0);
        assertNotNull(xml);
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        final VmTemplate newtemplate = new VmTemplate();
        manager.ImportTemplate(replacedXml,
                newtemplate,
                new ArrayList<DiskImage>(),
                new ArrayList<VmNetworkInterface>());
        assertEquals("imported template is different than expected", template, newtemplate);
        assertTrue("imported db generation is different than expected", newtemplate.getDbGeneration() == 1);
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

    private VM serializeAndDeserialize(VM inputVm) throws OvfReaderException {
        String xml = manager.ExportVm(inputVm, new ArrayList<DiskImage>(), Version.v3_1);
        assertNotNull(xml);
        final VM resultVm = new VM();
        assertTrue(xml.contains("Generation"));
        String replacedXml = xml.replaceAll("Generation", "test_replaced");
        manager.ImportVm(replacedXml, resultVm, new ArrayList<DiskImage>(), new ArrayList<VmNetworkInterface>());
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
        VmNetworkInterface vmInterface = new VmNetworkInterface();
        vmInterface.setStatistics(new VmNetworkStatistics());
        vmInterface.setId(Guid.newGuid());
        vmInterface.setName("eth77");
        vmInterface.setNetworkName("blue");
        vmInterface.setLinked(false);
        vmInterface.setSpeed(1000);
        vmInterface.setType(3);
        vmInterface.setMacAddress("01:C0:81:21:71:17");

        VmNetworkInterface vmInterface2 = new VmNetworkInterface();
        vmInterface2.setStatistics(new VmNetworkStatistics());
        vmInterface2.setId(Guid.newGuid());
        vmInterface2.setName("eth88");
        vmInterface2.setNetworkName(null);
        vmInterface2.setLinked(true);
        vmInterface2.setSpeed(1234);
        vmInterface2.setType(1);
        vmInterface2.setMacAddress("02:C1:92:22:25:28");

        vm.setInterfaces(Arrays.asList(vmInterface, vmInterface2));
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
