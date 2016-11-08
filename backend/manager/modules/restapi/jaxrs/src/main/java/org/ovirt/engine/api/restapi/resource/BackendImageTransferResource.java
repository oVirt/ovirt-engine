package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.ImageTransfer;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ImageTransferResource;
import org.ovirt.engine.core.common.action.TransferImageStatusParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendImageTransferResource extends
        AbstractBackendSubResource<ImageTransfer,
                org.ovirt.engine.core.common.businessentities.storage.ImageTransfer> implements ImageTransferResource {

    public BackendImageTransferResource(String id) {
        super(id, ImageTransfer.class,
                org.ovirt.engine.core.common.businessentities.storage.ImageTransfer.class);
    }

    @Override
    public
    ImageTransfer get() {
        return performGet(VdcQueryType.GetImageTransferById, new IdQueryParameters(guid));
    }

    @Override
    public Response extend(Action action) {
        // For just keeping the upload alive, all we need is to refresh the entity querying.
        return performAction(VdcActionType.TransferImageStatus, prepareStatusParams(null));
    }

    @Override
    public Response pause(Action action) {
        return performAction(VdcActionType.TransferImageStatus,
                prepareStatusParams(ImageTransferPhase.PAUSED_USER));
    }

    @Override
    public Response resume(Action action) {
        return performAction(VdcActionType.TransferImageStatus,
                prepareStatusParams(ImageTransferPhase.RESUMING));
    }

    @Override
    public Response doFinalize(Action action) {
        return performAction(VdcActionType.TransferImageStatus,
                prepareStatusParams(ImageTransferPhase.FINALIZING_SUCCESS));
    }

    private TransferImageStatusParameters prepareStatusParams(ImageTransferPhase phase) {
        org.ovirt.engine.core.common.businessentities.storage.ImageTransfer updates =
                new org.ovirt.engine.core.common.businessentities.storage.ImageTransfer(guid);
        updates.setPhase(phase);

        TransferImageStatusParameters params = new TransferImageStatusParameters();
        params.setTransferImageCommandId(guid);
        params.setUpdates(updates);
        return params;
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }
}
