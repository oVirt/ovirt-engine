package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public abstract class OvfOvirtWriter extends OvfWriter {

    private OsRepository osRepository;

    public OvfOvirtWriter(VmBase vmBase, List<DiskImage> images, Version version, OsRepository osRepository) {
        super(vmBase, images, version);
        this.osRepository = osRepository;
    }

    @Override
    protected void writeFile(DiskImage image) {
        _writer.writeAttributeString(OVF_URI, "href", OvfParser.createImageFile(image));
        _writer.writeAttributeString(OVF_URI, "id", image.getImageId().toString());
        _writer.writeAttributeString(OVF_URI, "size", String.valueOf(image.getActualSizeInBytes()));
        // these properties have to be written for forward compatibility
        // with previous versions of the engine
        _writer.writeAttributeString(OVF_URI, "description", StringUtils.defaultString(image.getDescription()));
        _writer.writeAttributeString(OVF_URI, "disk_storage_type", image.getDiskStorageType().name());
        _writer.writeAttributeString(OVF_URI, "cinder_volume_type", StringUtils.defaultString(image.getCinderVolumeType()));
    }

    @Override
    protected void writeHeader() {
        super.writeHeader();
        // Setting the OVF version according to ENGINE (in 2.2 , version was set to "0.9")
        _writer.writeAttributeString(OVF_URI, "version", Config.getValue(ConfigValues.VdcVersion));
    }

    @Override
    protected void writeOS() {
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(OVF_URI, "id", vmBase.getId().toString());
        _writer.writeAttributeString(OVF_URI, "required", "false");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":OperatingSystemSection_Type");
        _writer.writeElement("Info", "Guest Operating System");
        _writer.writeElement("Description", osRepository.getUniqueOsNames().get(vmBase.getOsId()));
        _writer.writeEndElement();
    }

    @Override
    protected void startHardware() {
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualHardwareSection_Type");
    }

    @Override
    protected void startDiskSection() {
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":DiskSection_Type");
    }

    @Override
    protected void writeDisk(DiskImage image) {
        DiskVmElement dve = image.getDiskVmElementForVm(vmBase.getId());
        _writer.writeAttributeString(OVF_URI, "diskId", image.getImageId().toString());
        _writer.writeAttributeString(OVF_URI, "size", String.valueOf(bytesToGigabyte(image.getSize())));
        _writer.writeAttributeString(OVF_URI,
                "actual_size",
                String.valueOf(bytesToGigabyte(image.getActualSizeInBytes())));
        _writer.writeAttributeString(OVF_URI, "vm_snapshot_id", (image.getVmSnapshotId() != null) ? image
                .getVmSnapshotId().toString() : "");
        writeDiskParentRef(image);
        _writer.writeAttributeString(OVF_URI, "fileRef", OvfParser.createImageFile(image));
        _writer.writeAttributeString(OVF_URI, "format", getVolumeImageFormat(image.getVolumeFormat()));
        _writer.writeAttributeString(OVF_URI, "volume-format", image.getVolumeFormat().toString());
        _writer.writeAttributeString(OVF_URI, "volume-type", image.getVolumeType().toString());
        _writer.writeAttributeString(OVF_URI, "disk-interface", dve.getDiskInterface().toString());
        _writer.writeAttributeString(OVF_URI, "boot", String.valueOf(dve.isBoot()));
        if (FeatureSupported.passDiscardSupported(version)) {
            _writer.writeAttributeString(OVF_URI, "pass-discard", String.valueOf(dve.isPassDiscard()));
        }
        if (image.getDiskAlias() != null) {
            _writer.writeAttributeString(OVF_URI, "disk-alias", image.getDiskAlias());
        }
        if (image.getDiskDescription() != null) {
            _writer.writeAttributeString(OVF_URI, "disk-description", image.getDiskDescription());
        }
        _writer.writeAttributeString(OVF_URI,
                "wipe-after-delete",
                String.valueOf(image.isWipeAfterDelete()));
    }

    private void writeDiskParentRef(DiskImage image) {
        if (image.getParentId().equals(Guid.Empty)) {
            _writer.writeAttributeString(OVF_URI, "parentRef", "");
        } else {
            int i = 0;
            while (_images.get(i).getImageId().equals(image.getParentId())) {
                i++;
            }
            List<DiskImage> res = _images.subList(i, _images.size() - 1);

            if (res.size() > 0) {
                _writer.writeAttributeString(OVF_URI, "parentRef", OvfParser.createImageFile(res.get(0)));
            } else {
                _writer.writeAttributeString(OVF_URI, "parentRef", "");
            }
        }
    }

    @Override
    protected void startVirtualSystem() {
        _writer.writeStartElement("Content");
        _writer.writeAttributeString(OVF_URI, "id", "out");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualSystem_Type");
    }
}
