package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNamespaceManager;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.compat.backendcompat.XmlNodeList;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class OvfReader implements IOvfBuilder {
    protected java.util.ArrayList<DiskImage> _images;
    protected java.util.ArrayList<VmNetworkInterface> interfaces;
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;
    private static final int BYTES_IN_GB = 1024 * 1024 * 1024;
    public static final String EmptyName = "[Empty Name]";
    protected String name = EmptyName;
    private String version;
    private final VmBase vmBase;
    private DisplayType defaultDisplayType;

    public OvfReader(XmlDocument document, ArrayList<DiskImage> images, ArrayList<VmNetworkInterface> interfaces, VmBase vmBase) {
        _images = images;
        this.interfaces = interfaces;
        _document = document;
        this.vmBase = vmBase;

        _xmlNS = new XmlNamespaceManager(_document.NameTable);
        _xmlNS.AddNamespace("ovf", OVF_URI);
        _xmlNS.AddNamespace("rasd", RASD_URI);
        _xmlNS.AddNamespace("vssd", VSSD_URI);
        _xmlNS.AddNamespace("xsi", XSI_URI);
        readHeader();

    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    /**
     * reads the OVF header
     */
    private void readHeader() {
        version = "";
        XmlNode node = _document.SelectSingleNode("//ovf:Envelope", _xmlNS);
        if (node != null) {
            version = node.Attributes.get("ovf:version").getValue();
        }
    }

    @Override
    public void buildReference() {
        buildImageReference();
        buildNicReference();
    }

    @Override
    public void buildNetwork() {
        // No implementation - networks aren't read from the OVF.
    }

    protected long convertGigabyteToBytes(long gb) {
        return gb * BYTES_IN_GB;
    }

    @Override
    public void buildDisk() {
        XmlNodeList list = _document.SelectNodes("//*/Section/Disk");
        for (XmlNode node : list) {
            final Guid guid = new Guid(node.Attributes.get("ovf:diskId").getValue());

            DiskImage image = LinqUtils.firstOrNull(_images, new Predicate<DiskImage>() {
                @Override
                public boolean eval(DiskImage diskImage) {
                    return diskImage.getImageId().equals(guid);
                }
            });

            if (node.Attributes.get("ovf:vm_snapshot_id") != null) {
                image.setvm_snapshot_id(new Guid(node.Attributes.get("ovf:vm_snapshot_id").getValue()));
            }

            if (!StringUtils.isEmpty(node.Attributes.get("ovf:size").getValue())) {
                image.setsize(convertGigabyteToBytes(Long.parseLong(node.Attributes.get("ovf:size").getValue())));
            }
            if (!StringUtils.isEmpty(node.Attributes.get("ovf:actual_size").getValue())) {
                image.setactual_size(convertGigabyteToBytes(Long.parseLong(node.Attributes.get("ovf:actual_size").getValue())));
            }
            if (node.Attributes.get("ovf:volume-format") != null) {
                if (!StringUtils.isEmpty(node.Attributes.get("ovf:volume-format").getValue())) {
                    image.setvolume_format(VolumeFormat.valueOf(node.Attributes.get("ovf:volume-format").getValue()));
                } else {
                    image.setvolume_format(VolumeFormat.Unassigned);
                }
            }
            else {
                image.setvolume_format(VolumeFormat.Unassigned);
            }
            if (node.Attributes.get("ovf:volume-type") != null) {
                if (!StringUtils.isEmpty(node.Attributes.get("ovf:volume-type").getValue())) {
                    image.setvolume_type(VolumeType.valueOf(node.Attributes.get("ovf:volume-type").getValue()));
                } else {
                    image.setvolume_type(VolumeType.Unassigned);
                }
            }
            else {
                image.setvolume_type(VolumeType.Unassigned);
            }
            if (node.Attributes.get("ovf:disk-interface") != null) {
                if (!StringUtils.isEmpty(node.Attributes.get("ovf:disk-interface").getValue())) {
                    image.setDiskInterface(DiskInterface.valueOf(node.Attributes.get("ovf:disk-interface").getValue()));
                }
            }
            else {
                image.setDiskInterface(DiskInterface.IDE);
            }
            if (node.Attributes.get("ovf:boot") != null) {
                if (!StringUtils.isEmpty(node.Attributes.get("ovf:boot").getValue())) {
                    image.setBoot(Boolean.parseBoolean(node.Attributes.get("ovf:boot").getValue()));
                }
            }
            if (node.Attributes.get("ovf:wipe-after-delete") != null) {
                if (!StringUtils.isEmpty(node.Attributes.get("ovf:wipe-after-delete").getValue())) {
                    image.setWipeAfterDelete(Boolean.parseBoolean(node.Attributes.get("ovf:wipe-after-delete")
                            .getValue()));
                }
            }
            if (node.Attributes.get("ovf:disk-alias") != null) {
                if (!StringUtils.isEmpty(node.Attributes.get("ovf:disk-alias").getValue())) {
                    image.setDiskAlias(String.valueOf(node.Attributes.get("ovf:disk-alias")
                            .getValue()));
                }
            }
            if (node.Attributes.get("ovf:disk-description") != null) {
                if (!StringUtils.isEmpty(node.Attributes.get("ovf:disk-description").getValue())) {
                    image.setDiskDescription(String.valueOf(node.Attributes.get("ovf:disk-description")
                            .getValue()));
                }
            }
        }
    }

    @Override
    public void buildVirtualSystem() {
        readGeneralData();
    }

    /**
     * Reads vm device attributes from OVF and stores in in the collection
     *
     * @param node
     * @param vmBase
     * @param deviceId
     */
    public void readVmDevice(XmlNode node, VmBase vmBase, Guid deviceId, boolean isManaged) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(deviceId, vmBase.getId()));
        if (node.SelectSingleNode(OvfProperties.VMD_ADDRESS, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_ADDRESS, _xmlNS).InnerText)) {
            vmDevice.setAddress(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_ADDRESS, _xmlNS).InnerText));
        } else {
            vmDevice.setAddress("");
        }
        if (node.SelectSingleNode(OvfProperties.VMD_ALIAS, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_ALIAS, _xmlNS).InnerText)) {
            vmDevice.setAlias(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_ALIAS, _xmlNS).InnerText));
        } else {
            vmDevice.setAlias("");
        }
        XmlNode specParamsNode = node.SelectSingleNode(OvfProperties.VMD_SPEC_PARAMS, _xmlNS);
        if (specParamsNode != null
                && !StringUtils.isEmpty(specParamsNode.InnerText)) {
            vmDevice.setSpecParams(getMapNode(specParamsNode));
        } else {
            // Empty map
            vmDevice.setSpecParams(Collections.<String,Object>emptyMap());
        }
        if (node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).InnerText)) {
            vmDevice.setType(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).InnerText));
        } else {
            int resourceType = getResourceType(node, OvfProperties.VMD_RESOURCE_TYPE);
            vmDevice.setType(VmDeviceType.getoVirtDevice(resourceType).getName());
        }
        if (node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS).InnerText)) {
            vmDevice.setDevice(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS).InnerText));
        } else {
            setDeviceByResource(node, vmDevice);
        }
        if (node.SelectSingleNode(OvfProperties.VMD_BOOT_ORDER, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_BOOT_ORDER, _xmlNS).InnerText)) {
            vmDevice.setBootOrder(Integer.valueOf(node.SelectSingleNode(OvfProperties.VMD_BOOT_ORDER, _xmlNS).InnerText));
        } else {
            vmDevice.setBootOrder(0);
        }
        if (node.SelectSingleNode(OvfProperties.VMD_IS_PLUGGED, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_IS_PLUGGED, _xmlNS).InnerText)) {
            vmDevice.setIsPlugged(Boolean.valueOf(node.SelectSingleNode(OvfProperties.VMD_IS_PLUGGED, _xmlNS).InnerText));
        } else {
            vmDevice.setIsPlugged(Boolean.TRUE);
        }
        if (node.SelectSingleNode(OvfProperties.VMD_IS_READONLY, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_IS_READONLY, _xmlNS).InnerText)) {
            vmDevice.setIsReadOnly(Boolean.valueOf(node.SelectSingleNode(OvfProperties.VMD_IS_READONLY, _xmlNS).InnerText));
        } else {
            vmDevice.setIsReadOnly(Boolean.FALSE);
        }
        if (isManaged) {
            vmBase.getManagedDeviceMap().put(vmDevice.getDeviceId(), vmDevice);
        } else {
            vmBase.getUnmanagedDeviceList().add(vmDevice);
        }
    }

    /**
     * gets the VM interface
     *
     * @param node
     *            the xml node
     * @return VmNetworkInterface
     */
    public VmNetworkInterface getNetwotkInterface(XmlNode node) {
        // prior to 3.0 the instanceId is int , in 3.1 and on this is Guid
        String str = node.SelectSingleNode("rasd:InstanceId", _xmlNS).InnerText;
        final Guid guid;
        VmNetworkInterface iface;
        if (!StringUtils.isNumeric(str)) { // 3.1 and above OVF format
            guid = new Guid(str);
            iface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
                @Override
                public boolean eval(VmNetworkInterface iface) {
                    return iface.getId().equals(guid);
                }
            });
            if (iface == null) {
                iface = new VmNetworkInterface();
                iface.setId(guid);
            }
        } else { // 3.0 and below OVF format
            iface = new VmNetworkInterface();
        }
        return iface;
    }

    /**
     * This method should return the string representation of 'default display type'
     * property in the ovf file. this representation is different for ovf file
     * of VM and ovf file of template.
     *
     * @return the String representation of 'default display type' property in the ovf file
     */
    protected abstract String getDefaultDisplayTypeStringRepresentation();

    protected abstract void readOsSection(XmlNode section);

    protected abstract void readHardwareSection(XmlNode section);

    protected void readGeneralData() {
        XmlNode content = _document.SelectSingleNode("//*/Content");
        XmlNode node;

        // set ovf version to the ovf object
        vmBase.setOvfVersion(getVersion());

        node = content.SelectSingleNode("Description");
        if (node != null) {
            vmBase.setDescription(node.InnerText);
        }

        node = content.SelectSingleNode("Domain");
        if (node != null) {
            vmBase.setDomain(node.InnerText);
        }

        node = content.SelectSingleNode("CreationDate");
        if (node != null) {
            Date creationDate = OvfParser.UtcDateStringToLocaDate(node.InnerText);
            if (creationDate != null) {
                vmBase.setCreationDate(creationDate);
            }
        }

        node = content.SelectSingleNode("ExportDate");
        if (node != null) {
            Date exportDate = OvfParser.UtcDateStringToLocaDate(node.InnerText);
            if (exportDate != null) {
                vmBase.setExportDate(exportDate);
            }
        }

        node = content.SelectSingleNode("IsAutoSuspend");
        if (node != null) {
            vmBase.setAutoSuspend(Boolean.parseBoolean(node.InnerText));
        }

        node = content.SelectSingleNode("TimeZone");
        if (node != null) {
            vmBase.setTimeZone(node.InnerText);
        }

        node = content.SelectSingleNode("default_boot_sequence");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setDefaultBootSequence(BootSequence.forValue(Integer.parseInt(node.InnerText)));
            }
        }

        node = content.SelectSingleNode("initrd_url");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setInitrdUrl(node.InnerText);
            }
        }

        node = content.SelectSingleNode("kernel_url");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setKernelUrl(node.InnerText);
            }
        }

        node = content.SelectSingleNode("kernel_params");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setKernelParams(node.InnerText);
            }
        }

        node = content.SelectSingleNode("Generation");
        if (node != null) {
            vmBase.setDbGeneration(Long.parseLong(node.InnerText));
        } else {
            vmBase.setDbGeneration(1L);
        }

        // Note: the fetching of 'default display type' should happen before reading
        // the hardware section
        node = content.SelectSingleNode(getDefaultDisplayTypeStringRepresentation());
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                defaultDisplayType = DisplayType.forValue(Integer.parseInt(node.InnerText));
                vmBase.setDefaultDisplayType(defaultDisplayType);
            }
        }

        XmlNodeList list = content.SelectNodes("Section");
        for (XmlNode section : list) {
            String value = section.Attributes.get("xsi:type").getValue();

            if ("ovf:OperatingSystemSection_Type".equals(value)) {
                readOsSection(section);

            }
            else if ("ovf:VirtualHardwareSection_Type".equals(value)) {
                readHardwareSection(section);
            } else if ("ovf:SnapshotsSection_Type".equals(value)) {
                readSnapshotsSection(section);
            }
        }

        node = content.SelectSingleNode("Origin");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setOrigin(OriginType.forValue(Integer.parseInt(node.InnerText)));
            }
        }

        node = content.SelectSingleNode("VmType");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setVmType(VmType.forValue(Integer.parseInt(node.InnerText)));
            }
        }

        node = content.SelectSingleNode("IsSmartcardEnabled");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setSmartcardEnabled(Boolean.parseBoolean(node.InnerText));
            }
        }

        node = content.SelectSingleNode("DeleteProtected");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setDeleteProtected(Boolean.parseBoolean(node.InnerText));
            }
        }

        node = content.SelectSingleNode("TunnelMigration");
        if (node != null) {
            if (!StringUtils.isEmpty(node.InnerText)) {
                vmBase.setTunnelMigration(Boolean.parseBoolean(node.InnerText));
            }
        }

        readGeneralData(content);
    }

    protected void readSnapshotsSection(@SuppressWarnings("unused") XmlNode section) {
        // The snapshot section only has meaning for VMs, and is overridden in OvfVmReader.
    }

    protected abstract void readGeneralData(XmlNode content);

    protected void buildNicReference() {
        XmlNodeList list = _document.SelectNodes("//*/Nic", _xmlNS);
        for (XmlNode node : list) {
            VmNetworkInterface iface = new VmNetworkInterface();
            iface.setId(new Guid(node.Attributes.get("ovf:id").getValue()));
            interfaces.add(iface);
        }
        if (!list.iterator().hasNext()) {
            StringBuilder sb = new StringBuilder();
            sb.append("//*/Item[");
            sb.append(OvfProperties.VMD_RESOURCE_TYPE);
            sb.append("=");
            sb.append(OvfHardware.Network);
            sb.append("]");
            list = _document.SelectNodes(sb.toString(), _xmlNS);
            for (XmlNode node : list) {
                VmNetworkInterface iface = new VmNetworkInterface();
                iface.setId(Guid.NewGuid());
                updateSingleNic(node, iface);
                interfaces.add(iface);
            }
        }
    }

    protected void updateSingleNic(XmlNode node, VmNetworkInterface iface) {

        iface.setName(node.SelectSingleNode(OvfProperties.VMD_NAME, _xmlNS).InnerText);
        iface.setMacAddress((node.SelectSingleNode("rasd:MACAddress", _xmlNS) != null) ? node.SelectSingleNode(
                "rasd:MACAddress", _xmlNS).InnerText : "");

        String networkName = node.SelectSingleNode(OvfProperties.VMD_CONNECTION, _xmlNS).InnerText;
        iface.setNetworkName(StringUtils.defaultIfEmpty(networkName, null));

        XmlNode linkedNode = node.SelectSingleNode(OvfProperties.VMD_LINKED, _xmlNS);
        iface.setLinked(linkedNode == null ? true : Boolean.valueOf(linkedNode.InnerText));

        String resourceSubType = node.SelectSingleNode("rasd:ResourceSubType", _xmlNS).InnerText;
        if (StringUtils.isNotEmpty(resourceSubType)) {
            iface.setType(Integer.parseInt(resourceSubType));
        }

        XmlNode speed = node.SelectSingleNode("rasd:speed", _xmlNS);
        iface.setSpeed((speed != null) ? Integer.parseInt(speed.InnerText) : VmInterfaceType.forValue(iface.getType())
                .getSpeed());

    }

    private void buildImageReference() {
        XmlNodeList list = _document.SelectNodes("//*/File", _xmlNS);
        for (XmlNode node : list) {
            DiskImage image = new DiskImage();
            image.setImageId(new Guid(node.Attributes.get("ovf:id").getValue()));
            image.setId(OvfParser.GetImageGrupIdFromImageFile(node.Attributes.get("ovf:href").getValue()));
            // Default values:
            image.setactive(true);
            image.setimageStatus(ImageStatus.OK);
            image.setdescription(node.Attributes.get("ovf:description").getValue());
            _images.add(image);
        }
    }

    private int getResourceType(XmlNode node, String resource) {
        if (node.SelectSingleNode(resource, _xmlNS) != null
                && !StringUtils.isEmpty(node.SelectSingleNode(resource, _xmlNS).InnerText)) {
            return Integer.valueOf(node.SelectSingleNode(resource, _xmlNS).InnerText);
        }
        return -1;
    }

    private void setDeviceByResource(XmlNode node, VmDevice vmDevice) {
        int resourceType = getResourceType(node, OvfProperties.VMD_RESOURCE_TYPE);
        int resourceSubType = getResourceType(node, OvfProperties.VMD_SUB_RESOURCE_TYPE);
        if (resourceSubType == -1) {
            // we need special handling for Monitor to define it as vnc or spice
            if (Integer.valueOf(OvfHardware.Monitor) == resourceType) {
                // if default display type is defined in the ovf, set the video device that is suitable for it
                if (defaultDisplayType != null) {
                    vmDevice.setDevice(defaultDisplayType.getVmDeviceType().getName());
                }
                else {
                    // get number of monitors from VirtualQuantity in OVF
                    if (node.SelectSingleNode(OvfProperties.VMD_VIRTUAL_QUANTITY, _xmlNS) != null
                            && !StringUtils.isEmpty(node.SelectSingleNode(OvfProperties.VMD_VIRTUAL_QUANTITY,
                                    _xmlNS).InnerText)) {
                        int virtualQuantity =
                                Integer.valueOf(node.SelectSingleNode(OvfProperties.VMD_VIRTUAL_QUANTITY, _xmlNS).InnerText);
                        if (virtualQuantity > 1) {
                            vmDevice.setDevice(VmDeviceType.QXL.getName());
                        } else {
                            vmDevice.setDevice(VmDeviceType.CIRRUS.getName());
                        }
                    } else { // default to spice if quantity not found
                        vmDevice.setDevice(VmDeviceType.QXL.getName());
                    }
                }
            } else {
                vmDevice.setDevice(VmDeviceType.getoVirtDevice(resourceType).getName());
            }
        } else if (Integer.valueOf(OvfHardware.Network) == resourceType) {
            // handle interfaces with different sub types : we have 0-3 as the VmInterfaceType enum
            boolean isKnownType = false;
            for (VmInterfaceType vmInterfaceType : VmInterfaceType.values()) {
                if (Integer.valueOf(vmInterfaceType.getValue()) == resourceSubType) {
                    vmDevice.setDevice(VmDeviceType.BRIDGE.getName());
                    isKnownType = true;
                    break;
                }
            }
            if (!isKnownType) {
                vmDevice.setDevice(VmDeviceType.getoVirtDevice(resourceType).getName());
            }
        }
    }

    private static Map<String,Object> getMapNode(XmlNode node) {
        Map<String,Object> returnValue = new HashMap<String,Object>();

        NodeList list = node.GetChildNodes();
        for (int index = 0; index < list.getLength(); ++index) {
            Node currNode = list.item(index);
            short nodeType = currNode.getNodeType();
            if (nodeType == Node.ELEMENT_NODE) {
                NodeList childNodes = currNode.getChildNodes();
                // If the element node has only one child, then it contains the value
                if (childNodes.getLength() == 1) {
                    Node valueNode = childNodes.item(0);
                    if (valueNode.getNodeType() == Node.TEXT_NODE) {
                        returnValue.put(currNode.getNodeName(), valueNode.getNodeValue());
                    }
                } else if (childNodes.getLength() > 1) {
                    // In this case, we have a nested map, so we parse it
                    returnValue.put(currNode.getNodeName(), getMapNode(new XmlNode(currNode)));
                }
            }
        }

        return returnValue;
    }

    @Override
    public String getStringRepresentation() {
        return _document.getOuterXml();
    }
}
