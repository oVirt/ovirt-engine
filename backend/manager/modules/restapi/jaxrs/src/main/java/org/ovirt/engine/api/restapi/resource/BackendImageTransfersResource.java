package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.ImageTransfer;
import org.ovirt.engine.api.model.ImageTransferDirection;
import org.ovirt.engine.api.model.ImageTransfers;
import org.ovirt.engine.api.resource.ImageTransferResource;
import org.ovirt.engine.api.resource.ImageTransfersResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendImageTransfersResource
        extends AbstractBackendCollectionResource<ImageTransfer,
        org.ovirt.engine.core.common.businessentities.storage.ImageTransfer> implements ImageTransfersResource {

    protected BackendImageTransfersResource() {
        super(ImageTransfer.class, org.ovirt.engine.core.common.businessentities.storage.ImageTransfer.class);
    }

    @Override
    public Response add(ImageTransfer imageTransfer) {
        TransferDiskImageParameters params = new TransferDiskImageParameters();
        if (imageTransfer.isSetDirection() && imageTransfer.getDirection() == ImageTransferDirection.DOWNLOAD) {
            // Upload is the default direction, so we set the transfer type only if download was explicitly specified.
            params.setTransferType(TransferType.Download);
        }
        params.setImageId(GuidUtils.asGuid(imageTransfer.getImage().getId()));
        return performCreate(VdcActionType.TransferDiskImage, params, new QueryIdResolver<Guid>(VdcQueryType.GetImageTransferById,
                IdQueryParameters.class));
    }

    @Override
    public ImageTransfers list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllImageTransfers, new VdcQueryParametersBase()));
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
