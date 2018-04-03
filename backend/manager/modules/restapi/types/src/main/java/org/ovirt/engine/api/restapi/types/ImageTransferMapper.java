package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.model.ImageTransfer;
import org.ovirt.engine.api.model.ImageTransferDirection;
import org.ovirt.engine.api.model.ImageTransferPhase;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;

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
        if (entity.getSignedTicket() != null) {
            model.setSignedTicket(entity.getSignedTicket());
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
            case CANCELLED:
                return ImageTransferPhase.CANCELLED;
            case FINALIZING_SUCCESS:
                return ImageTransferPhase.FINALIZING_SUCCESS;
            case FINALIZING_FAILURE:
                return ImageTransferPhase.FINALIZING_FAILURE;
            case FINISHED_SUCCESS:
                return ImageTransferPhase.FINISHED_SUCCESS;
            case FINISHED_FAILURE:
                return ImageTransferPhase.FINISHED_FAILURE;
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
}
