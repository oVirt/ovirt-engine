package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ImageResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendStorageDomainImageResource
        extends AbstractBackendActionableResource<Image, RepoImage>
        implements ImageResource {

    private final BackendStorageDomainImagesResource parent;

    protected BackendStorageDomainImageResource(String id, BackendStorageDomainImagesResource parent) {
        super(id, Image.class, RepoImage.class);
        this.parent = parent;
    }

    public Guid getStorageDomainId() {
        return parent.getStorageDomainId();
    }

    @Override
    public Response doImport(Action action) {
        validateParameters(action, "storageDomain.id|name");

        ImportRepoImageParameters importParameters = new ImportRepoImageParameters();

        importParameters.setSourceRepoImageId(id);
        importParameters.setSourceStorageDomainId(getStorageDomainId());

        importParameters.setStoragePoolId(getDataCenterId(getStorageDomainId(action)));
        importParameters.setStorageDomainId(getStorageDomainId(action));

        if (action.isSetImportAsTemplate()) {
            if (action.isImportAsTemplate()) {
                validateParameters(action, "cluster.id|name");
                importParameters.setClusterId(getClusterId(action));

                if (action.isSetTemplate() && action.getTemplate().isSetName()) {
                    importParameters.setTemplateName(action.getTemplate().getName());
                }
            }
            importParameters.setImportAsTemplate(action.isImportAsTemplate());
        }

        if (action.isSetDisk()) {
            if (action.getDisk().isSetName()) {
                importParameters.setDiskAlias(action.getDisk().getName());
            }
            if (action.getDisk().isSetAlias()) {
                importParameters.setDiskAlias(action.getDisk().getAlias());
            }
        }

        EntityResolver resolver = new SimpleIdResolver(
                Disk.class,
                org.ovirt.engine.core.common.businessentities.storage.Disk.class,
                QueryType.GetDiskByDiskId,
                IdQueryParameters.class
        );
        return doAction(ActionType.ImportRepoImage, importParameters, action, resolver);
    }

    @Override
    public ActionResource getActionResource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    public Image get() {
        return performGet(QueryType.GetImageById, new GetImageByIdParameters(getStorageDomainId(), id));
    }

    @Override
    protected Image addParents(Image image) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(getStorageDomainId().toString());
        image.setStorageDomain(storageDomain);
        return image;
    }
}
