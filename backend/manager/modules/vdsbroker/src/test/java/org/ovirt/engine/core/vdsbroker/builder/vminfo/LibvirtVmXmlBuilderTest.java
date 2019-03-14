package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder.adjustSpiceSecureChannels;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldSetter;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;
import org.ovirt.engine.core.utils.ovf.xml.XmlTextWriter;

public class LibvirtVmXmlBuilderTest {

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

    @Test
    @MockedConfig("vgpuPlacementNotSupported")
    void testMdevNodisplay() throws NoSuchFieldException {
        LibvirtVmXmlBuilder underTest = mock(LibvirtVmXmlBuilder.class);
        XmlTextWriter writer = mock(XmlTextWriter.class);
        Map<String, String> properties = new HashMap<>();

        setUpMdevTest(underTest, writer, properties);

        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // display="on" is the default
        reset(writer);
        properties.put("mdev_type", "nvidia28");
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
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");

        // nodisplay affects all mdevs
        reset(writer);
        properties.put("mdev_type", "nodisplay,nvidia10,nvidia28");
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");

        // nodisplay must be the first entry in the mdev_type list
        reset(writer);
        properties.put("mdev_type", "nvidia28,nodisplay");
        underTest.writeVGpu();
        verify(writer, times(2)).writeAttributeString("display", "on");

        // When there's only nodisplay in mdev list, no hostdev elements are added
        reset(writer);
        properties.put("mdev_type", "nodisplay");
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // Empty and null mdev_types produce no hostdev elements
        reset(writer);
        properties.put("mdev_type", "");
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        reset(writer);
        properties.put("mdev_type", null);
        underTest.writeVGpu();
        verify(writer, times(0)).writeStartElement("hostdev");

        // display="on" is not included in cluster version < 4.3
        reset(writer);
        properties.put("mdev_type", "nvidia28");
        VM vm = mock(VM.class);
        when(vm.getCompatibilityVersion()).thenReturn(Version.v4_2);
        setVm(underTest, vm);
        underTest.writeVGpu();
        verify(writer, times(0)).writeAttributeString("display", "on");
    }

    private void setUpMdevTest(LibvirtVmXmlBuilder underTest, XmlTextWriter writer, Map<String, String> properties) throws NoSuchFieldException {
        doCallRealMethod().when(underTest).writeVGpu();
        Map<String, Map<String, String>> metadata = new HashMap<>();
        VM vm = mock(VM.class);
        when(vm.getCompatibilityVersion()).thenReturn(Version.v4_3);
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
}
