package org.ovirt.engine.core.utils.ovf;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Version;

public abstract class OvfOvaWriter extends OvfWriter {

    protected Map<VmExternalDataKind, String> vmExternalData;
    private OsRepository osRepository;

    public OvfOvaWriter(VmBase vmBase,
            List<DiskImage> images,
            Version version,
            Map<VmExternalDataKind, String> vmExternalData,
            OsRepository osRepository) {
        super(vmBase, images, Collections.emptyList(), version);
        this.vmExternalData = vmExternalData;
        this.osRepository = osRepository;
    }

    @Override
    protected void writeHeader() {
        super.writeHeader();
        _writer.writeDefaultNamespace(getOvfUri());
        _writer.setPrefix(OVIRT_PREFIX, OVIRT_URI);
        _writer.writeNamespace(OVIRT_PREFIX, OVIRT_URI);
    }

    @Override
    public String getOvfUri() {
        return OVF_URI;
    }

    @Override
    protected void writeReferenceData() {
        super.writeReferenceData();
        int tpmDataSize = getVmExternalDataSize(VmExternalDataKind.TPM);
        if (tpmDataSize > 0) {
            _writer.writeStartElement("File");
            writeExternalDataFile("tpm.dat", tpmDataSize);
            _writer.writeEndElement();
        }
        int nvramDataSize = getVmExternalDataSize(VmExternalDataKind.NVRAM);
        if (nvramDataSize > 0) {
            _writer.writeStartElement("File");
            writeExternalDataFile("nvram.dat", nvramDataSize);
            _writer.writeEndElement();
        }
    }

    private int getVmExternalDataSize(VmExternalDataKind kind) {
        return vmExternalData != null ? vmExternalData.getOrDefault(kind, "").length() : 0;
    }

    private void writeExternalDataFile(String fileName, int size) {
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "href", fileName);
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "id", fileName);
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "size", String.valueOf(size));
    }

    @Override
    protected void writeFile(DiskImage image) {
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "href", image.getImageId().toString());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "id", image.getImageId().toString());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "size", String.valueOf(image.getActualSizeInBytes()));
    }

    @Override
    protected void writeDisk(DiskImage image) {
        DiskVmElement dve = image.getDiskVmElementForVm(vmBase.getId());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "diskId", image.getId().toString());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "capacity",
                String.valueOf(convertBytesToGigabyte(image.getSize())));
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "capacityAllocationUnits", "byte * 2^30");
        // When we generate an OVF representation to the client usage, the client needs to fill the populatedSize.
        // In that case the actualSizeInBytes set to negative in order to distinguish it.
        if (image.getActualSizeInBytes() >= 0) {
            _writer.writeAttributeString(
                    OVF_PREFIX, getOvfUri(), "populatedSize", String.valueOf(image.getActualSizeInBytes()));
        }
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "parentRef", "");
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "fileRef", image.getImageId().toString());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "format", getVolumeImageFormat(image.getVolumeFormat()));
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "volume-format", image.getVolumeFormat().toString());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "volume-type", image.getVolumeType().toString());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "disk-interface", dve.getDiskInterface().toString());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "boot", String.valueOf(dve.isBoot()));
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "pass-discard", String.valueOf(dve.isPassDiscard()));
        if (image.getDiskAlias() != null) {
            _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "disk-alias", image.getDiskAlias());
        }
        if (image.getDiskDescription() != null) {
            _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "disk-description", image.getDiskDescription());
        }
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "wipe-after-delete",
                String.valueOf(image.isWipeAfterDelete()));
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "description",
                StringUtils.defaultString(image.getDescription()));
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "disk_storage_type", image.getDiskStorageType().name());
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "cinder_volume_type",
                StringUtils.defaultString(image.getCinderVolumeType()));
    }

    @Override
    protected void writeTpmHostResource() {
        if (getVmExternalDataSize(VmExternalDataKind.TPM) != 0) {
            _writer.writeElement(RASD_URI, "HostResource", getFileHostResource("tpm.dat"));
        }
    }

    @Override
    protected void writeOS() {
        _writer.writeStartElement("OperatingSystemSection");
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "id", Integer.toString(mapOsId(vmBase.getOsId())));
        _writer.writeAttributeString(OVIRT_URI, "id", Integer.toString(vmBase.getOsId()));
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "required", "false");
        _writer.writeElement("Info", "Guest Operating System");
        _writer.writeElement("Description", osRepository.getOsName(vmBase.getOsId()));
        _writer.writeEndElement();
    }

    protected int mapOsId(int ovfOsId) {
        switch (ovfOsId) {
        case 1: // Windows XP
            return 67;
        case 1002:
        case 2002:
        case 5: // Other Linux
            return 36;
        case 3: // Windows 2003
            return 69;
        case 4: // Windows 2008
            return 76;
        case 10: // Windows 2003x64
            return 70;
        case 11: // Windows 7
        case 12: // Windows 7x64
            return 105;
        case 16: // Windows 2008x64
            return 77;
        case 17: // Windows 2008R2x64
            return 103;
        case 20: // Windows 8
            return 114;
        case 21: // Windows 8x64
            return 115;
        case 23: // Windows 2012x64
            return 113;
        case 25: // Windows 2012R2x64
            return 116;
        case 26: // Windows 10
            return 120;
        case 27: // Windows 10x64
            return 121;
        case 29: // Windows 2016x64
            return 117;
        case 31: // Windows 2019x64
            return 122;
        case 35: // RedHat CoreOS
            return 125;
        case 1004:
        case 1193: // Suse
        case 2004:
            return 83;
        case 1252:
        case 1253:
        case 1254:
        case 1255:
        case 1256:
        case 1257:
        case 1005:
        case 2005:
            return 94; // Ubuntu
        case 1300: // Debian 7
        case 1301: // Debian 9
            return 96;
        case 1500: // FreeBSD
            return 42;
        case 1501: // FreeBSDx64
            return 78;
        case 7:
        case 8:
        case 9:
        case 18:
            return 79; // RHEL
        case 15:
        case 14:
        case 13:
        case 19:
        case 24:
        case 28:
        case 33:
        case 34:
        case 1003:
        case 1006:
        case 1007:
        case 1008:
        case 1009:
        case 2003:
            return 80; // RHEL x64
        case 1001:
        case 2001:
        default:
            return 1; // Other
        }
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
        _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "id", vmBase.getId().toString());
    }

    protected String getFileHostResource(String name) {
        return String.format("ovf:/file/%s", name);
    }

    @Override
    protected String getDriveHostResource(DiskImage image) {
        return String.format("ovf:/disk/%s", image.getId());
    }

    @Override
    protected String getInstaceIdTag() {
        return "InstanceID";
    }
}
