package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNamespaceManager;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.compat.backendcompat.XmlNodeList;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public abstract class OvfReader implements IOvfBuilder {
    protected java.util.ArrayList<DiskImage> _images;
    protected java.util.ArrayList<VmNetworkInterface> interfaces;
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;
    private static final int BYTES_IN_GB = 1024 * 1024 * 1024;
    public static final String EmptyName = "[Empty Name]";
    protected String name = EmptyName;
    private String version;

    public OvfReader(XmlDocument document, ArrayList<DiskImage> images, ArrayList<VmNetworkInterface> interfaces) {
        _images = images;
        this.interfaces = interfaces;
        _document = document;

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
    public void BuildReference() {
        buildImageReference();
        buildNicReference();
    }

    @Override
    public void BuildNetwork() {
    }

    protected long GigabyteToBytes(long gb) {
        return gb * BYTES_IN_GB;
    }

    @Override
    public void BuildDisk() {
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

            if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:size").getValue())) {
                image.setsize(GigabyteToBytes(Long.parseLong(node.Attributes.get("ovf:size").getValue())));
            }
            if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:actual_size").getValue())) {
                image.setactual_size(GigabyteToBytes(Long.parseLong(node.Attributes.get("ovf:actual_size").getValue())));
            }
            if (node.Attributes.get("ovf:volume-format") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:volume-format").getValue())) {
                    image.setvolume_format(VolumeFormat.valueOf(node.Attributes.get("ovf:volume-format").getValue()));
                } else {
                    image.setvolume_format(VolumeFormat.Unassigned);
                }
            }
            else {
                image.setvolume_format(VolumeFormat.Unassigned);
            }
            if (node.Attributes.get("ovf:volume-type") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:volume-type").getValue())) {
                    image.setvolume_type(VolumeType.valueOf(node.Attributes.get("ovf:volume-type").getValue()));
                } else {
                    image.setvolume_type(VolumeType.Unassigned);
                }
            }
            else {
                image.setvolume_type(VolumeType.Unassigned);
            }
            if (node.Attributes.get("ovf:disk-interface") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:disk-interface").getValue())) {
                    image.setDiskInterface(DiskInterface.valueOf(node.Attributes.get("ovf:disk-interface").getValue()));
                }
            }
            else {
                image.setDiskInterface(DiskInterface.IDE);
            }
            if (node.Attributes.get("ovf:boot") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:boot").getValue())) {
                    image.setBoot(Boolean.parseBoolean(node.Attributes.get("ovf:boot").getValue()));
                }
            }
            if (node.Attributes.get("ovf:wipe-after-delete") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:wipe-after-delete").getValue())) {
                    image.setWipeAfterDelete(Boolean.parseBoolean(node.Attributes.get("ovf:wipe-after-delete")
                            .getValue()));
                }
            }
        }
    }

    @Override
    public void BuildVirtualSystem() {
        ReadGeneralData();
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
                && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_ADDRESS, _xmlNS).InnerText)) {
            vmDevice.setAddress(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_ADDRESS, _xmlNS).InnerText));
        } else {
            vmDevice.setAddress("");
        }
        if (node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS) != null
                && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).InnerText)) {
            vmDevice.setType(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).InnerText));
        } else {
            int resourceType = getResourceType(node, vmDevice, OvfProperties.VMD_RESOURCE_TYPE);
            vmDevice.setType(VmDeviceType.getoVirtDevice(resourceType).getName());
        }
        if (node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS) != null
                && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS).InnerText)) {
            vmDevice.setDevice(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS).InnerText));
        } else {
            setDeviceByResource(node, vmDevice);
        }
        if (node.SelectSingleNode(OvfProperties.VMD_BOOT_ORDER, _xmlNS) != null
                && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_BOOT_ORDER, _xmlNS).InnerText)) {
            vmDevice.setBootOrder(Integer.valueOf(node.SelectSingleNode(OvfProperties.VMD_BOOT_ORDER, _xmlNS).InnerText));
        } else {
            vmDevice.setBootOrder(0);
        }
        if (node.SelectSingleNode(OvfProperties.VMD_IS_PLUGGED, _xmlNS) != null
                && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_IS_PLUGGED, _xmlNS).InnerText)) {
            vmDevice.setIsPlugged(Boolean.valueOf(node.SelectSingleNode(OvfProperties.VMD_IS_PLUGGED, _xmlNS).InnerText));
        } else {
            vmDevice.setIsPlugged(Boolean.TRUE);
        }
        if (node.SelectSingleNode(OvfProperties.VMD_IS_READONLY, _xmlNS) != null
                && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_IS_READONLY, _xmlNS).InnerText)) {
            vmDevice.setIsReadOnly(Boolean.valueOf(node.SelectSingleNode(OvfProperties.VMD_IS_READONLY, _xmlNS).InnerText));
        } else {
            vmDevice.setIsReadOnly(Boolean.FALSE);
        }
        if (isManaged) {
            vmBase.getManagedVmDeviceMap().put(vmDevice.getDeviceId(), vmDevice);
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

    protected abstract void ReadOsSection(XmlNode section);

    protected abstract void ReadHardwareSection(XmlNode section);

    protected abstract void ReadGeneralData();

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
        iface.setNetworkName(node.SelectSingleNode(OvfProperties.VMD_CONNECTION, _xmlNS).InnerText);

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
            image.setimage_group_id(OvfParser.GetImageGrupIdFromImageFile(node.Attributes.get("ovf:href").getValue()));
            // Default values:
            image.setactive(true);
            image.setimageStatus(ImageStatus.OK);
            image.setdescription(node.Attributes.get("ovf:description").getValue());
            _images.add(image);
        }
    }

    private int getResourceType(XmlNode node, VmDevice vmDevice, String resource) {
        if (node.SelectSingleNode(resource, _xmlNS) != null
                && !StringHelper.isNullOrEmpty(node.SelectSingleNode(resource, _xmlNS).InnerText)) {
            return Integer.valueOf(node.SelectSingleNode(resource, _xmlNS).InnerText);
        }
        return -1;
    }

    private void setDeviceByResource(XmlNode node, VmDevice vmDevice) {
        int resourceType = getResourceType(node, vmDevice, OvfProperties.VMD_RESOURCE_TYPE);
        int resourceSubType = getResourceType(node, vmDevice, OvfProperties.VMD_SUB_RESOURCE_TYPE);
        if (resourceSubType == -1) {
            // we need special handling for Monitor to define it as vnc or spice
            if (Integer.valueOf(OvfHardware.Monitor) == resourceType) {
                // get number of monitors from VirtualQuantity in OVF
                if (node.SelectSingleNode(OvfProperties.VMD_VIRTUAL_QUANTITY, _xmlNS) != null
                        && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_VIRTUAL_QUANTITY,
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
            } else {
                vmDevice.setDevice(VmDeviceType.getoVirtDevice(resourceType).getName());
            }
        } else if (Integer.valueOf(OvfHardware.Network) == resourceType) {
            // handle interfaces with different sub types : we have 0-3 as the VmInterfaceType enum
            boolean isKnownType = false;
            for (VmInterfaceType vmInterfaceType : VmInterfaceType.values()) {
                if ((Integer.valueOf(vmInterfaceType.getValue()) == resourceSubType)) {
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
}
