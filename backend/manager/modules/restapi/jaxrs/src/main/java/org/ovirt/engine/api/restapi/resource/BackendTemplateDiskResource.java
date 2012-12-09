package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDiskResource extends BackendReadOnlyDeviceResource<Disk, Disks, org.ovirt.engine.core.common.businessentities.Disk>
        implements TemplateDiskResource {

    public BackendTemplateDiskResource(Guid guid,
                                       BackendTemplateDisksResource collection,
                                       String... subCollections) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.Disk.class, guid, collection, subCollections);
    }

    @Override
    public Response copy(Action action) {
        validateParameters(action, "storageDomain.id|name");
        MoveOrCopyImageGroupParameters params =
                new MoveOrCopyImageGroupParameters(asGuid(get().getImageId()),
                                                   Guid.Empty,
                                                   getStorageDomainId(action),
                                                   ImageOperation.Copy);
        return doAction(VdcActionType.MoveOrCopyDisk, params, action);
    }

    @Override
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return model;
    }
}
