package org.ovirt.engine.core.utils.ovf;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;

public abstract class OvfOvaWriter extends OvfWriter {

    private OsRepository osRepository;

    public OvfOvaWriter(VmBase vmBase, List<DiskImage> images, Version version, OsRepository osRepository) {
        super(vmBase, images, Collections.EMPTY_LIST, version);
        this.osRepository = osRepository;
    }

    @Override
    protected void writeHeader() {
        super.writeHeader();
        _writer.writeDefaultNamespace(OVF_URI);
        _writer.setPrefix(OVIRT_PREFIX, OVIRT_URI);
        _writer.writeNamespace(OVIRT_PREFIX, OVIRT_URI);
    }

    @Override
    protected void writeFile(DiskImage image) {
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "href", image.getImageId().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "id", image.getImageId().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "size", String.valueOf(image.getActualSizeInBytes()));
    }

    @Override
    protected void writeDisk(DiskImage image) {
        DiskVmElement dve = image.getDiskVmElementForVm(vmBase.getId());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "diskId", image.getId().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "capacity",
                String.valueOf(convertBytesToGigabyte(image.getSize())));
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "capacityAllocationUnits", "byte * 2^30");
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "populatedSize",
                String.valueOf(image.getActualSizeInBytes()));
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "parentRef", "");
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "fileRef", image.getImageId().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "format", getVolumeImageFormat(image.getVolumeFormat()));
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "volume-format", image.getVolumeFormat().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "volume-type", image.getVolumeType().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "disk-interface", dve.getDiskInterface().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "boot", String.valueOf(dve.isBoot()));
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "pass-discard", String.valueOf(dve.isPassDiscard()));
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "disk-alias", image.getDiskAlias());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "disk-description", image.getDiskDescription());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "wipe-after-delete",
                String.valueOf(image.isWipeAfterDelete()));
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "description",
                StringUtils.defaultString(image.getDescription()));
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "disk_storage_type", image.getDiskStorageType().name());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "cinder_volume_type",
                StringUtils.defaultString(image.getCinderVolumeType()));
    }

    @Override
    protected void writeOS() {
        _writer.writeStartElement("OperatingSystemSection");
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "id", vmBase.getId().toString());
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "required", "false");
        _writer.writeElement("Info", "Guest Operating System");
        _writer.writeElement("Description", osRepository.getUniqueOsNames().get(vmBase.getOsId()));
        _writer.writeEndElement();
    }

    @Override
    protected void startHardwareSection() {
        _writer.writeStartElement("VirtualHardwareSection");
    }

    @Override
    protected void startDiskSection() {
        _writer.writeStartElement("DiskSection");
    }

    @Override
    protected void startVirtualSystem() {
        _writer.writeStartElement("VirtualSystem");
        _writer.writeAttributeString(OVF_PREFIX, OVF_URI, "id", vmBase.getId().toString());
    }

    @Override
    protected String getDriveHostResource(DiskImage image) {
        return String.format("ovf:disk/%s", image.getImageId());
    }
}
