package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.ImageTransfer;
import org.ovirt.engine.api.model.ImageTransferDirection;
import org.ovirt.engine.api.model.ImageTransfers;
import org.ovirt.engine.api.resource.ImageTransferResource;
import org.ovirt.engine.api.resource.ImageTransfersResource;
import org.ovirt.engine.api.restapi.types.ImageTransferMapper;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.businessentities.storage.TimeoutPolicyType;
import org.ovirt.engine.core.common.businessentities.storage.TransferClientType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendImageTransfersResource
        extends AbstractBackendCollectionResource<ImageTransfer,
        org.ovirt.engine.core.common.businessentities.storage.ImageTransfer> implements ImageTransfersResource {

    protected BackendImageTransfersResource() {
        super(ImageTransfer.class, org.ovirt.engine.core.common.businessentities.storage.ImageTransfer.class);
    }

    /**
     * Adding using an Image entity - for 4.1 backwards compatibility
     */
    @Deprecated
    @Override
    public Response addForImage(ImageTransfer imageTransfer) {
        TransferDiskImageParameters params = new TransferDiskImageParameters();
        params.setImageGroupID(GuidUtils.asGuid(imageTransfer.getImage().getId()));
        return performCreate(imageTransfer, params);
    }

    @Override
    public Response addForDisk(ImageTransfer imageTransfer) {
        TransferDiskImageParameters params = new TransferDiskImageParameters();
        params.setImageGroupID(GuidUtils.asGuid(imageTransfer.getDisk().getId()));
        return performCreate(imageTransfer, params);
    }

    @Override
    public Response addForSnapshot(ImageTransfer imageTransfer) {
        TransferDiskImageParameters params = new TransferDiskImageParameters();
        params.setImageId(GuidUtils.asGuid(imageTransfer.getSnapshot().getId()));
        return performCreate(imageTransfer, params);
    }

    private Response performCreate(ImageTransfer imageTransfer, TransferDiskImageParameters params) {
        updateTransferType(imageTransfer, params);
        if (imageTransfer.isSetHost() && imageTransfer.getHost().isSetId()) {
            params.setVdsId(Guid.createGuidFromString(imageTransfer.getHost().getId()));
        }
        if (imageTransfer.isSetInactivityTimeout()) {
            params.setClientInactivityTimeout(imageTransfer.getInactivityTimeout());
        }
        if (imageTransfer.isSetTimeoutPolicy()) {
            params.setTimeoutPolicyType(TimeoutPolicyType.forString(imageTransfer.getTimeoutPolicy().value()));
        }
        if (imageTransfer.isSetFormat()) {
            params.setVolumeFormat(ImageTransferMapper.map(imageTransfer.getFormat(), null));
        }
        if (imageTransfer.isSetBackup() && imageTransfer.getBackup().isSetId()) {
            params.setBackupId(Guid.createGuidFromString(imageTransfer.getBackup().getId()));
        }
        if (imageTransfer.isSetShallow()) {
            params.setShallow(imageTransfer.isShallow());
        }
        params.setTransferClientType(TransferClientType.TRANSFER_VIA_API);
        return performCreate(ActionType.TransferDiskImage, params, new QueryIdResolver<Guid>(QueryType.GetImageTransferById,
                IdQueryParameters.class));
    }

    private void updateTransferType(ImageTransfer imageTransfer, TransferDiskImageParameters params) {
        if (imageTransfer.isSetDirection() && imageTransfer.getDirection() == ImageTransferDirection.DOWNLOAD) {
            // Upload is the default direction, so we set the transfer type only if download was explicitly specified.
            params.setTransferType(TransferType.Download);
        }
    }

    @Override
    public ImageTransfers list() {
        return mapCollection(getBackendCollection(QueryType.GetAllImageTransfers, new QueryParametersBase()));
    }

    @Override
    public ImageTransferResource getImageTransferResource(String id) {
        return inject(new BackendImageTransferResource(id));
    }

    private ImageTransfers mapCollection(List<org.ovirt.engine.core.common.businessentities.storage.ImageTransfer> imageTransfers) {
        ImageTransfers mappedImageTransfers = new ImageTransfers();
        for (org.ovirt.engine.core.common.businessentities.storage.ImageTransfer imageTransfer : imageTransfers) {
            mappedImageTransfers.getImageTransfers().add(addLinks(populate(map(imageTransfer), imageTransfer)));
        }

        return mappedImageTransfers;
    }

    @Override
    protected ImageTransfer addLinks(ImageTransfer model,
            Class<? extends BaseResource> suggestedParent,
            String... subCollectionMembersToExclude) {
        super.addLinks(model, suggestedParent, subCollectionMembersToExclude);
        if (model.isSetImage()) {
            model.getImage().unsetLinks();
        }

        return model;
    }
}
