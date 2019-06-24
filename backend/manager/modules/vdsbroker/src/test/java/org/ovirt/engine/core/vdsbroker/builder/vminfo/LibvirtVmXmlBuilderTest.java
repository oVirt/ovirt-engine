package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder.adjustSpiceSecureChannels;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.ovf.xml.XmlTextWriter;

public class LibvirtVmXmlBuilderTest {
    MockitoSession mockito;

    @BeforeEach
    void setUp() {
        //initialize session to start mocking
        mockito = Mockito.mockitoSession()
                .initMocks(this)
                .strictness(Strictness.LENIENT)
                .startMocking();
    }

    @AfterEach
    void tearDown() {
        //It is necessary to finish the session so that Mockito
        // can detect incorrect stubbing and validate Mockito usage
        //'finishMocking()' is intended to be used in your test framework's 'tear down' method.
        mockito.finishMocking();
    }


    @Test
    public void testSpiceSecureChannelsAdjustment() {
        String[] channels = new String[]{"smain", "dog", "", "sinputs", "scursor", "display", "scat", "smartcard", "splayback"};
        List<String> adjustedChannels = adjustSpiceSecureChannels(channels).collect(Collectors.toList());
        assertEquals(Arrays.asList("main", "inputs", "cursor", "display", "smartcard", "playback"), adjustedChannels);
    }

    @SuppressWarnings("unused")
    public static Stream<MockConfigDescriptor<?>> vgpuPlacementNotSupported() {
        return Stream.concat(
            Stream.of(MockConfigDescriptor.of(ConfigValues.VgpuPlacementSupported, Version.v4_3, Boolean.FALSE)),
            Stream.of(MockConfigDescriptor.of(ConfigValues.VgpuPlacementSupported, Version.v4_2, Boolean.FALSE)));
    }

    public static Stream<MockConfigDescriptor<?>> tscConfig() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.SendSMPOnRunVm, false));
    }

    @Test
    @MockedConfig("vgpuPlacementNotSupported")
    void testVideoNoDisplay() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        doCallRealMethod().when(underTest).writeVideo(any());
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();

        setUpMdevTest(underTest, writer, properties);
        VM vm = getVm(underTest);

        VmDevice device = mock(VmDevice.class);
        when(device.getDevice()).thenReturn("testDevice");
        properties.put("mdev_type", "nvidia28");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));

        underTest.writeVideo(device);
        verify(writer, times(1)).writeAttributeString("type", "none");

        reset(writer);
        properties.put("mdev_type", "nodisplay,nvidia28");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVideo(device);
        verify(writer, times(0)).writeAttributeString("type", "none");
        verify(writer, times(1)).writeAttributeString("type", "testDevice");
    }

    @Test
    @MockedConfig("vgpuPlacementNotSupported")
    void testMdevNodisplay() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();

        setUpMdevTest(underTest, writer, properties);
        VM vm = getVm(underTest);

        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // display="on" is the default
        reset(writer);
        properties.put("mdev_type", "nvidia28");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVGpu();
        verify(writer, times(1)).writeAttributeString("display", "on");

        // display="on" is inserted for each mdev
        reset(writer);
        properties.put("mdev_type", "nvidia28,nvidia10");
        underTest.writeVGpu();
        verify(writer, times(2)).writeAttributeString("display", "on");

        // nodisplay prevents adding display="on" in the xml
        reset(writer);
        properties.put("mdev_type", "nodisplay,nvidia28");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");

        // nodisplay affects all mdevs
        reset(writer);
        properties.put("mdev_type", "nodisplay,nvidia10,nvidia28");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");

        // nodisplay must be the first entry in the mdev_type list
        reset(writer);
        properties.put("mdev_type", "nvidia28,nodisplay");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVGpu();
        verify(writer, times(2)).writeAttributeString("display", "on");

        // When there's only nodisplay in mdev list, no hostdev elements are added
        reset(writer);
        properties.put("mdev_type", "nodisplay");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // Empty and null mdev_types produce no hostdev elements
        reset(writer);
        properties.put("mdev_type", "");
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        reset(writer);
        properties.put("mdev_type", null);
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // display="on" is not included in cluster version < 4.3
        reset(writer);
        properties.put("mdev_type", "nvidia28");
        VM vm2 = mock(VM.class);
        when(vm2.getCompatibilityVersion()).thenReturn(Version.v4_2);
        setVm(underTest, vm2);
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm2));
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");
    }

    @Test
    void testNoneVideo() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);

        setupNoneVideoTest(underTest);
        setWriter(underTest, writer);

        underTest.writeDevices();
        verify(writer, times(1)).writeStartElement("video");
        verify(writer, times(1)).writeStartElement("model");
        verify(writer, times(1)).writeAttributeString("type", "none");
    }

    private void setupNoneVideoTest(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException {
        doCallRealMethod().when(underTest).writeDevices();

        VmInfoBuildUtils utils = mock(VmInfoBuildUtils.class);
        when(utils.getVmDevices(any())).thenReturn(new ArrayList<>());
        setVmInfoBuildUtils(underTest, utils);

        VM vm = mock(VM.class);
        when(vm.getId()).thenReturn(Guid.newGuid());
        when(vm.getClusterArch()).thenReturn(ArchitectureType.x86_64);
        when(vm.getBiosType()).thenReturn(BiosType.I440FX_SEA_BIOS);
        when(vm.getBootSequence()).thenReturn(BootSequence.C);
        setVm(underTest, vm);

        setVolumeLeases(underTest, new ArrayList<>());
    }

    private void setVolumeLeases(LibvirtVmXmlBuilder underTest, ArrayList<Object> volumeLeases) throws NoSuchFieldException {
        Field volumeLeasesField = LibvirtVmXmlBuilder.class.getDeclaredField("volumeLeases");
        FieldSetter.setField(underTest, volumeLeasesField, volumeLeases);
    }

    void testIsTscFrequencyNeeded() throws NoSuchFieldException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.Desktop);
        when(vm.getMigrationSupport()).thenReturn(MigrationSupport.PINNED_TO_HOST);

        setUpTscTest(underTest, vm, writer, properties);

        // CPU does not support invtsc
        setCpuFlagsSupplier(underTest, "tsc,constant_tsc");
        assertFalse(underTest.isTscFrequencyNeeded());

        setCpuFlagsSupplier(underTest, "tsc,constant_tsc,nonstop_tsc");

        // Not migratable, not HP
        assertFalse(underTest.isTscFrequencyNeeded());

        // Not migratable, HP VM
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        assertFalse(underTest.isTscFrequencyNeeded());

        // Migratable, HP VM
        when(vm.getMigrationSupport()).thenReturn(MigrationSupport.MIGRATABLE);
        assertTrue(underTest.isTscFrequencyNeeded());

        // Migratable, not HP
        when(vm.getVmType()).thenReturn(VmType.Server);
        assertFalse(underTest.isTscFrequencyNeeded());

        when(vm.getVmType()).thenReturn(VmType.Desktop);
        assertFalse(underTest.isTscFrequencyNeeded());

        // Not on PPC or s390
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getClusterArch()).thenReturn(ArchitectureType.ppc);
        assertFalse(underTest.isTscFrequencyNeeded());

        when(vm.getClusterArch()).thenReturn(ArchitectureType.s390x);
        assertFalse(underTest.isTscFrequencyNeeded());
    }

    @Test
    @MockedConfig("tscConfig")
    void testTscFrequencyCpu() throws NoSuchFieldException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getMigrationSupport()).thenReturn(MigrationSupport.MIGRATABLE);

        setUpTscTest(underTest, vm, writer, properties);
        setCpuFlagsSupplier(underTest, "tsc,constant_tsc,nonstop_tsc");

        underTest.writeCpu(false);
        verify(writer, times(1)).writeStartElement("feature");
        verify(writer, times(1)).writeAttributeString("name", "invtsc");
        verify(writer, times(1)).writeAttributeString("policy", "require");
    }

    @Test
    @MockedConfig("tscConfig")
    void testTscFrequencyTimer() throws NoSuchFieldException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getVmType()).thenReturn(VmType.HighPerformance);
        when(vm.getMigrationSupport()).thenReturn(MigrationSupport.MIGRATABLE);

        setUpTscTest(underTest, vm, writer, properties);
        setTscFreqSupplier(underTest);
        setVmInfoBuildUtils(underTest);
        setCpuFlagsSupplier(underTest, "tsc,constant_tsc,nonstop_tsc");

        underTest.writeClock();
        verify(writer, times(4)).writeStartElement("timer");
        verify(writer, times(1)).writeAttributeString("name", "tsc");
        verify(writer, times(1)).writeAttributeString("frequency", "1234000");
    }

    private void setVmInfoBuildUtils(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException {
        Field vmInfoBuildUtils = LibvirtVmXmlBuilder.class.getDeclaredField("vmInfoBuildUtils");
        VmInfoBuildUtils buildUtils = mock(VmInfoBuildUtils.class);
        when(buildUtils.getVmTimeZone(any())).thenReturn(0);
        FieldSetter.setField(underTest, vmInfoBuildUtils, buildUtils);
    }

    private void setTscFreqSupplier(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException {
        Field tscFrequencySupplier = LibvirtVmXmlBuilder.class.getDeclaredField("tscFrequencySupplier");
        FieldSetter.setField(underTest, tscFrequencySupplier, new MemoizingSupplier<>(() -> "1234.567"));
    }

    private void setCpuFlagsSupplier(LibvirtVmXmlBuilder underTest, String flags) throws NoSuchFieldException {
        Field cpuFlagsSupplier = LibvirtVmXmlBuilder.class.getDeclaredField("cpuFlagsSupplier");
        FieldSetter.setField(underTest, cpuFlagsSupplier, new MemoizingSupplier<>(() -> flags));
    }

    private VM getVm(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException, IllegalAccessException {
        Field vmField = LibvirtVmXmlBuilder.class.getDeclaredField("vm");
        vmField.setAccessible(true);
        return (VM) vmField.get(underTest);
    }

    private void setMdevDisplayOn(LibvirtVmXmlBuilder underTest, boolean value) throws NoSuchFieldException {
        Field mdevDisplayOnField = LibvirtVmXmlBuilder.class.getDeclaredField("mdevDisplayOn");
        FieldSetter.setField(underTest, mdevDisplayOnField, value);
    }

    private void setUpMdevTest(LibvirtVmXmlBuilder underTest, XmlTextWriter writer, Map<String, String> properties) throws NoSuchFieldException {
        doCallRealMethod().when(underTest).writeVGpu();
        Map<String, Map<String, String>> metadata = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getCompatibilityVersion()).thenReturn(Version.v4_3);
        when(underTest.isMdevDisplayOn(any(), any())).thenCallRealMethod();
        setVm(underTest, vm);
        setProperties(underTest, properties);
        setMdevDisplayOn(underTest, underTest.isMdevDisplayOn(properties, vm));
        setWriter(underTest, writer);
        setMetadata(underTest, metadata);
    }

    private void setUpTscTest(LibvirtVmXmlBuilder underTest, VM vm, XmlTextWriter writer, Map<String, String> properties) throws NoSuchFieldException {
        doCallRealMethod().when(underTest).writeCpu(false);
        doCallRealMethod().when(underTest).writeClock();
        when(underTest.isTscFrequencyNeeded()).thenCallRealMethod();
        Map<String, Map<String, String>> metadata = new HashMap<>();
        when(vm.getClusterArch()).thenReturn(ArchitectureType.x86_64);

        setVm(underTest, vm);
        setProperties(underTest, properties);
        setWriter(underTest, writer);
        setMetadata(underTest, metadata);
    }

    private void setVm(LibvirtVmXmlBuilder underTest, VM vm) throws NoSuchFieldException {
        Field vmField = LibvirtVmXmlBuilder.class.getDeclaredField("vm");
        FieldSetter.setField(underTest, vmField, vm);
    }

    private void setMetadata(LibvirtVmXmlBuilder underTest, Map<String, Map<String, String>> metadata) throws NoSuchFieldException {
        Field metadataField = LibvirtVmXmlBuilder.class.getDeclaredField("mdevMetadata");
        FieldSetter.setField(underTest, metadataField, metadata);
    }

    private void setWriter(LibvirtVmXmlBuilder underTest, XmlTextWriter writer) throws NoSuchFieldException {
        Field writerField = LibvirtVmXmlBuilder.class.getDeclaredField("writer");
        FieldSetter.setField(underTest, writerField, writer);
    }

    private void setProperties(LibvirtVmXmlBuilder underTest, Map<String, String> properties) throws NoSuchFieldException {
        Field propField = LibvirtVmXmlBuilder.class.getDeclaredField("vmCustomProperties");
        FieldSetter.setField(underTest, propField, properties);
    }

    private void setVmInfoBuildUtils(LibvirtVmXmlBuilder underTest, VmInfoBuildUtils utils) throws NoSuchFieldException {
        Field vmInfoBuildUtilsField = LibvirtVmXmlBuilder.class.getDeclaredField("vmInfoBuildUtils");
        FieldSetter.setField(underTest, vmInfoBuildUtilsField, utils);
    }
}
