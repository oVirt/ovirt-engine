package org.ovirt.engine.core.utils.ovf;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public abstract class OvfOvirtWriter extends OvfWriter {
    protected FullEntityOvfData fullEntityOvfData;
    private OsRepository osRepository;

    public OvfOvirtWriter(FullEntityOvfData fullEntityOvfData,
            Version version,
            OsRepository osRepository) {
        super(fullEntityOvfData.getVmBase(),
                fullEntityOvfData.getDiskImages(),
                fullEntityOvfData.getLunDisks(),
                version);
        this.osRepository = osRepository;
        this.fullEntityOvfData = fullEntityOvfData;
    }

    @Override
    protected void writeFile(DiskImage image) {
        _writer.writeAttributeString(getOvfUri(), "href", OvfParser.createImageFile(image));
        _writer.writeAttributeString(getOvfUri(), "id", image.getImageId().toString());
        _writer.writeAttributeString(getOvfUri(), "size", String.valueOf(image.getActualSizeInBytes()));
        // these properties have to be written for forward compatibility
        // with previous versions of the engine
        _writer.writeAttributeString(getOvfUri(), "description", StringUtils.defaultString(image.getDescription()));
        _writer.writeAttributeString(getOvfUri(), "disk_storage_type", image.getDiskStorageType().name());
        _writer.writeAttributeString(getOvfUri(), "cinder_volume_type", StringUtils.defaultString(image.getCinderVolumeType()));
    }

    @Override
    protected void writeFileForLunDisk(LunDisk lun) {
        // Lun disk does not have image id, therefor the id will be preserved with the disk ID as identifier.
        _writer.writeAttributeString(getOvfUri(), "id", lun.getId().toString());
        _writer.writeAttributeString(getOvfUri(), "href", OvfParser.createLunFile(lun));
        _writer.writeAttributeString(getOvfUri(), "disk_storage_type", lun.getDiskStorageType().name());
    }

    @Override
    protected void writeHeader() {
        super.writeHeader();
        // Setting the OVF version according to ENGINE (in 2.2 , version was set to "0.9")
        _writer.writeAttributeString(getOvfUri(), "version", Config.getValue(ConfigValues.VdcVersion));
    }

    @Override
    public String getOvfUri() {
        // We add / at the end to support forward compatibility between different engine setups prior to 4.2
        return OVF_URI + "/";
    }

    @Override
    protected void writeOS() {
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(getOvfUri(), "id", vmBase.getId().toString());
        _writer.writeAttributeString(getOvfUri(), "required", "false");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":OperatingSystemSection_Type");
        _writer.writeElement("Info", "Guest Operating System");
        _writer.writeElement("Description", osRepository.getUniqueOsNames().get(vmBase.getOsId()));
        _writer.writeEndElement();
    }

    @Override
    protected void startHardwareSection() {
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
        _writer.writeAttributeString(getOvfUri(), "diskId", image.getImageId().toString());
        _writer.writeAttributeString(getOvfUri(), "size", String.valueOf(bytesToGigabyte(image.getSize())));
        _writer.writeAttributeString(getOvfUri(),
                "actual_size",
                String.valueOf(bytesToGigabyte(image.getActualSizeInBytes())));
        _writer.writeAttributeString(getOvfUri(), "vm_snapshot_id", (image.getVmSnapshotId() != null) ? image
                .getVmSnapshotId().toString() : "");
        writeDiskParentRef(image);
        _writer.writeAttributeString(getOvfUri(), "fileRef", OvfParser.createImageFile(image));
        _writer.writeAttributeString(getOvfUri(), "format", getVolumeImageFormat(image.getVolumeFormat()));
        _writer.writeAttributeString(getOvfUri(), "volume-format", image.getVolumeFormat().toString());
        _writer.writeAttributeString(getOvfUri(), "volume-type", image.getVolumeType().toString());
        _writer.writeAttributeString(getOvfUri(), "disk-interface", dve.getDiskInterface().toString());
        _writer.writeAttributeString(getOvfUri(), "read-only", String.valueOf(dve.isReadOnly()));
        _writer.writeAttributeString(getOvfUri(), "shareable", String.valueOf(image.isShareable()));
        _writer.writeAttributeString(getOvfUri(), "boot", String.valueOf(dve.isBoot()));
        _writer.writeAttributeString(getOvfUri(), "pass-discard", String.valueOf(dve.isPassDiscard()));
        if (image.getDiskAlias() != null) {
            _writer.writeAttributeString(getOvfUri(), "disk-alias", image.getDiskAlias());
        }
        if (image.getDiskDescription() != null) {
            _writer.writeAttributeString(getOvfUri(), "disk-description", image.getDiskDescription());
        }
        _writer.writeAttributeString(getOvfUri(),
                "wipe-after-delete",
                String.valueOf(image.isWipeAfterDelete()));
    }

    @Override
    protected void writeLunDisk(LunDisk lun) {
        // Lun disk does not have image id, therefor the id will be preserved with the disk ID as identifier.
        _writer.writeAttributeString(getOvfUri(), "diskId", lun.getId().toString());
        DiskVmElement dve = lun.getDiskVmElementForVm(vmBase.getId());
        if (lun.getDiskAlias() != null) {
            _writer.writeAttributeString(getOvfUri(), "disk-alias", lun.getDiskAlias());
        }
        if (lun.getDiskDescription() != null) {
            _writer.writeAttributeString(getOvfUri(), "disk-description", lun.getDiskDescription());
        }
        _writer.writeAttributeString(getOvfUri(), "pass-discard", String.valueOf(dve.isPassDiscard()));
        _writer.writeAttributeString(getOvfUri(), "fileRef", OvfParser.createLunFile(lun));
        _writer.writeAttributeString(getOvfUri(), "shareable", String.valueOf(lun.isShareable()));
        _writer.writeAttributeString(getOvfUri(), "boot", String.valueOf(dve.isBoot()));
        _writer.writeAttributeString(getOvfUri(), "disk-interface", dve.getDiskInterface().toString());
        _writer.writeAttributeString(getOvfUri(), "read-only", String.valueOf(dve.isReadOnly()));
        _writer.writeAttributeString(getOvfUri(), "scsi_reservation", String.valueOf(dve.isUsingScsiReservation()));
        _writer.writeAttributeString(getOvfUri(), "plugged", String.valueOf(dve.isPlugged()));
        _writer.writeAttributeString(getOvfUri(), LUN_ID, String.valueOf(lun.getLun().getLUNId()));
        if (lun.getLun().getLunConnections() != null) {
            for (StorageServerConnections conn : lun.getLun().getLunConnections()) {
                _writer.writeStartElement(LUN_CONNECTION);
                _writer.writeAttributeString(getOvfUri(), LUNS_CONNECTION, conn.getConnection());
                _writer.writeAttributeString(getOvfUri(), LUNS_IQN, conn.getIqn());
                _writer.writeAttributeString(getOvfUri(), LUNS_PORT, conn.getPort());
                _writer.writeAttributeString(XSI_URI, LUNS_STORAGE_TYPE, conn.getStorageType().name());
                _writer.writeAttributeString(XSI_URI, LUNS_PORTAL, conn.getPortal());
                _writer.writeEndElement();
            }
        }
    }

    private void writeDiskParentRef(DiskImage image) {
        if (image.getParentId().equals(Guid.Empty)) {
            _writer.writeAttributeString(getOvfUri(), "parentRef", "");
        } else {
            int i = 0;
            while (_images.get(i).getImageId().equals(image.getParentId())) {
                i++;
            }
            List<DiskImage> res = _images.subList(i, _images.size() - 1);

            if (res.size() > 0) {
                _writer.writeAttributeString(getOvfUri(), "parentRef", OvfParser.createImageFile(res.get(0)));
            } else {
                _writer.writeAttributeString(getOvfUri(), "parentRef", "");
            }
        }
    }

    @Override
    protected void startVirtualSystem() {
        _writer.writeStartElement("Content");
        _writer.writeAttributeString(getOvfUri(), "id", "out");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualSystem_Type");
    }

    @Override
    protected String getDriveHostResource(DiskImage image) {
        return OvfParser.createImageFile(image);
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(CLUSTER_NAME, fullEntityOvfData.getClusterName());
        writeUserData();
        writeVmExternalData();
    }

    private void writeUserData() {
        Set<DbUser> dbUsers = fullEntityOvfData.getDbUsers();
        if (dbUsers.isEmpty()) {
            logger.debug("There are no users with permissions on VM {} to write", vmBase.getId());
            return;
        }

        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:UserDomainsSection_Type");

        dbUsers.forEach(dbUser -> {
            _writer.writeStartElement(OvfProperties.USER);
            _writer.writeElement(OvfProperties.USER_DOMAIN, String.format("%s@%s", dbUser.getName(), dbUser.getDomain()));
            _writer.writeStartElement(OvfProperties.USER_ROLES);
            Set<String> roles = fullEntityOvfData.getUserToRoles().getOrDefault(dbUser.getLoginName(), Collections.emptySet());
            roles.forEach(role -> _writer.writeElement(OvfProperties.ROLE_NAME, role));

            // Close the <UserRoles> element
            _writer.writeEndElement();

            // Close the <User> element
            _writer.writeEndElement();
        });

        _writer.writeEndElement();
    }

    private void writeVmExternalData() {
        Map<VmExternalDataKind, String> vmExternalData = fullEntityOvfData.getVmExternalData();
        if (vmExternalData.isEmpty()) {
            return;
        }

        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":VmExternalDataSection_Type");
        vmExternalData.forEach((kind, data) -> {
                _writer.writeStartElement(OvfProperties.VM_EXTERNAL_DATA_ITEM);
                _writer.writeAttributeString(OvfProperties.VM_EXTERNAL_DATA_KIND, kind.getExternal());
                _writer.writeElement(OvfProperties.VM_EXTERNAL_DATA_CONTENT, data);
                _writer.writeEndElement();
        });
        _writer.writeEndElement();
    }

    @Override
    protected String adjustHardwareResourceType(String resourceType) {
        switch(resourceType) {
        case OvfHardware.Graphics:
            return OvfHardware.OVIRT_Graphics;
        case OvfHardware.Monitor:
            return OvfHardware.OVIRT_Monitor;
        default:
            return super.adjustHardwareResourceType(resourceType);
        }
    }

    @Override
    protected String getInstaceIdTag() {
        return "InstanceId";
    }

    protected void writeIntegerList(String label, List<Integer> list) {
        if (list != null && !list.isEmpty()) {
            String writeList = list.stream().map(i -> i.toString()).collect(Collectors.joining(","));
            _writer.writeElement(label, writeList);
        } else {
            _writer.writeElement(label, "");
        }
    }
}
