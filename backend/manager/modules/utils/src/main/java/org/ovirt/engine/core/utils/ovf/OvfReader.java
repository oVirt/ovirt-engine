package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
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
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;
    private static final int BYTES_IN_GB = 1024 * 1024 * 1024;
    public static final String EmptyName = "[Empty Name]";
    protected String name = EmptyName;
    private String version;

    public OvfReader(XmlDocument document, java.util.ArrayList<DiskImage> images) {
        _images = images;
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
        XmlNodeList list = _document.SelectNodes("//*/File", _xmlNS);
        for (XmlNode node : list) {
            DiskImage image = new DiskImage();
            image.setId(new Guid(node.Attributes.get("ovf:id").getValue()));
            image.setimage_group_id(OvfParser.GetImageGrupIdFromImageFile(node.Attributes.get("ovf:href").getValue()));
            // Default values:
            image.setactive(true);
            image.setimageStatus(ImageStatus.OK);
            image.setdescription(node.Attributes.get("ovf:description").getValue());
            _images.add(image);
        }
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
            // DiskImage image = null; //LINQ _images.FirstOrDefault(img =>
            // img.image_guid == guid);
            DiskImage image = LinqUtils.firstOrNull(_images, new Predicate<DiskImage>() {
                @Override
                public boolean eval(DiskImage diskImage) {
                    return diskImage.getId().equals(guid);
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
                    image.setdisk_interface(DiskInterface.valueOf(node.Attributes.get("ovf:disk-interface").getValue()));
                }
            }
            else {
                image.setdisk_interface(DiskInterface.IDE);
            }
            if (node.Attributes.get("ovf:disk-type") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:disk-type").getValue())) {
                    image.setdisk_type(DiskType.valueOf(node.Attributes.get("ovf:disk-type").getValue()));
                } else {
                    image.setdisk_type(DiskType.Unassigned);
                }
            }
            else {
                image.setdisk_type(DiskType.Unassigned);
            }
            if (node.Attributes.get("ovf:boot") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:boot").getValue())) {
                    image.setboot(Boolean.parseBoolean(node.Attributes.get("ovf:boot").getValue()));
                }
            }
            if (node.Attributes.get("ovf:wipe-after-delete") != null) {
                if (!StringHelper.isNullOrEmpty(node.Attributes.get("ovf:wipe-after-delete").getValue())) {
                    image.setwipe_after_delete(Boolean.parseBoolean(node.Attributes.get("ovf:wipe-after-delete")
                            .getValue()));
                }
            }
        }
    }

    @Override
    public void BuildVirtualSystem() {
        ReadGeneralData();
    }

    protected abstract void ReadOsSection(XmlNode section);

    protected abstract void ReadHardwareSection(XmlNode section);

    protected abstract void ReadGeneralData();
}
