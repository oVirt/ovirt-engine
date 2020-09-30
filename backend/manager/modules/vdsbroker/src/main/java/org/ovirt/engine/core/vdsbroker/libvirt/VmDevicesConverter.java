package org.ovirt.engine.core.vdsbroker.libvirt;

import static org.ovirt.engine.core.common.utils.VmDeviceCommonUtils.SPEC_PARAM_NODE;
import static org.ovirt.engine.core.common.utils.VmDeviceCommonUtils.SPEC_PARAM_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbControllerModel;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.utils.ovf.xml.XmlAttribute;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNamespaceManager;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder;
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
    private DiskLunMapDao diskLunMapDao;
    @Inject
    private ResourceManager resourceManager;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public static final String DEVICE = "device";
    private static final String TYPE = "type";
    private static final String MODEL = "model";
    private static final String INDEX = "index";
    private static final String IO_THREAD_ID = "ioThreadId";
    private static final String NODE = "node";
    private static final String SIZE = "size";
    private static final String DEVICES_START_ELEMENT = "<devices>";
    private static final String DEVICES_END_ELEMENT = "</devices>";

    public Map<String, Object> convert(Guid vmId, Guid hostId, String xml) throws Exception {
        String devicesXml = xml.substring(
                xml.indexOf(DEVICES_START_ELEMENT),
                xml.indexOf(DEVICES_END_ELEMENT) + DEVICES_END_ELEMENT.length());
        XmlDocument document = new XmlDocument(devicesXml);
        XmlNode metadata = new XmlDocument(xml).selectSingleNode("domain/metadata");
        Map<String, Object> result = new HashMap<>();
        result.put(VdsProperties.vm_guid, vmId.toString());
        result.put(VdsProperties.Devices, parseDevices(vmId, hostId, document));
        result.put(VdsProperties.GuestDiskMapping, parseDiskMapping(metadata));
        return result;
    }

    private Map<String, Object> parseDiskMapping(XmlNode metadata) throws Exception {
        if (metadata == null) {
            return null;
        }
        XmlNamespaceManager xmlNS = new XmlNamespaceManager();
        xmlNS.addNamespace(LibvirtVmXmlBuilder.OVIRT_VM_PREFIX, LibvirtVmXmlBuilder.OVIRT_VM_URI);
        XmlNode vm = metadata.selectSingleNode("ovirt-vm:vm", xmlNS);
        if (vm == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        for (XmlNode node : vm.selectNodes("ovirt-vm:device", xmlNS)) {
            if (!VmDeviceGeneralType.DISK.getValue().equals(DomainXmlUtils.parseAttribute(node, "devtype"))) {
                continue;
            }

            XmlNode guestNameNode = node.selectSingleNode("ovirt-vm:guestName", xmlNS);
            if (guestNameNode != null) {
                // Both LUN and regular disk are having imageID. Therefore, we first check the LUN.
                XmlNode lunId = node.selectSingleNode("ovirt-vm:GUID", xmlNS);
                if (lunId != null) {
                    // direct LUN
                    result.put(diskLunMapDao.getDiskIdByLunId(lunId.innerText).getDiskId().toString(),
                            Collections.singletonMap(VdsProperties.Name, guestNameNode.innerText));
                } else {
                    XmlNode imageId = node.selectSingleNode("ovirt-vm:imageID", xmlNS);
                    if (imageId != null) {
                        // regular disk
                        result.put(imageId.innerText,
                                Collections.singletonMap(VdsProperties.Name, guestNameNode.innerText));
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object>[] parseDevices(Guid vmId, Guid hostId, XmlDocument document) throws Exception {
        List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vmId);
        OriginType vmOrigin = resourceManager.getVmManager(vmId).getOrigin();
        boolean isHostedEngine = OriginType.HOSTED_ENGINE == vmOrigin || OriginType.MANAGED_HOSTED_ENGINE == vmOrigin;
        MemoizingSupplier<Map<Map<String, String>, HostDevice>> addressToHostDeviceSupplier =
                new MemoizingSupplier<>(() -> hostDeviceDao.getHostDevicesByHostId(hostId)
                        .stream()
                        .filter(device -> !device.getAddress().isEmpty())
                        .collect(Collectors.toMap(HostDevice::getAddress, device -> device)));

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(parseBalloon(document, devices)); // memballoon
        result.add(parseDev(VmDeviceGeneralType.RNG, document, devices));
        result.add(parseDev(VmDeviceGeneralType.WATCHDOG, document, devices));
        result.add(parseDev(VmDeviceGeneralType.SMARTCARD, document, devices));
        result.add(parseDev(VmDeviceGeneralType.SOUND, document, devices));
        result.add(parseDev(VmDeviceGeneralType.CONSOLE, document, devices));
        result.addAll(parseChannels(document, devices));
        result.addAll(parseControllers(document, devices, isHostedEngine));
        result.addAll(parseVideos(document, devices, isHostedEngine));
        result.addAll(parseInterfaces(document, devices, addressToHostDeviceSupplier, isHostedEngine));
        result.addAll(parseDisks(document, devices, isHostedEngine));
        result.addAll(parseRedirs(document, devices));
        result.addAll(parseMemories(document, devices));
        result.addAll(parseNvdimms(document, devices, hostId));
        result.addAll(parseManagedHostDevices(document, devices, addressToHostDeviceSupplier));
        result.addAll(parseUnmanagedHostDevices(document, devices, addressToHostDeviceSupplier));
        return result.stream()
                .filter(map -> !map.isEmpty())
                .toArray(Map[]::new);
    }

    private List<Map<String, Object>> parseChannels(XmlDocument document, List<VmDevice> devices) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.CHANNEL)) {
            String address = DomainXmlUtils.parseAddress(node);
            // Ignore channel devices without address
            if (address.isEmpty()) {
                continue;
            }

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.CHANNEL.getValue());
            dev.put(VdsProperties.Device, DomainXmlUtils.parseAttribute(node, TYPE)); // shouldn't it be VdsProperties.DeviceType?
            dev.put(VdsProperties.Address, address);
            dev.put(VdsProperties.Alias, parseAlias(node));
            // Channel devices are not sent within the domain xml during VM launching.
            // Therefore, unlike other devices there is no need to call the correlateWithAlias
            // function here because there will not be a database channel device available.
            // We just need to create a new channel device here.
            dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseControllers(XmlDocument document, List<VmDevice> devices,
            boolean isHostedEngine) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.CONTROLLER);

        // devices with spec params to appear first
        dbDevices.sort((d1, d2) -> d1.getSpecParams().isEmpty() && !d2.getSpecParams().isEmpty() ? 1 : 0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.CONTROLLER)) {
            String address = DomainXmlUtils.parseAddress(node);
            String index = DomainXmlUtils.parseAttribute(node, INDEX);
            String model = DomainXmlUtils.parseAttribute(node, MODEL);
            Integer ioThreadId = DomainXmlUtils.parseIoThreadId(node);
            String devType = "virtio-scsi".equals(model) ? model : DomainXmlUtils.parseAttribute(node, TYPE);

            boolean devWithModelNone = model != null ? model.equals(UsbControllerModel.NONE.libvirtName) : false;
            // Ignore controller devices without address, unless it is a device with model='none'
            // which is a special case of a device without address that is still marked as plugged
            if (address.isEmpty() && !devWithModelNone) {
                continue;
            }

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.CONTROLLER.getValue());
            dev.put(VdsProperties.Device, devType);
            dev.put(VdsProperties.Address, address);
            dev.put(VdsProperties.Alias, parseAlias(node));

            Optional<VmDevice> dbDev = Optional.empty();
            // Controllers such as pci controllers that are not sent by the engine during initial loading, but are
            // received from the dumpxml will not have the device id in the alias. To avoid a warning for these types
            // of devices we first validate if the alias contains the user access prefix and the device is unmanged.
            // If not the correlationWithUserAlias function is skipped.
            if (dev.get(VdsProperties.Alias).toString().startsWith(DomainXmlUtils.USER_ALIAS_PREFIX)
                    || dbDevices.stream().anyMatch(d -> d.isManaged() && devType.equals(d.getDevice()))) {
                dbDev = correlateWithAlias(dev, dbDevices, isHostedEngine);
            }

            if (dbDev.isPresent()) {
                dbDevices.remove(dbDev.get());
                dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
                dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());
            } else {
                dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());
                Map<String, Object> specParams = new HashMap<>();
                if (index != null) {
                    specParams.put(INDEX, index);
                }
                if (model != null) {
                    specParams.put(MODEL, model);
                }
                if (ioThreadId != null) {
                    specParams.put(IO_THREAD_ID, ioThreadId);
                }
                dev.put(VdsProperties.SpecParams, specParams);
            }

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseMemories(XmlDocument document, List<VmDevice> devices) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.MEMORY);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : document.selectNodes("//*/memory[@model != 'nvdimm']")) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.MEMORY.getValue());
            dev.put(VdsProperties.Device, VmDeviceGeneralType.MEMORY.getValue());
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            XmlNode target = node.selectSingleNode("target");
            if (target == null) {
                continue;
            }

            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, false);

            if (dbDev.isPresent()) {
                dbDevices.remove(dbDev.get());
                dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
                dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());
            } else {
                dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());
                Map<String, Object> specParams = new HashMap<>();
                specParams.put(SPEC_PARAM_NODE, target.selectSingleNode(NODE).innerText);
                specParams.put(SPEC_PARAM_SIZE, kiloBytesToMegaBytes(target.selectSingleNode(SIZE).innerText));
                dev.put(VdsProperties.SpecParams, specParams);
            }

            result.add(dev);
        }
        return result;
    }

    private String getDevicePath(final HostDevice hostDevice) {
        return (String)hostDevice.getSpecParams().get(VdsProperties.DEVICE_PATH);
    }

    private List<Map<String, Object>> parseNvdimms(XmlDocument document, List<VmDevice> devices, Guid hostId) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.HOSTDEV);
        MemoizingSupplier<Map<String, HostDevice>> pathToHostDeviceSupplier =
                new MemoizingSupplier<>(() -> hostDeviceDao.getHostDevicesByHostId(hostId)
                        .stream()
                        .filter(device -> device.getCapability().equals("nvdimm"))
                        .collect(Collectors.toMap(device -> getDevicePath(device), device -> device)));

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : document.selectNodes("//*/memory[@model='nvdimm']")) {
            XmlNode path = node.selectSingleNode("./source/path");
            if (path == null) {
                log.warn("No <path> found in NVDIMM device XML");
                continue;
            }
            HostDevice hostDevice = pathToHostDeviceSupplier.get().get(path.innerText);
            if (hostDevice == null) {
                log.warn("NVDIMM device of '{}' could not be matched with any known device", path);
                continue;
            }

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Type, VmDeviceGeneralType.HOSTDEV.getValue());
            dev.put(VdsProperties.Alias, parseAlias(node));
            dev.put(VdsProperties.Device, hostDevice.getDeviceName());

            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, false);

            if (!dbDev.isPresent()) {
                log.warn("NVDIMM device '{}' does not exist in the database, thus ignored",
                         hostDevice.getDeviceName());
                continue;
            }

            dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());

            result.add(dev);
        }
        return result;
    }

    private String kiloBytesToMegaBytes(String value) {
        final int intKbValue = Integer.parseUnsignedInt(value);
        return String.valueOf(intKbValue / 1024);
    }

    private List<Map<String, Object>> parseUnmanagedHostDevices(XmlDocument document, List<VmDevice> devices,
            MemoizingSupplier<Map<Map<String, String>, HostDevice>> addressToHostDeviceSupplier) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.HOSTDEV);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : document.selectNodes("//*/hostdev")) {
            Map<String, String> hostAddress = parseHostAddress(node);
            if (hostAddress == null) {
                continue;
            }

            if (addressToHostDeviceSupplier.get().containsKey(hostAddress)) {
                // managed
                continue;
            }

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.HOSTDEV.getValue());
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));
            String deviceType = DomainXmlUtils.parseAttribute(node, TYPE);
            dev.put(VdsProperties.Device, deviceType);
            dev.put(VdsProperties.SpecParams, hostAddress);

            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, false);

            dev.put(VdsProperties.DeviceId, dbDev.isPresent() ? dbDev.get().getDeviceId().toString() : Guid.newGuid().toString());
            result.add(dev);
        }
        return result;
    }

    /**
     * This method processes managed host devices (those that are set by the engine).
     * That means that the device should already exist in the database and can be correlated
     * with one of the devices of the host. Host devices that were designed to be added as
     * unmanaged devices, like mdev devices, are handled separately.
     */
    private List<Map<String, Object>> parseManagedHostDevices(XmlDocument document, List<VmDevice> devices,
            MemoizingSupplier<Map<Map<String, String>, HostDevice>> addressToHostDeviceSupplier) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.HOSTDEV);
        if (dbDevices.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : document.selectNodes("//*/hostdev")) {
            Map<String, String> hostAddress = parseHostAddress(node);
            if (hostAddress == null) {
                continue;
            }

            HostDevice hostDevice = addressToHostDeviceSupplier.get().get(hostAddress);
            if (hostDevice == null) {
                // unmanaged
                continue;
            }

            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Type, VmDeviceGeneralType.HOSTDEV.getValue());
            dev.put(VdsProperties.Alias, parseAlias(node));
            dev.put(VdsProperties.Device, hostDevice.getDeviceName());

            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, false);

            if (!dbDev.isPresent()) {
                log.warn("VM host device '{}' does not exist in the database, thus ignored",
                        hostDevice.getDeviceName());
                continue;
            }

            dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());

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
            dev.put(VdsProperties.Device, DomainXmlUtils.parseAttribute(node, TYPE));
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, false);

            if (dbDev.isPresent()) {
                dbDevices.remove(dbDev.get());
                dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
                dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());
            } else {
                dev.put(VdsProperties.DeviceId, Guid.newGuid().toString());
            }

            result.add(dev);
        }
        return result;
    }

    List<Map<String, Object>> parseDisks(XmlDocument document, List<VmDevice> devices, boolean isHostedEngine) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.DISK, VmDeviceGeneralType.HOSTDEV);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.DISK)) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.DISK.getValue());
            String diskType = DomainXmlUtils.parseAttribute(node, DEVICE);
            dev.put(VdsProperties.Device, diskType);
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            String path = DomainXmlUtils.parseDiskPath(node);
            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, isHostedEngine);

            if (!dbDev.isPresent()) {
                log.warn("unmanaged disk with path '{}' is ignored", path);
                continue;
            }

            dbDevices.remove(dbDev.get());

            dev.put(VdsProperties.ImageId, parseImageIdFromPath(path));
            dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());

            List<Map<String, Object>> volumeChain = parseVolumeChain(node);
            if (!volumeChain.isEmpty()) {
                dev.put(VdsProperties.VolumeChain, volumeChain.toArray());
            }

            result.add(dev);
        }
        return result;
    }

    private List<Map<String, Object>> parseInterfaces(XmlDocument document, List<VmDevice> devices,
            MemoizingSupplier<Map<Map<String, String>, HostDevice>> addressToHostDeviceSupplier,
            boolean isHostedEngine) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.INTERFACE);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.INTERFACE)) {
            String type = DomainXmlUtils.parseAttribute(node, TYPE);
            Map<String, Object> dev = new HashMap<>();

            if (VmDeviceType.HOST_DEVICE.getName().equals(type)) {
                dev.put(VdsProperties.HostDev, getHostDeviceName(node, addressToHostDeviceSupplier));
            }

            dev.put(VdsProperties.Type, VmDeviceGeneralType.INTERFACE.getValue());
            dev.put(VdsProperties.Device, type);
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, isHostedEngine);

            if (!dbDev.isPresent()) {
                String macAddress = DomainXmlUtils.parseMacAddress(node);
                log.warn("unmanaged network interface with mac address '{}' is ignored", macAddress);
                continue;
            }

            dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());

            result.add(dev);
        }
        return result;
    }

    private String getHostDeviceName(XmlNode hostDevInterfaceNode,
            MemoizingSupplier<Map<Map<String, String>, HostDevice>> addressToHostDeviceSupplier) {
        Map<String, String> hostAddress = parseHostAddress(hostDevInterfaceNode);
        if (hostAddress == null) {
            return null;
        }
        HostDevice hostDevice = addressToHostDeviceSupplier.get().get(hostAddress);
        if (hostDevice == null) {
            return null;
        }
        return hostDevice.getDeviceName();
    }

    private List<Map<String, Object>> parseVideos(XmlDocument document, List<VmDevice> devices,
            boolean isHostedEngine) {
        List<VmDevice> dbDevices = filterDevices(devices, VmDeviceGeneralType.VIDEO);

        List<Map<String, Object>> result = new ArrayList<>();
        for (XmlNode node : selectNodes(document, VmDeviceGeneralType.VIDEO)) {
            Map<String, Object> dev = new HashMap<>();
            dev.put(VdsProperties.Type, VmDeviceGeneralType.VIDEO.getValue());
            dev.put(VdsProperties.Device, DomainXmlUtils.parseVideoType(node));
            dev.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
            dev.put(VdsProperties.Alias, parseAlias(node));

            // There is supposed to be one video device of each type (spice/vnc/..)
            Optional<VmDevice> dbDev = correlateWithAlias(dev, dbDevices, isHostedEngine);

            if (!dbDev.isPresent()) {
                log.warn("unmanaged video device with address '{}' is ignored", dev.get(VdsProperties.Address));
                continue;
            }

            dbDevices.remove(dbDev.get());
            dev.put(VdsProperties.DeviceId, dbDev.get().getDeviceId().toString());
            dev.put(VdsProperties.SpecParams, dbDev.get().getSpecParams());
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
        result.put(VdsProperties.DeviceId, dbDevice.getDeviceId().toString());
        result.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
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
        result.put(VdsProperties.DeviceId, dbDevice.getDeviceId().toString());
        result.put(VdsProperties.Address, DomainXmlUtils.parseAddress(node));
        result.put(VdsProperties.Alias, parseAlias(node));
        result.put(VdsProperties.SpecParams, dbDevice.getSpecParams());
        return result;
    }

    List<Map<String, Object>> parseVolumeChain(XmlNode xmlNode) {
        List<Map<String, Object>> chain = new ArrayList<>();

        while (true) {
            String path = DomainXmlUtils.parseDiskPath(xmlNode);

            String volumeId = parseVolumeIdFromPath(path);
            if (!StringUtils.isEmpty(volumeId)) {
                // needs to be returned in the opposite order as provided by libvirt
                chain.add(0, Collections.singletonMap(VdsProperties.VolumeId, volumeId));
            }

            xmlNode = xmlNode.selectSingleNode("backingStore");
            if (xmlNode == null) {
                return chain;
            }
        }
    }

    String parseImageIdFromPath(String path) {
        return parsePathSegment(path, 2);
    }

    String parseVolumeIdFromPath(String path) {
        return parsePathSegment(path, 1);
    }

    private String parsePathSegment(String path, int index) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        String[] pathSegments = path.split("/");
        if (pathSegments.length < index + 1) {
            return "";
        }
        return pathSegments[pathSegments.length - index];
    }

    public static String parseAlias(XmlNode node) {
        XmlNode aliasNode = node.selectSingleNode("alias");
        return aliasNode != null ? aliasNode.attributes.get("name").getValue() : "";
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
        XmlAttribute uuidAttribute = addressNode.attributes.get("uuid");
        if (uuidAttribute != null) {
            address.put("uuid", uuidAttribute.getValue());
        }
        return address;
    }

    private XmlNodeList selectNodes(XmlDocument document, VmDeviceGeneralType devType) {
        return document.selectNodes("//*/" + devType.getValue());
    }

    private XmlNode selectSingleNode(XmlDocument document, VmDeviceGeneralType devType) {
        return document.selectSingleNode("//*/" + devType.getValue());
    }

    private List<VmDevice> filterDevices(List<VmDevice> devices, VmDeviceGeneralType... devType) {
        final List<VmDeviceGeneralType> devTypes = Arrays.asList(devType);
        return devices.stream().filter(d -> devTypes.contains(d.getType())).collect(Collectors.toList());
    }

    private VmDevice filterDevice(List<VmDevice> devices, VmDeviceGeneralType devType) {
        return devices.stream().filter(d -> d.getType() == devType).findFirst().orElse(null);
    }

    private Optional<VmDevice> correlateWithAlias(Map<String, Object> device, List<VmDevice> dbDevices,
            boolean isHostedEngine) {
        String alias = (String) device.get(VdsProperties.Alias);
        try {
            Guid deviceId = Guid.createGuidFromString(alias.substring(DomainXmlUtils.USER_ALIAS_PREFIX.length()));
            Optional<VmDevice> result = dbDevices.stream()
                    .filter(dev -> deviceId.equals(dev.getDeviceId()))
                    .findFirst();
            if (result.isPresent()) {
                return result;
            }
        } catch(Exception e) {
            log.debug("Received unexpected user-alias: {}", alias);
        }
        // We need it in case the user have a VM running from old versions(4.1) without shutting it down as a backward
        // compatibility.
        Optional<VmDevice> result = dbDevices.stream()
                .filter(dev -> alias.equals(dev.getAlias()))
                .findFirst();
        if (result.isPresent()) {
            return result;
        }
        // In case of HE VM, it is possible the alias is missing after the first boot.
        if (isHostedEngine) {
            result = dbDevices.stream()
                    .filter(d -> d.isManaged() && d.getDevice().equals(device.get(VdsProperties.Device))
                            && d.getType().getValue().equals(device.get(VdsProperties.Type)))
                    .findAny();
            if (result.isPresent() && result.stream().count() == 1) {
                return result;
            }
        }
        log.warn("The alias wasn't found in the database, {}", alias);
        return Optional.empty();
    }
}
