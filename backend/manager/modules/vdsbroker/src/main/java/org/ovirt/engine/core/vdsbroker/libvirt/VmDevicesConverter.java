package org.ovirt.engine.core.vdsbroker.libvirt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.ovf.xml.XmlAttribute;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmDevicesConverter {

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private HostDeviceDao hostDeviceDao;
    @Inject
    private VmNetworkInterfaceDao vmNetworkInterfaceDao;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String TYPE = "type";
    private static final String MODEL = "model";
    private static final String INDEX = "index";
    private static final String DEVICE = "device";
    private static final String NODE = "node";
    private static final String SIZE = "size";
    private static final String DEVICES_START_ELEMENT = "<devices>";
    private static final String DEVICES_END_ELEMENT = "</devices>";

    public Map<String, Object> convert(Guid vmId, Guid hostId, String xml) throws Exception {
        String devicesXml = xml.substring(
                xml.indexOf(DEVICES_START_ELEMENT),
                xml.indexOf(DEVICES_END_ELEMENT) + DEVICES_END_ELEMENT.length());
        XmlDocument document = new XmlDocument(devicesXml);
        Map<String, Object> result = new HashMap<>();
        result.put(VdsProperties.vm_guid, vmId.toString());
        result.put(VdsProperties.Devices, parseDevices(vmId, hostId, document));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object>[] parseDevices(Guid vmId, Guid hostId, XmlDocument document) throws Exception {
        List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmId);

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(parseBalloon(document, devices)); // memballoon
        result.add(parseDev(VmDeviceGeneralType.RNG, document, devices));
        result.add(parseDev(VmDeviceGeneralType.WATCHDOG, document, devices));
        result.add(parseDev(VmDeviceGeneralType.SMARTCARD, document, devices));
        result.add(parseDev(VmDeviceGeneralType.SOUND, document, devices));
        result.add(parseDev(VmDeviceGeneralType.CONSOLE, document, devices));
        result.addAll(parseChannels(document, devices));
        result.addAll(parseControllers(document, devices));
        result.addAll(parseVideos(document, devices));
        result.addAll(parseInterfaces(document, devices, vmId));
        result.addAll(parseDisks(document, devices, vmId));
        result.addAll(parseRedirs(document, devices));
        result.addAll(parseMemories(document, devices));
        result.addAll(parseHostDevices(document, devices, hostId));
        return result.stream()
                .filter(map -> !map.isEmpty())
                .toArray(Map[]::new);
    }

    private List<Map<String, Object>> parseChannels(XmlDocument document, List<VmDevice> devices) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.CHANNEL);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.CHANNEL)) {
            String address = parseAddress(node);
            // Ignore channel devices without address
            if (address.isEmpty()) {
                continue;
            }

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.CHANNEL.getValue());
            dev.put(VdsProperties.Device, parseAttribute(node, TYPE)); // shouldn't it be VdsProperties.DeviceType?
            dev.put(VdsProperties.Address, address);
            dev.put(VdsProperties.Alias, parseAlias(node));

            VmDevice dbDev = dbDevices.stream()
                    .filter(d -> d.getDevice().equals(dev.get(VdsProperties.Device)))
                    .findFirst()
                    .orElse(null);

            if (dbDev != null) {
                dbDevices.remove(dbDev);
                dev.put(VdsProperties.DeviceId, dbDev.getId().getDeviceId().toString());
                dev.put(VdsProperties.SpecParams, dbDev.getSpecParams());
            } else {
                dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());
            }

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseControllers(XmlDocument document, List<VmDevice> devices) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.CONTROLLER);

        // devices with spec params to appear first
        dbDevices.sort((d1, d2) -> d1.getSpecParams().isEmpty() && !d2.getSpecParams().isEmpty() ? 1 : 0);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.CONTROLLER)) {
            String address = parseAddress(node);
            // Ignore controller devices without address
            if (address.isEmpty()) {
                continue;
            }

            String index = parseAttribute(node, INDEX);
            String model = parseAttribute(node, MODEL);

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.CONTROLLER.getValue());
            String devType = "virtio-scsi".equals(model) ? model : parseAttribute(node, TYPE);
            dev.put(VdsProperties.Device, devType);
            dev.put(VdsProperties.Address, address);
            dev.put(VdsProperties.Alias, parseAlias(node));

            VmDevice dbDev = dbDevices.stream()
                    .filter(d -> d.getDevice().equals(dev.get(VdsProperties.Device)))
                    .filter(d -> d.getSpecParams().isEmpty() || Objects.equals(d.getSpecParams().get(INDEX), index))
                    .filter(d -> d.getSpecParams().isEmpty() || Objects.equals(d.getSpecParams().get(MODEL), model))
                    .findFirst()
                    .orElse(null);

            if (dbDev != null) {
                dbDevices.remove(dbDev);
                dev.put(VdsProperties.DeviceId, dbDev.getId().getDeviceId().toString());
                dev.put(VdsProperties.SpecParams, dbDev.getSpecParams());
            } else {
                dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());
                Map<String, Object> specParams = new HashMap<>();
                if (index != null) {
                    specParams.put(INDEX, index);
                }
                if (model != null) {
                    specParams.put(MODEL, model);
                }
                dev.put(VdsProperties.SpecParams, specParams);
            }

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseMemories(XmlDocument document, List<VmDevice> devices) {
        List<VmDevice> dbDevices= filterDevices(devices, VmDeviceGeneralType.MEMORY);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.MEMORY)) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.MEMORY.getValue());
            dev.put(VdsProperties.Device, VmDeviceGeneralType.MEMORY.getValue());
            dev.put(VdsProperties.Address, parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            XmlNode target = node.selectSingleNode("target");
            if (target == null) {
                continue;
            }

            String devNode = target.selectSingleNode(NODE).innerText;
            String devSize = target.selectSingleNode(SIZE).innerText;
            VmDevice dbDev = dbDevices.stream()
                    .sorted((d1, d2) -> d1.isManaged() ? -1 : 1) // try to match managed devices first
                    .filter(d -> d.getDevice().equals(dev.get(VdsProperties.Device)))
                    .filter(d -> Objects.equals(d.getSpecParams().get(NODE).toString(), devNode))
                    .filter(d -> Objects.equals(d.getSpecParams().get(SIZE).toString(), devSize))
                    .findFirst()
                    .orElse(null);

            if (dbDev != null) {
                dbDevices.remove(dbDev);
                dev.put(VdsProperties.DeviceId, dbDev.getId().getDeviceId().toString());
                dev.put(VdsProperties.SpecParams, dbDev.getSpecParams());
            } else {
                dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());
                Map<String, Object> specParams = new HashMap<>();
                specParams.put(NODE, devNode);
                specParams.put(SIZE, devSize);
                dev.put(VdsProperties.SpecParams, specParams);
            }

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseHostDevices(XmlDocument document, List<VmDevice> devices, Guid hostId) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.HOSTDEV);
        if (dbDevices.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Map<String, String>, HostDevice> addressToHostDevice = hostDeviceDao.getHostDevicesByHostId(hostId)
                .stream()
                .filter(dev -> !dev.getAddress().isEmpty())
                .collect(Collectors.toMap(HostDevice::getAddress, device -> device));

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : document.selectNodes("//*/hostdev")) {
            Map<String, String> hostAddress = parseHostAddress(node);
            if (hostAddress == null) {
                continue;
            }
            HostDevice hostDevice = addressToHostDevice.get(hostAddress);

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.HOSTDEV.getValue());
            dev.put(VdsProperties.Address, parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));
            dev.put(VdsProperties.Device, hostDevice.getDeviceName());

            VmDevice dbDev = dbDevices.stream()
                    .filter(d -> d.getDevice().equals(hostDevice.getDeviceName()))
                    .findFirst()
                    .orElse(null);

            if (dbDev == null) {
                log.warn("VM host device '{}' does not exist in the database, thus ignored",
                        hostDevice.getDeviceName());
                continue;
            }

            dev.put(VdsProperties.DeviceId, dbDev.getId().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.getSpecParams());

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseRedirs(XmlDocument document, List<VmDevice> devices) {
        List<VmDevice> dbDevices= filterDevices(devices, VmDeviceGeneralType.REDIR);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : document.selectNodes("//*/redirdev")) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.REDIR.getValue());
            dev.put(VdsProperties.Device, parseAttribute(node, TYPE));
            dev.put(VdsProperties.Address, parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            VmDevice dbDev = dbDevices.stream()
                    .filter(d -> d.getDevice().equals(dev.get(VdsProperties.Device)))
                    .findFirst()
                    .orElse(null);

            if (dbDev != null) {
                dbDevices.remove(dbDev);
                dev.put(VdsProperties.DeviceId, dbDev.getId().getDeviceId().toString());
                dev.put(VdsProperties.SpecParams, dbDev.getSpecParams());
            } else {
                dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());
            }

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseDisks(XmlDocument document, List<VmDevice> devices, Guid vmId) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.DISK);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.DISK)) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.DISK.getValue());
            dev.put(VdsProperties.Device, parseAttribute(node, DEVICE));
            dev.put(VdsProperties.Address, parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            String path = parseDiskPath(node);
            VmDevice dbDev = dbDevices.stream()
                    .filter(d -> d.getDevice().equals(dev.get(VdsProperties.Device)))
                    .filter(d -> path.isEmpty() || path.contains(d.getId().getDeviceId().toString()))
                    .findFirst()
                    .orElse(null);

            if (dbDev == null) {
                log.warn("unmanaged disk with path '{}' is ignored", path);
                continue;
            }

            dbDevices.remove(dbDev);

            dev.put(VdsProperties.DeviceId, dbDev.getId().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.getSpecParams());
            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseInterfaces(XmlDocument document, List<VmDevice> devices, Guid vmId) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.INTERFACE);
        Map<Guid, VmDevice> devIdToDbDev = dbDevices.stream().collect(Collectors.toMap(
                device -> device.getId().getDeviceId(),
                device -> device));

        List<VmNetworkInterface> dbInterfaces = vmNetworkInterfaceDao.getAllForVm(vmId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.INTERFACE)) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.INTERFACE.getValue());
            dev.put(VdsProperties.Device, parseAttribute(node, TYPE));
            dev.put(VdsProperties.Address, parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            String macAddress = parseMacAddress(node);
            // MAC address is a unique identifier of network interface devices
            VmNetworkInterface dbInterface = dbInterfaces.stream()
                    .filter(iface -> iface.getMacAddress().equals(macAddress))
                    .findFirst()
                    .orElse(null);

            if (dbInterface == null) {
                log.warn("unmanaged network interface with mac address '{}' is ignored", macAddress);
                continue;
            }

            Guid deviceId = dbInterface.getId();
            dev.put(VdsProperties.DeviceId, deviceId.toString());
            dev.put(VdsProperties.SpecParams, devIdToDbDev.get(deviceId).getSpecParams());

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseVideos(XmlDocument document, List<VmDevice> devices) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.VIDEO);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.VIDEO)) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.VIDEO.getValue());
            dev.put(VdsProperties.Device, parseVideoType(node));
            dev.put(VdsProperties.Address, parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            // There is supposed to be one video device of each type (spice/vnc/..)
            VmDevice dbDev = dbDevices.stream()
                    .filter(d -> d.getDevice().equals(dev.get(VdsProperties.Device)))
                    .findFirst()
                    .orElse(null);

            if (dbDev == null) {
                log.warn("unmanaged video device with address '{}' is ignored", dev.get(VdsProperties.Address));
                continue;
            }

            dbDevices.remove(dbDev);
            dev.put(VdsProperties.DeviceId, dbDev.getId().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.getSpecParams());
            result.add(dev);
        }

        return result;
    }

    /**
     * This method should be used for managed devices with one instance per VM
     */
    private Map<String, Object> parseDev(VmDeviceGeneralType devType, XmlDocument document, List<VmDevice> devices) {
        VmDevice dbDevice = filterDevice(devices, devType);
        if (dbDevice == null) {
            return Collections.emptyMap();
        }

        XmlNode node = selectSingleNode(document, devType);
        if (node == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        result.put(VdsProperties.Device, devType);
        result.put(VdsProperties.DeviceId, dbDevice.getId().getDeviceId().toString());
        result.put(VdsProperties.Address, parseAddress(node));
        result.put(VdsProperties.Alias, parseAlias(node));
        result.put(VdsProperties.SpecParams, dbDevice.getSpecParams());
        return result;
    }

    private Map<String, Object> parseBalloon(XmlDocument document, List<VmDevice> devices) {
        VmDevice dbDevice = filterDevice(devices, VmDeviceGeneralType.BALLOON);
        if (dbDevice == null) {
            return Collections.emptyMap();
        }

        XmlNode node = document.selectSingleNode("//*/memballoon");
        if (node == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        result.put(VdsProperties.Device, "memballoon");
        result.put(VdsProperties.DeviceId, dbDevice.getId().getDeviceId().toString());
        result.put(VdsProperties.Address, parseAddress(node));
        result.put(VdsProperties.Alias, parseAlias(node));
        result.put(VdsProperties.SpecParams, dbDevice.getSpecParams());
        return result;
    }

    private String parseAddress(XmlNode node) {
        String result = "";
        XmlNode addressNode = node.selectSingleNode("address");
        if (addressNode == null) {
            return "";
        }
        XmlAttribute val = addressNode.attributes.get("type");
        result += String.format("%s=%s", "type", val.getValue());
        val = addressNode.attributes.get("slot");
        if (val != null) {
            result += String.format(", %s=%s", "slot", val.getValue());
        }
        val = addressNode.attributes.get("bus");
        if (val != null) {
            result += String.format(", %s=%s", "bus", val.getValue());
        }
        val = addressNode.attributes.get("domain");
        if (val != null) {
            result += String.format(", %s=%s", "domain", val.getValue());
        }
        val = addressNode.attributes.get("function");
        if (val != null) {
            result += String.format(", %s=%s", "function", val.getValue());
        }
        val = addressNode.attributes.get("controller");
        if (val != null) {
            result += String.format(", %s=%s", "controller", val.getValue());
        }
        val = addressNode.attributes.get("target");
        if (val != null) {
            result += String.format(", %s=%s", "target", val.getValue());
        }
        val = addressNode.attributes.get("unit");
        if (val != null) {
            result += String.format(", %s=%s", "unit", val.getValue());
        }
        val = addressNode.attributes.get("port");
        if (val != null) {
            result += String.format(", %s=%s", "port", val.getValue());
        }
        val = addressNode.attributes.get("multifunction");
        if (val != null) {
            result += String.format(", %s=%s", "multifunction", val.getValue());
        }
        val = addressNode.attributes.get("base");
        if (val != null) {
            result += String.format(", %s=%s", "base", val.getValue());
        }

        return result.isEmpty() ? result : String.format("{%s}", result);
    }

    private String parseAttribute(XmlNode node, String attribute) {
        XmlAttribute xmlAttribute = node.attributes.get(attribute);
        return xmlAttribute != null ? xmlAttribute.getValue() : null;
    }

    private String parseAlias(XmlNode node) {
        XmlNode aliasNode = node.selectSingleNode("alias");
        return aliasNode.attributes.get("name").getValue();
    }

    private String parseDiskPath(XmlNode node) {
        XmlNode sourceNode = node.selectSingleNode("source");
        if (sourceNode == null) {
            return "";
        }
        XmlAttribute attr = sourceNode.attributes.get("file");
        if (attr != null) {
            return attr.getValue();
        }
        attr = sourceNode.attributes.get("dev");
        if (attr != null) {
            return attr.getValue();
        }
        attr = sourceNode.attributes.get("name");
        if (attr != null) {
            return attr.getValue();
        }
        return "";
    }

    private Map<String, String> parseHostAddress(XmlNode node) {
        XmlNode sourceNode = node.selectSingleNode("source");
        if (sourceNode == null) {
            return null;
        }

        XmlNode addressNode = sourceNode.selectSingleNode("address");
        if (addressNode == null) {
            return null;
        }

        Map<String, String> address = new HashMap<>();
        Arrays.asList("domain", "slot", "bus", "function", "device", "host", "target", "lun").forEach(key -> {
            XmlAttribute attribute = addressNode.attributes.get(key);
            if (attribute != null) {
                String valStr = attribute.getValue();
                boolean hex = valStr.startsWith("0x");
                int val = Integer.parseInt(hex ? valStr.substring(2) : valStr, hex ? 16 : 10);
                address.put(key, String.valueOf(val));
            }
        });
        return address;
    }

    private String parseMacAddress(XmlNode node) {
        XmlNode macNode = node.selectSingleNode("mac");
        return macNode.attributes.get("address").getValue();
    }

    private String parseVideoType(XmlNode node) {
        XmlNode videoModelNode = node.selectSingleNode("model");
        return videoModelNode.attributes.get("type").getValue();
    }

    private XmlNodeList selectNodes(XmlDocument document, VmDeviceGeneralType devType) {
        return document.selectNodes("//*/" + devType.getValue());
    }

    private XmlNode selectSingleNode(XmlDocument document, VmDeviceGeneralType devType) {
        return document.selectSingleNode("//*/" + devType.getValue());
    }

    private List<VmDevice> filterDevices(List<VmDevice> devices, VmDeviceGeneralType devType) {
        return devices.stream().filter(d -> d.getType() == devType).collect(Collectors.toList());
    }

    private VmDevice filterDevice(List<VmDevice> devices, VmDeviceGeneralType devType) {
        return devices.stream().filter(d -> d.getType() == devType).findFirst().orElse(null);
    }
}
