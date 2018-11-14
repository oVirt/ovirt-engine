package org.ovirt.engine.core.vdsbroker.libvirt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmConverter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public Map<String, Object> convert(Guid vmId, String xml) throws Exception {
        Map<String, Object> result = new HashMap<>();
        XmlDocument domainxml = new XmlDocument(xml);
        XmlNode domain = domainxml.selectSingleNode("domain");
        result.putAll(extractCoreGeneralInfo(domain));
        result.putAll(extractCoreDevices(domain));
        result.putAll(retrieveDefaultDisplayType((Map<String, Object>[]) result.get(VdsProperties.Devices)));
        return result;
    }

    private Map<String, Object> extractCoreGeneralInfo(XmlNode domain) {
        Map<String, Object> info = new HashMap<>();
        info.put(VdsProperties.vm_name, domain.selectSingleNode("name").innerText);
        info.put(VdsProperties.vm_guid, domain.selectSingleNode("uuid").innerText);
        XmlNode cpusTopology = domain.selectSingleNode("cpu").selectSingleNode("topology");
        if (cpusTopology != null) {
            info.put(VdsProperties.num_of_cpus, cpusTopology.attributes.get("sockets").innerText);
        } else { // fallback when no topology is specified (ignoring offline CPUs)
            info.put(VdsProperties.num_of_cpus, domain.selectSingleNode("vcpu").innerText);
        }
        info.put(VdsProperties.mem_size_mb, DomainXmlUtils.parseMemSize(domain.selectSingleNode("memory")));
        info.putAll(DomainXmlUtils.parseMaxMemSize(domain.selectSingleNode("maxMemory")));
        info.put(VdsProperties.emulatedMachine, DomainXmlUtils.parseEmulatedMachine(domain.selectSingleNode("os")));
        return info;
    }

    private Map<String, Object> extractCoreDevices(XmlNode domain) {
        List<Map<String, Object>> result = new ArrayList<>();
        XmlNode devicesNode = domain.selectSingleNode("devices");
        devicesNode.selectNodes("console").forEach(dev -> result.add(parseConsole(dev)));
        devicesNode.selectNodes("video").forEach(dev -> result.add(parseVideo(dev)));
        devicesNode.selectNodes("graphics").forEach(dev -> result.add(parseGraphics(dev)));
        devicesNode.selectNodes("interface").forEach(dev -> result.add(parseInterface(dev)));
        devicesNode.selectNodes("disk").forEach(dev -> result.add(parseDisk(dev)));
        return Collections.singletonMap(
                VdsProperties.Devices,
                result.stream().filter(Objects::nonNull).toArray(Map[]::new));
    }

    private Map<String, Object> retrieveDefaultDisplayType(Map<String, Object>[] devices) {
        for (Map<String, Object> device : devices) {
            if (device.get(VdsProperties.Type).equals(VmDeviceGeneralType.VIDEO.getValue())) {
                return Collections.singletonMap(VdsProperties.displayType, (String) device.get(VdsProperties.Device));
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, Object> parseConsole(XmlNode dev) {
        Map<String, Object> device = new HashMap<>();
        device.put(VdsProperties.Device, VmDeviceType.CONSOLE.getName());
        device.put(VdsProperties.Type, VmDeviceGeneralType.CONSOLE.getValue());
        device.put(VdsProperties.DeviceId, Guid.newGuid().toString());
        device.put(VdsProperties.Alias, VmDevicesConverter.parseAlias(dev));
        return device;
    }

    private Map<String, Object> parseGraphics(XmlNode dev) {
        Map<String, Object> device = new HashMap<>();
        device.put(VdsProperties.Device, DomainXmlUtils.parseAttribute(dev, "type"));
        device.put(VdsProperties.Type, VmDeviceGeneralType.GRAPHICS.getValue());
        device.put(VdsProperties.DeviceId, Guid.newGuid().toString());
        device.put(VdsProperties.Alias, VmDevicesConverter.parseAlias(dev));
        return device;
    }

    private Map<String, Object> parseVideo(XmlNode dev) {
        Map<String, Object> device = new HashMap<>();
        device.put(VdsProperties.Device, DomainXmlUtils.parseVideoType(dev));
        device.put(VdsProperties.Type, VmDeviceGeneralType.VIDEO.getValue());
        device.put(VdsProperties.DeviceId, Guid.newGuid().toString());
        device.put(VdsProperties.Alias, VmDevicesConverter.parseAlias(dev));
        return device;
    }

    private Map<String, Object> parseInterface(XmlNode dev) {
        switch(DomainXmlUtils.parseAttribute(dev, "type")) {
        case "bridge":
            Map<String, Object> device = new HashMap<>();
            device.put(VdsProperties.Device, DomainXmlUtils.parseAttribute(dev, "type"));
            device.put(VdsProperties.Type, VmDeviceGeneralType.INTERFACE.getValue());
            device.put(VdsProperties.DeviceId, Guid.newGuid().toString());
            device.put(VdsProperties.Alias, VmDevicesConverter.parseAlias(dev));
            device.put(VdsProperties.Name, VmDevicesConverter.parseAlias(dev));
            device.put(VdsProperties.MAC_ADDR, DomainXmlUtils.parseMacAddress(dev));
            device.put(VdsProperties.NETWORK, DomainXmlUtils.parseNicNetwork(dev));
            device.put(VdsProperties.NIC_TYPE, DomainXmlUtils.parseNicType(dev));
            return device;
        default:
            return null;
        }
    }

    private Map<String, Object> parseDisk(XmlNode dev) {
        switch(DomainXmlUtils.parseAttribute(dev, "device")) {
        case "disk":
            Map<String, String> uuids = parseDiskUuids(DomainXmlUtils.parseDiskPath(dev));
            if (uuids == null) {
                return null;
            }

            Map<String, Object> device = new HashMap<>();
            device.put(VdsProperties.Device, VdsProperties.Disk);
            device.put(VdsProperties.Alias, VmDevicesConverter.parseAlias(dev));
            device.put(VdsProperties.Format,
                    "raw".equals(DomainXmlUtils.parseDiskDriver(dev)) ? "raw" : "cow");
            device.put(VdsProperties.INTERFACE, DomainXmlUtils.parseDiskBus(dev));
            device.putAll(uuids);
            return device;
        default:
            return null;
        }
    }

    public Map<String, String> parseDiskUuids(String cdPath) {
        Matcher m = Pattern.compile(ValidationUtils.GUID).matcher(cdPath);
        if (!m.find()) {
            return null;
        }
        Guid domainId = Guid.createGuidFromString(m.group());
        if (!m.find()) {
            return null;
        }
        Guid imageId = Guid.createGuidFromString(m.group());
        if (!m.find()) {
            return null;
        }
        Guid volumeId = Guid.createGuidFromString(m.group());
        Map<String, String> uuids = new HashMap<>();
        uuids.put(VdsProperties.DomainId, domainId.toString());
        uuids.put(VdsProperties.ImageId, imageId.toString());
        uuids.put(VdsProperties.VolumeId, volumeId.toString());
        return uuids;
    }
}
