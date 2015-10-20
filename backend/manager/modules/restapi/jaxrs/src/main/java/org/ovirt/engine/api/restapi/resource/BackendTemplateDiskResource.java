package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDiskResource
        extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements TemplateDiskResource {

    private Guid templateId;

    public BackendTemplateDiskResource(String diskId, Guid templateId) {
        super(diskId, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.templateId = templateId;
    }

    @Override
    public Disk get() {
        List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities = getBackendCollection(
            org.ovirt.engine.core.common.businessentities.storage.Disk.class,
            VdcQueryType.GetVmTemplatesDisks,
            new IdQueryParameters(templateId)
        );
        for (org.ovirt.engine.core.common.businessentities.storage.Disk entity : entities) {
            if (Objects.equals(entity.getId(), guid)) {
                return addLinks(populate(map(entity), entity));
            }
        }
        return notFound();
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    @Override
    public Disk addParents(Disk entity) {
        entity.setTemplate(new Template());
        entity.getTemplate().setId(templateId.toString());
        return entity;
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

    protected Disk getDisk() {
        return performGet(VdcQueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    @Override
    public Response doExport(Action action) {
        validateParameters(action, "storageDomain.id|name");
        return doAction(VdcActionType.ExportRepoImage,
                new ExportRepoImageParameters(guid, getStorageDomainId(action)), action);
    }

    @Override
    public Response remove(Action action) {
        get(); // will throw 404 if entity not found.
        RemoveDiskParameters params = new RemoveDiskParameters(asGuid(id));
        if (action.isSetForce()) {
            params.setForceDelete(action.isForce());
        }
        if (action.isSetStorageDomain() && action.getStorageDomain().isSetId()) {
            params.setStorageDomainId(asGuid(action.getStorageDomain().getId()));
        }
        return performAction(VdcActionType.RemoveDisk, params);
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveDisk, new RemoveDiskParameters(guid));
    }
}
