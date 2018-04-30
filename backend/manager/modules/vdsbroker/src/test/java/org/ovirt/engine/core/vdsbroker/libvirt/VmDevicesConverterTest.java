package org.ovirt.engine.core.vdsbroker.libvirt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class VmDevicesConverterTest {

    private VmDevicesConverter converter = new VmDevicesConverter();

    @Test
    public void parseVolumeChainNoItem() throws Exception {
        XmlDocument devices = new XmlDocument(DEVICES_XML);
        List<Map<String, Object>> res = converter.parseVolumeChain(devices.selectSingleNode("//*/disk[1]"));
        assertEquals(0, res.size());
    }

    @Test
    public void parseVolumeChainMoreItems() throws Exception {
        XmlDocument devices = new XmlDocument(DEVICES_XML);
        List<Map<String, Object>> res = converter.parseVolumeChain(devices.selectSingleNode("//*/disk[2]"));
        assertEquals(3, res.size());
        assertEquals("cc5beaf2-3265-4b36-b7be-4f23dc3b07af", res.get(0).get(VdsProperties.VolumeId));
        assertEquals("fff3c995-e7c3-4a2c-96a3-f0ce569a57c2", res.get(1).get(VdsProperties.VolumeId));
        assertEquals("011046ce-312a-42e6-bcb7-764fd332da02", res.get(2).get(VdsProperties.VolumeId));
    }

    @Test
    public void parseVolumeIdFromPath() {
        assertEquals("ccc", converter.parseVolumeIdFromPath("/a/b/ccc"));
        assertEquals("", converter.parseVolumeIdFromPath(""));
        assertEquals("", converter.parseVolumeIdFromPath("not a path"));
    }

    @Test
    public void parseImageIdFromPath() {
        assertEquals("b", converter.parseImageIdFromPath("/a/b/ccc"));
        assertEquals("", converter.parseImageIdFromPath(""));
        assertEquals("", converter.parseImageIdFromPath("not a path"));
    }

    private static String DEVICES_XML = "  <devices>\n" +
            "    <emulator>/usr/libexec/qemu-kvm</emulator>\n" +
            "    <disk type='file' device='cdrom'>\n" +
            "      <driver name='qemu' type='raw'/>\n" +
            "      <source startupPolicy='optional'/>\n" +
            "      <backingStore/>\n" +
            "      <target dev='hdc' bus='ide'/>\n" +
            "      <readonly/>\n" +
            "      <alias name='ide0-1-0'/>\n" +
            "      <address type='drive' controller='0' bus='1' target='0' unit='0'/>\n" +
            "    </disk>\n" +
            "    <disk type='file' device='disk' snapshot='no'>\n" +
            "      <driver name='qemu' type='qcow2' cache='none' error_policy='stop' io='threads'/>\n" +
            "      <source file='/rhev/data-center/mnt/192.168.122.1:_home_exports_data/a568e7d5-3938-4229-9d5c-1e022ba92a80/images/b75eef2b-c96c-4ac9-a720-90d3f8235249/011046ce-312a-42e6-bcb7-764fd332da02'/>\n" +
            "      <backingStore type='file' index='1'>\n" +
            "        <format type='qcow2'/>\n" +
            "        <source file='/rhev/data-center/mnt/192.168.122.1:_home_exports_data/a568e7d5-3938-4229-9d5c-1e022ba92a80/images/b75eef2b-c96c-4ac9-a720-90d3f8235249/fff3c995-e7c3-4a2c-96a3-f0ce569a57c2'/>\n" +
            "        <backingStore type='file' index='2'>\n" +
            "          <format type='raw'/>\n" +
            "          <source file='/rhev/data-center/mnt/192.168.122.1:_home_exports_data/a568e7d5-3938-4229-9d5c-1e022ba92a80/images/b75eef2b-c96c-4ac9-a720-90d3f8235249/cc5beaf2-3265-4b36-b7be-4f23dc3b07af'/>\n" +
            "          <backingStore/>\n" +
            "        </backingStore>\n" +
            "      </backingStore>\n" +
            "      <target dev='sda' bus='scsi'/>\n" +
            "      <serial>b75eef2b-c96c-4ac9-a720-90d3f8235249</serial>\n" +
            "      <boot order='1'/>\n" +
            "      <alias name='scsi0-0-0-0'/>\n" +
            "      <address type='drive' controller='0' bus='0' target='0' unit='0'/>\n" +
            "    </disk>\n" +
            "    <controller type='scsi' index='0' model='virtio-scsi'>\n" +
            "      <alias name='scsi0'/>\n" +
            "      <address type='pci' domain='0x0000' bus='0x00' slot='0x03' function='0x0'/>\n" +
            "    </controller>\n" +
            "</devices>";

}
