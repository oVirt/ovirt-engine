package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import java.util.Collections;
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
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.plugins.MemberAccessor;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MDevTypesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.ovf.xml.XmlTextWriter;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class LibvirtVmXmlBuilderTest {
    MockitoSession mockito;

    private final MemberAccessor accessor = Plugins.getMemberAccessor();

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
        return Stream.of(MockConfigDescriptor.of(ConfigValues.VgpuFramebufferSupported, Version.v4_5, Boolean.TRUE),
                MockConfigDescriptor.of(ConfigValues.VgpuFramebufferSupported, Version.v4_3, Boolean.FALSE),
                MockConfigDescriptor.of(ConfigValues.VgpuPlacementSupported, Version.v4_5, Boolean.FALSE),
                MockConfigDescriptor.of(ConfigValues.VgpuPlacementSupported, Version.v4_3, Boolean.FALSE),
                MockConfigDescriptor.of(ConfigValues.VgpuPlacementSupported, Version.v4_2, Boolean.FALSE));
    }

    @SuppressWarnings("unused")
    public static Stream<MockConfigDescriptor<?>> hotPlugCpuNotSupported() {
        Map<String, String> hotPlugCpuMap = new HashMap<>();
        hotPlugCpuMap.put("s390x", "false");
        hotPlugCpuMap.put("x86", "false");
        hotPlugCpuMap.put("ppc", "false");
        return Stream.of(MockConfigDescriptor.of(ConfigValues.HotPlugCpuSupported, Version.v4_5, hotPlugCpuMap),
                MockConfigDescriptor.of(ConfigValues.HotPlugCpuSupported, Version.v4_3, hotPlugCpuMap),
                MockConfigDescriptor.of(ConfigValues.HotPlugCpuSupported, Version.v4_2, hotPlugCpuMap));
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

        setUpMdevTest(underTest, writer, properties, Version.v4_3);
        VM vm = getVm(underTest);

        VmDevice device = mock(VmDevice.class);
        when(device.getDevice()).thenReturn("testDevice");
        when(vm.getCustomProperties()).thenReturn("mdev_type=nvidia28");
        setMdevDisplayOn(underTest, vm);

        underTest.writeVideo(device);
        verify(writer, times(1)).writeAttributeString("type", "none");

        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nodisplay,nvidia28");
        setMdevDisplayOn(underTest, vm);
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

        setUpMdevTest(underTest, writer, properties, Version.v4_3);
        VM vm = getVm(underTest);

        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // display="on" is the default
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nvidia28");
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(1)).writeAttributeString("display", "on");
        verify(writer, times(0)).writeAttributeString("ramfb", "on");

        // display="on" is inserted for each mdev
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nvidia28,nvidia10");
        underTest.writeVGpu();
        verify(writer, times(2)).writeAttributeString("display", "on");

        // nodisplay prevents adding display="on" in the xml
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nodisplay,nvidia28");
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");

        // nodisplay affects all mdevs
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nodisplay,nvidia10,nvidia28");
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");

        // nodisplay must be the first entry in the mdev_type list
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nvidia28,nodisplay");
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(2)).writeAttributeString("display", "on");

        // When there's only nodisplay in mdev list, no hostdev elements are added
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nodisplay");
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // Empty and null mdev_types produce no hostdev elements
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=");
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        reset(writer);
        when(vm.getCustomProperties()).thenReturn(null);
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // display="on" is not included in cluster version < 4.3
        reset(writer);
        when(vm.getCustomProperties()).thenReturn("mdev_type=nvidia28");
        VM vm2 = mock(VM.class);
        when(vm2.getCompatibilityVersion()).thenReturn(Version.v4_2);
        setVm(underTest, vm2);
        setMdevDisplayOn(underTest, vm2);
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");
    }

    @Test
    @MockedConfig("vgpuPlacementNotSupported")
    void testMdevRamfb() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();

        setUpMdevTest(underTest, writer, properties, Version.v4_5);
        VM vm = getVm(underTest);

        when(vm.getCustomProperties()).thenReturn("mdev_type=nvidia28");
        setMdevDisplayOn(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(1)).writeAttributeString("display", "on");
        verify(writer, times(1)).writeAttributeString("ramfb", "on");
    }

    @Test
    @MockedConfig("hotPlugCpuNotSupported")
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

    private void setupNoneVideoTest(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException, IllegalAccessException {
        doCallRealMethod().when(underTest).writeDevices();

        VmInfoBuildUtils utils = mock(VmInfoBuildUtils.class);
        setVmDevicesSupplier(underTest, new ArrayList<>());
        setVmInfoBuildUtils(underTest, utils);

        VM vm = mock(VM.class);
        when(vm.getId()).thenReturn(Guid.newGuid());
        when(vm.getClusterArch()).thenReturn(ArchitectureType.x86_64);
        when(vm.getCompatibilityVersion()).thenReturn(Version.v4_5);
        when(vm.getBiosType()).thenReturn(BiosType.I440FX_SEA_BIOS);
        when(vm.getBiosType()).thenReturn(BiosType.I440FX_SEA_BIOS);
        when(vm.getBootSequence()).thenReturn(BootSequence.C);
        setVm(underTest, vm);

        setVolumeLeases(underTest, new ArrayList<>());
    }

    private void setVolumeLeases(LibvirtVmXmlBuilder underTest, ArrayList<Object> volumeLeases) throws NoSuchFieldException, IllegalAccessException {
        Field volumeLeasesField = LibvirtVmXmlBuilder.class.getDeclaredField("volumeLeases");
        accessor.set(volumeLeasesField, underTest, volumeLeases);
    }

    @Test
    @MockedConfig("tscConfig")
    void testTscFrequencyCpu() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getUseTscFrequency()).thenReturn(true);

        setUpTscTest(underTest, vm, writer, properties);
        setTscFreqSupplier(underTest);
        setCpuFlagsSupplier(underTest, "tsc,constant_tsc,nonstop_tsc");
        setCpuModelSupplier(underTest);

        underTest.writeCpu(false);
        verify(writer, times(1)).writeStartElement("feature");
        verify(writer, times(1)).writeAttributeString("name", "invtsc");
        verify(writer, times(1)).writeAttributeString("policy", "require");
    }

    @Test
    @MockedConfig("tscConfig")
    void testTscFrequencyTimer() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getUseTscFrequency()).thenReturn(true);

        setUpTscTest(underTest, vm, writer, properties);
        setTscFreqSupplier(underTest);
        setVmInfoBuildUtils(underTest);
        setCpuFlagsSupplier(underTest, "tsc,constant_tsc,nonstop_tsc");
        setCpuModelSupplier(underTest);

        underTest.writeClock();
        verify(writer, times(4)).writeStartElement("timer");
        verify(writer, times(1)).writeAttributeString("name", "tsc");
        verify(writer, times(1)).writeAttributeString("frequency", "1234567980");
    }

    @Test
    @MockedConfig("hotPlugCpuNotSupported")
    void testHostdevScsiDisk() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();
        VmDevice device = mock(VmDevice.class);

        setUpHostdevScsiTest(underTest, writer, properties, device);

        properties.put("scsi_hostdev", "scsi_generic");
        underTest.writeDevices();
        verify(writer, times(0)).writeStartElement("disk");
        verify(writer, times(1)).writeStartElement("hostdev");

        properties.put("scsi_hostdev", "scsi_hd");
        reset(writer);
        underTest.writeDevices();
        verify(writer, times(1)).writeStartElement("disk");
        verify(writer, times(1)).writeAttributeString("type", "block");
        verify(writer, times(1)).writeAttributeString("device", "disk");
        verify(writer, times(1)).writeStartElement("blockio");
        verify(writer, times(2)).writeStartElement("target"); // 1 for qemu guest agent
        verify(writer, times(1)).writeAttributeString("bus", "scsi");
        verify(writer, times(1)).writeStartElement("address");
        verify(writer, times(1)).writeAttributeString("type", "drive");
        verify(writer, times(1)).writeAttributeString("bus", "1");
        verify(writer, times(1)).writeAttributeString("controller", "2");
        verify(writer, times(1)).writeAttributeString("unit", "3");

        properties.put("scsi_hostdev", "scsi_block");
        reset(writer);
        underTest.writeDevices();
        verify(writer, times(1)).writeStartElement("disk");
        verify(writer, times(1)).writeAttributeString("type", "block");
        verify(writer, times(1)).writeAttributeString("device", "lun");
        verify(writer, times(1)).writeAttributeString("rawio", "yes");
        verify(writer, times(2)).writeStartElement("target"); // 1 for qemu guest agent
        verify(writer, times(1)).writeAttributeString("bus", "scsi");
        verify(writer, times(1)).writeStartElement("address");
        verify(writer, times(1)).writeAttributeString("type", "drive");
        verify(writer, times(1)).writeAttributeString("bus", "1");
        verify(writer, times(1)).writeAttributeString("controller", "2");
        verify(writer, times(1)).writeAttributeString("unit", "3");

        properties.put("scsi_hostdev", "virtio_blk_pci");
        reset(writer);
        underTest.writeDevices();
        verify(writer, times(1)).writeStartElement("disk");
        verify(writer, times(1)).writeAttributeString("type", "block");
        verify(writer, times(1)).writeAttributeString("device", "disk");
        verify(writer, times(2)).writeStartElement("target"); // 1 for qemu guest agent
        verify(writer, times(1)).writeAttributeString("bus", "virtio");
        verify(writer, times(0)).writeStartElement("address");

        when(device.getAddress()).thenReturn("{type=pci, bus=1, domain=2, slot=3, function=4}");
        reset(writer);
        underTest.writeDevices();
        verify(writer, times(1)).writeStartElement("disk");
        verify(writer, times(1)).writeAttributeString("type", "block");
        verify(writer, times(1)).writeAttributeString("device", "disk");
        verify(writer, times(2)).writeStartElement("target"); // 1 for qemu guest agent
        verify(writer, times(1)).writeAttributeString("bus", "virtio");
        verify(writer, times(1)).writeStartElement("address");
        verify(writer, times(1)).writeAttributeString("type", "pci");
        verify(writer, times(1)).writeAttributeString("bus", "1");
        verify(writer, times(1)).writeAttributeString("domain", "2");
        verify(writer, times(1)).writeAttributeString("slot", "3");
        verify(writer, times(1)).writeAttributeString("function", "4");
    }

    @Test
    @MockedConfig("hotPlugCpuNotSupported")
    void testControllerVirioScsiQueues() throws NoSuchFieldException, IllegalAccessException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();
        VmDevice device = mock(VmDevice.class);
        VmInfoBuildUtils buildUtils = setVmInfoBuildUtils(underTest);

        setupControllerVirtioScsiQueuesTest(underTest, writer, properties, device, buildUtils);
        VM vm = getVm(underTest);
        when(vm.getVirtioScsiMultiQueues()).thenReturn(5);
        underTest.writeDevices();
        verify(writer, times(1)).writeStartElement("driver");
        verify(writer, times(1)).writeAttributeString("queues", "5");

        reset(writer);
        when(vm.getVirtioScsiMultiQueues()).thenReturn(0);

        underTest.writeDevices();
        verify(writer, times(0)).writeStartElement("driver");

        reset(writer);
        when(vm.getVirtioScsiMultiQueues()).thenReturn(-1);
        when(buildUtils.getNumOfScsiQueues(0, 0)).thenReturn(33);
        underTest.writeDevices();
        verify(writer, times(1)).writeStartElement("driver");
        verify(writer, times(1)).writeAttributeString("queues", "33");
    }

    private VmInfoBuildUtils setVmInfoBuildUtils(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException, IllegalAccessException {
        Field vmInfoBuildUtils = LibvirtVmXmlBuilder.class.getDeclaredField("vmInfoBuildUtils");
        VmInfoBuildUtils buildUtils = mock(VmInfoBuildUtils.class);
        when(buildUtils.getVmTimeZone(any())).thenReturn(0);
        accessor.set(vmInfoBuildUtils, underTest, buildUtils);

        return buildUtils;
    }

    private MemoizingSupplier setHostDeviceSupplier(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException, IllegalAccessException {
        Field hostDevicesSupplierUtils = LibvirtVmXmlBuilder.class.getDeclaredField("hostDevicesSupplier");
        MemoizingSupplier supplier = mock(MemoizingSupplier.class);
        accessor.set(hostDevicesSupplierUtils, underTest, supplier);

        return supplier;
    }

    private void setVmDevicesSupplier(LibvirtVmXmlBuilder underTest, List<VmDevice> vmDevices) throws NoSuchFieldException, IllegalAccessException {
        Field vmDevicesSupplier = LibvirtVmXmlBuilder.class.getDeclaredField("vmDevicesSupplier");
        accessor.set(vmDevicesSupplier, underTest, new MemoizingSupplier<>(() -> vmDevices));
    }

    private void setTscFreqSupplier(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException, IllegalAccessException {
        Field tscFrequencySupplier = LibvirtVmXmlBuilder.class.getDeclaredField("tscFrequencySupplier");
        accessor.set(tscFrequencySupplier, underTest, new MemoizingSupplier<>(() -> "1234567980"));
    }

    private void setCpuFlagsSupplier(LibvirtVmXmlBuilder underTest, String flags) throws NoSuchFieldException, IllegalAccessException {
        Field cpuFlagsSupplier = LibvirtVmXmlBuilder.class.getDeclaredField("cpuFlagsSupplier");
        accessor.set(cpuFlagsSupplier, underTest, new MemoizingSupplier<>(() -> flags));
    }

    private void setCpuModelSupplier(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException, IllegalAccessException {
        Field cpuModelSupplier = LibvirtVmXmlBuilder.class.getDeclaredField("cpuModelSupplier");
        accessor.set(cpuModelSupplier, underTest, new MemoizingSupplier<>(() -> "unknown"));
    }

    private VM getVm(LibvirtVmXmlBuilder underTest) throws NoSuchFieldException, IllegalAccessException {
        Field vmField = LibvirtVmXmlBuilder.class.getDeclaredField("vm");
        vmField.setAccessible(true);
        return (VM) vmField.get(underTest);
    }

    private void setMdevDisplayOn(LibvirtVmXmlBuilder underTest, VM vm) throws NoSuchFieldException, IllegalAccessException {
        Field mdevDisplayOnField = LibvirtVmXmlBuilder.class.getDeclaredField("mdevDisplayOn");
        accessor.set(mdevDisplayOnField, underTest, MDevTypesUtils.isMdevDisplayOn(vm));
    }

    private void setUpMdevTest(LibvirtVmXmlBuilder underTest, XmlTextWriter writer, Map<String, String> properties, Version compatibilityVersion) throws NoSuchFieldException, IllegalAccessException {
        doCallRealMethod().when(underTest).writeVGpu();
        Map<String, Map<String, String>> metadata = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getCompatibilityVersion()).thenReturn(compatibilityVersion);
        setVm(underTest, vm);
        setProperties(underTest, properties);
        setMdevDisplayOn(underTest, vm);
        setWriter(underTest, writer);
        setMetadata(underTest, metadata);
    }

    private void setUpTscTest(LibvirtVmXmlBuilder underTest, VM vm, XmlTextWriter writer, Map<String, String> properties) throws NoSuchFieldException, IllegalAccessException {
        doCallRealMethod().when(underTest).writeCpu(false);
        doCallRealMethod().when(underTest).writeClock();
        Map<String, Map<String, String>> metadata = new HashMap<>();
        when(vm.getClusterArch()).thenReturn(ArchitectureType.x86_64);

        setVm(underTest, vm);
        setProperties(underTest, properties);
        setWriter(underTest, writer);
        setMetadata(underTest, metadata);
    }

    private void setUpHostdevScsiTest(LibvirtVmXmlBuilder underTest, XmlTextWriter writer, Map<String, String> properties, VmDevice device) throws NoSuchFieldException, IllegalAccessException {
        doCallRealMethod().when(underTest).writeDevices();
        Map<String, Map<String, String>> metadata = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getClusterArch()).thenReturn(ArchitectureType.x86_64);
        when(vm.getCompatibilityVersion()).thenReturn(Version.v4_5);
        when(vm.getBiosType()).thenReturn(BiosType.I440FX_SEA_BIOS);
        when(vm.getBiosType()).thenReturn(BiosType.I440FX_SEA_BIOS);
        when(vm.getBootSequence()).thenReturn(BootSequence.C);

        VmInfoBuildUtils buildUtils = setVmInfoBuildUtils(underTest);
        MemoizingSupplier hostDeviceSupplier = setHostDeviceSupplier(underTest);
        when(device.getType()).thenReturn(VmDeviceGeneralType.HOSTDEV);
        when(device.isPlugged()).thenReturn(true);
        when(device.getDevice()).thenReturn("testScsi");
        when(device.getId()).thenReturn(new VmDeviceId(Guid.newGuid(), Guid.newGuid()));
        when(buildUtils.makeDiskName(any(), anyInt())).thenCallRealMethod();
        when(buildUtils.diskInterfaceToDevName(any())).thenCallRealMethod();

        Map<String, String> hostAddress = new HashMap<>();
        hostAddress.put("bus", "1");
        hostAddress.put("host", "2");
        hostAddress.put("lun", "3");
        HostDevice hostDev = mock(HostDevice.class);
        when(hostDev.getCapability()).thenReturn("scsi");
        when(hostDev.getAddress()).thenReturn(hostAddress);
        Map<String, HostDevice> devMap = new HashMap<>();
        devMap.put("testScsi", hostDev);
        when(hostDeviceSupplier.get()).thenReturn(devMap);

        setVm(underTest, vm);
        setProperties(underTest, properties);
        setInterface(underTest, "sd");
        setWriter(underTest, writer);
        setMetadata(underTest, metadata);
        setVolumeLeases(underTest, new ArrayList<>());
        setVmDevicesSupplier(underTest, Collections.singletonList(device));
    }

    private void setupControllerVirtioScsiQueuesTest(LibvirtVmXmlBuilder underTest,
            XmlTextWriter writer,
            Map<String, String> properties,
            VmDevice device,
            VmInfoBuildUtils buildUtils) throws NoSuchFieldException, IllegalAccessException {
        doCallRealMethod().when(underTest).writeDevices();
        Map<String, Map<String, String>> metadata = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getClusterArch()).thenReturn(ArchitectureType.x86_64);
        when(vm.getBiosType()).thenReturn(BiosType.I440FX_SEA_BIOS);
        when(vm.getBootSequence()).thenReturn(BootSequence.C);
        when(vm.getCompatibilityVersion()).thenReturn(Version.v4_5);
        when(device.isPlugged()).thenReturn(true);
        when(device.getType()).thenReturn(VmDeviceGeneralType.CONTROLLER);
        when(device.getDevice()).thenReturn("testScsi");
        Map<String, Object> m = new HashMap<>();
        m.put(VdsProperties.Model, "virtio-scsi");
        when(device.getSpecParams().get(VdsProperties.Model)).thenReturn(m);

        setVm(underTest, vm);
        setProperties(underTest, properties);
        setInterface(underTest, "sd");
        setWriter(underTest, writer);
        setMetadata(underTest, metadata);
        setVolumeLeases(underTest, new ArrayList<>());
        setVmDevicesSupplier(underTest, Collections.singletonList(device));
    }

    private void setVm(LibvirtVmXmlBuilder underTest, VM vm) throws NoSuchFieldException, IllegalAccessException {
        Field vmField = LibvirtVmXmlBuilder.class.getDeclaredField("vm");
        accessor.set(vmField, underTest, vm);
    }

    private void setMetadata(LibvirtVmXmlBuilder underTest, Map<String, Map<String, String>> metadata) throws NoSuchFieldException, IllegalAccessException {
        Field metadataField = LibvirtVmXmlBuilder.class.getDeclaredField("mdevMetadata");
        accessor.set(metadataField, underTest, metadata);
    }

    private void setWriter(LibvirtVmXmlBuilder underTest, XmlTextWriter writer) throws NoSuchFieldException, IllegalAccessException {
        Field writerField = LibvirtVmXmlBuilder.class.getDeclaredField("writer");
        accessor.set(writerField, underTest, writer);
    }

    private void setProperties(LibvirtVmXmlBuilder underTest, Map<String, String> properties) throws NoSuchFieldException, IllegalAccessException {
        Field propField = LibvirtVmXmlBuilder.class.getDeclaredField("vmCustomProperties");
        accessor.set(propField, underTest, properties);
    }

    private void setVmInfoBuildUtils(LibvirtVmXmlBuilder underTest, VmInfoBuildUtils utils) throws NoSuchFieldException, IllegalAccessException {
        Field vmInfoBuildUtilsField = LibvirtVmXmlBuilder.class.getDeclaredField("vmInfoBuildUtils");
        accessor.set(vmInfoBuildUtilsField, underTest, utils);
    }

    private void setInterface(LibvirtVmXmlBuilder underTest, String cdInterface) throws NoSuchFieldException, IllegalAccessException {
        Field cdInterfaceField = LibvirtVmXmlBuilder.class.getDeclaredField("cdInterface");
        accessor.set(cdInterfaceField, underTest, cdInterface);
    }
}
