package org.ovirt.engine.core.utils.ovf;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
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
    protected void writeFileForLunDisk(LunDisk lun) {
        // Lun disk does not have image id, therefor the id will be preserved with the disk ID as identifier.
        _writer.writeAttributeString(OVF_URI, "id", lun.getId().toString());
        _writer.writeAttributeString(OVF_URI, "href", OvfParser.createLunFile(lun));
        _writer.writeAttributeString(OVF_URI, "disk_storage_type", lun.getDiskStorageType().name());
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
        _writer.writeAttributeString(OVF_URI, "read-only", String.valueOf(dve.isReadOnly()));
        _writer.writeAttributeString(OVF_URI, "shareable", String.valueOf(image.isShareable()));
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

    @Override
    protected void writeLunDisk(LunDisk lun) {
        // Lun disk does not have image id, therefor the id will be preserved with the disk ID as identifier.
        _writer.writeAttributeString(OVF_URI, "diskId", lun.getId().toString());
        DiskVmElement dve = lun.getDiskVmElementForVm(vmBase.getId());
        if (lun.getDiskAlias() != null) {
            _writer.writeAttributeString(OVF_URI, "disk-alias", lun.getDiskAlias().toString());
        }
        if (lun.getDiskDescription() != null) {
            _writer.writeAttributeString(OVF_URI, "disk-description", lun.getDiskDescription().toString());
        }
        if (FeatureSupported.passDiscardSupported(version)) {
            _writer.writeAttributeString(OVF_URI, "pass-discard", String.valueOf(dve.isPassDiscard()));
        }
        _writer.writeAttributeString(OVF_URI, "fileRef", OvfParser.createLunFile(lun));
        _writer.writeAttributeString(OVF_URI, "shareable", String.valueOf(lun.isShareable()));
        _writer.writeAttributeString(OVF_URI, "boot", String.valueOf(dve.isBoot()));
        _writer.writeAttributeString(OVF_URI, "disk-interface", dve.getDiskInterface().toString());
        _writer.writeAttributeString(OVF_URI, "read-only", String.valueOf(dve.isReadOnly()));
        _writer.writeAttributeString(OVF_URI, "scsi_reservation", String.valueOf(dve.isUsingScsiReservation()));
        _writer.writeAttributeString(OVF_URI, "plugged", String.valueOf(dve.isPlugged()));
        _writer.writeAttributeString(OVF_URI, LUN_ID, String.valueOf(lun.getLun().getLUNId()));
        if (lun.getLun().getLunConnections() != null) {
            for (StorageServerConnections conn : lun.getLun().getLunConnections()) {
                _writer.writeStartElement(LUN_CONNECTION);
                _writer.writeAttributeString(OVF_URI, LUNS_CONNECTION, conn.getConnection());
                _writer.writeAttributeString(OVF_URI, LUNS_IQN, conn.getIqn());
                _writer.writeAttributeString(OVF_URI, LUNS_PORT, conn.getPort());
                _writer.writeAttributeString(XSI_URI, LUNS_STORAGE_TYPE, conn.getStorageType().name());
                _writer.writeAttributeString(XSI_URI, LUNS_PORTAL, conn.getPortal());
                _writer.writeEndElement();
            }
        }
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

    @Override
    protected String getDriveHostResource(DiskImage image) {
        return OvfParser.createImageFile(image);
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(CLUSTER_NAME, fullEntityOvfData.getClusterName());
        writeUserData();
    }

    private void writeUserData() {
        Set<DbUser> dbUsers = fullEntityOvfData.getDbUsers();
        if (dbUsers.isEmpty()) {
            logger.warn("There are no users with permissions on VM {} to write", vmBase.getId());
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

}
