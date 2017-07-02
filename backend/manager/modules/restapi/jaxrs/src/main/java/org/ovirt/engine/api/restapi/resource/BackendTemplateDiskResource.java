package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.TemplateDiskResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateDiskResource
        extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements TemplateDiskResource {

    public static final String FORCE = "force";
    public static final String STORAGE_DOMAIN = "storage_domain";

    private Guid templateId;

    public BackendTemplateDiskResource(String diskId, Guid templateId) {
        super(diskId, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.templateId = templateId;
    }

    @Override
    public Disk get() {
        List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities = getBackendCollection(
            org.ovirt.engine.core.common.businessentities.storage.Disk.class,
            QueryType.GetVmTemplatesDisks,
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
        Disk disk = getDisk();
        Guid imageId = asGuid(disk.getImageId());
        MoveOrCopyImageGroupParameters params =
                new MoveOrCopyImageGroupParameters(imageId,
                        Guid.Empty,
                        storageDomainId,
                        ImageOperation.Copy);
        params.setImageGroupID(asGuid(disk.getId()));
        return doAction(ActionType.MoveOrCopyDisk, params, action);
    }

    protected Disk getDisk() {
        return performGet(QueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }

    @Override
    public Response export(Action action) {
        validateParameters(action, "storageDomain.id|name");
        return doAction(ActionType.ExportRepoImage,
                new ExportRepoImageParameters(guid, getStorageDomainId(action)), action);
    }

    @Override
    public Response remove() {
        get(); // will throw 404 if entity not found.
        RemoveDiskParameters params = new RemoveDiskParameters(asGuid(id));
        boolean force = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, FORCE, true, false);
        if (force) {
            params.setForceDelete(force);
        }
        String storageDomain = ParametersHelper.getParameter(httpHeaders, uriInfo, STORAGE_DOMAIN);
        if (storageDomain != null) {
            params.setStorageDomainId(asGuid(storageDomain));
        }
        return performAction(ActionType.RemoveDisk, params);
    }
}
