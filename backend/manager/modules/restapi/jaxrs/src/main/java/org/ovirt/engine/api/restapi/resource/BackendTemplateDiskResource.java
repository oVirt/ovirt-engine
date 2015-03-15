package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDiskResource extends BackendReadOnlyDeviceResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements TemplateDiskResource {

    public BackendTemplateDiskResource(Guid guid,
                                       BackendTemplateDisksResource collection,
                                       String... subCollections) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class, guid, collection, subCollections);
    }

    @Override
    public Response copy(Action action) {
        validateParameters(action, "storageDomain.id|name");
        Guid storageDomainId = getStorageDomainId(action);
        Guid imageId = asGuid(getDisk().getImageId());
        MoveOrCopyImageGroupParameters params =
                new MoveOrCopyImageGroupParameters(imageId,
                        Guid.Empty,
                        storageDomainId,
                        ImageOperation.Copy);
        return doAction(VdcActionType.MoveOrCopyDisk, params, action);
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        return model;
    }

    protected Disk getDisk() {
        return performGet(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    @Override
    public Response doExport(Action action) {
        validateParameters(action, "storageDomain.id|name");
        return doAction(VdcActionType.ExportRepoImage,
                new ExportRepoImageParameters(guid, getStorageDomainId(action)), action);
    }

}
