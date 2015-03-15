package org.ovirt.engine.core.utils.ovf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class OvfManagerTest {

    @Mock
    private OsRepository osRepository;

    @ClassRule
    public static MockConfigRule mockConfigRule =
            new MockConfigRule(MockConfigRule.mockConfig(ConfigValues.VdcVersion, "1.0.0.0"));

    private OvfManager manager;

    @Before
    public void setUp() throws Exception {
        manager = new OvfManager();
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
        HashMap<Integer, String> osIdsToNames = new HashMap<>();

        when(osRepository.getArchitectureFromOS(any(Integer.class))).thenReturn(ArchitectureType.x86_64);
        when(osRepository.getUniqueOsNames()).thenReturn(osIdsToNames);
    }

    private static void assertVm(VM vm, VM newVm, long expectedDbGeneration) {
        assertEquals("imported vm is different than expected", vm, newVm);
        assertEquals("imported db generation is different than expected", expectedDbGeneration, newVm.getDbGeneration());
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
        return template;
    }
}
