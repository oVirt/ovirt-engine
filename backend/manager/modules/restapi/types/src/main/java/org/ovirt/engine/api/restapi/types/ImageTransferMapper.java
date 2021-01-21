package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.model.ImageTransfer;
import org.ovirt.engine.api.model.ImageTransferDirection;
import org.ovirt.engine.api.model.ImageTransferPhase;
import org.ovirt.engine.api.model.ImageTransferTimeoutPolicy;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.storage.TimeoutPolicyType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;

public class ImageTransferMapper {
    @Mapping(from = ImageTransfer.class,
            to = org.ovirt.engine.core.common.businessentities.storage.ImageTransfer.class)
    public static org.ovirt.engine.core.common.businessentities.storage.ImageTransfer map(ImageTransfer model,
            org.ovirt.engine.core.common.businessentities.storage.ImageTransfer template) {

        org.ovirt.engine.core.common.businessentities.storage.ImageTransfer entity = template != null ? template :
                        new org.ovirt.engine.core.common.businessentities.storage.ImageTransfer();

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetHost() && model.getHost().isSetId()) {
            entity.setVdsId(GuidUtils.asGuid(model.getHost().getId()));
        }
        if (model.isSetImage() && model.getImage().isSetId()) {
            entity.setDiskId(GuidUtils.asGuid(model.getImage().getId()));
        }
        if (model.isSetFormat()) {
            entity.setImageFormat(map(model.getFormat(), null));
        }
        if (model.isSetBackup() && model.getBackup().isSetId()) {
            entity.setBackupId(GuidUtils.asGuid(model.getBackup().getId()));
        }
        if (model.isSetShallow()) {
            entity.setShallow(model.isShallow());
        }
        return entity;
    }

    @Mapping(from = org.ovirt.engine.core.common.businessentities.storage.ImageTransfer.class, to = ImageTransfer.class)
    public static ImageTransfer map(org.ovirt.engine.core.common.businessentities.storage.ImageTransfer entity, ImageTransfer template) {
        ImageTransfer model = template != null ? template : new ImageTransfer();

        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getVdsId() != null) {
            model.setHost(new Host());
            model.getHost().setId(entity.getVdsId().toString());
        }
        if (entity.getDiskId() != null) {
            model.setImage(new Image());
            model.getImage().setId(entity.getDiskId().toString());
        }
        if (entity.getProxyUri() != null && entity.getImagedTicketId() != null) {
            model.setProxyUrl(entity.getProxyURLForTransfer());
        }
        if (entity.getDaemonUri() != null && entity.getImagedTicketId() != null) {
            model.setTransferUrl(entity.getDaemonURLForTransfer());
        }
        if (entity.getPhase() != null) {
            model.setPhase(mapPhase(entity.getPhase()));
        }
        if (entity.getActive() != null) {
            model.setActive(entity.getActive());
        }
        if (entity.getType() != null) {
            model.setDirection(mapDirection(entity.getType()));
        }
        if (entity.getBytesSent() != null) {
            model.setTransferred(entity.getBytesSent());
        }
        if (entity.getClientInactivityTimeout() != null) {
            model.setInactivityTimeout(entity.getClientInactivityTimeout());
        }
        if (entity.getTimeoutPolicy() != null) {
            model.setTimeoutPolicy(mapTimeoutPolicy(entity.getTimeoutPolicy()));
        }
        if (entity.getImageFormat() != null) {
            model.setFormat(map(entity.getImageFormat(), null));
        }
        if (entity.isShallow() != null) {
            model.setShallow(entity.isShallow());
        }
        return model;
    }

    private static ImageTransferPhase mapPhase(org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase phase) {
        switch (phase) {
            case UNKNOWN:
                return ImageTransferPhase.UNKNOWN;
            case INITIALIZING:
                return ImageTransferPhase.INITIALIZING;
            case TRANSFERRING:
                return ImageTransferPhase.TRANSFERRING;
            case RESUMING:
                return ImageTransferPhase.RESUMING;
            case PAUSED_SYSTEM:
                return ImageTransferPhase.PAUSED_SYSTEM;
            case PAUSED_USER:
                return ImageTransferPhase.PAUSED_USER;
            case CANCELLED_SYSTEM:
                return ImageTransferPhase.CANCELLED_SYSTEM;
            case CANCELLED_USER:
                return ImageTransferPhase.CANCELLED_USER;
            case FINALIZING_SUCCESS:
                return ImageTransferPhase.FINALIZING_SUCCESS;
            case FINALIZING_FAILURE:
                return ImageTransferPhase.FINALIZING_FAILURE;
            case FINALIZING_CLEANUP:
                return ImageTransferPhase.FINALIZING_CLEANUP;
            case FINISHED_SUCCESS:
                return ImageTransferPhase.FINISHED_SUCCESS;
            case FINISHED_FAILURE:
                return ImageTransferPhase.FINISHED_FAILURE;
            case FINISHED_CLEANUP:
                return ImageTransferPhase.FINISHED_CLEANUP;
            default:
                throw new IllegalArgumentException("The value \"" + phase + "\" isn't a valid image transfer phase.");
        }
    }

    private static ImageTransferDirection mapDirection(TransferType type) {
        switch (type) {
        case Download:
            return ImageTransferDirection.DOWNLOAD;
        case Upload:
            return ImageTransferDirection.UPLOAD;
        default:
            return null;
        }
    }

    private static ImageTransferTimeoutPolicy mapTimeoutPolicy(TimeoutPolicyType type) {
        switch (type) {
            case CANCEL:
                return ImageTransferTimeoutPolicy.CANCEL;
            case PAUSE:
                return ImageTransferTimeoutPolicy.PAUSE;
            default:
                return ImageTransferTimeoutPolicy.LEGACY;
        }
    }

    @Mapping(from = DiskFormat.class, to = String.class)
    public static VolumeFormat map(DiskFormat diskFormat, VolumeFormat template) {
        switch (diskFormat) {
            case COW:
                return VolumeFormat.COW;
            case RAW:
                return VolumeFormat.RAW;
            default:
                return VolumeFormat.Unassigned;
        }
    }

    @Mapping(from = VolumeFormat.class, to = DiskFormat.class)
    public static DiskFormat map(VolumeFormat volumeFormat, DiskFormat template) {
        switch (volumeFormat) {
            case COW:
                return DiskFormat.COW;
            case RAW:
                return DiskFormat.RAW;
            default:
                return null;
        }
    }

}
