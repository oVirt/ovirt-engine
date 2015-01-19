package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.ImageResource;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendStorageDomainImageResource
        extends AbstractBackendActionableResource<Image, org.ovirt.engine.core.common.businessentities.RepoImage>
        implements ImageResource {

    final private BackendStorageDomainImagesResource parent;

    protected BackendStorageDomainImageResource(String id, BackendStorageDomainImagesResource parent) {
        super(id, Image.class, org.ovirt.engine.core.common.businessentities.RepoImage.class);
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

        return doAction(VdcActionType.ImportRepoImage, importParameters, action);
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    protected Image doPopulate(Image model, RepoImage entity) {
        return model;
    }

    @Override
    public Image get() {
        return performGet(VdcQueryType.GetImageById, new GetImageByIdParameters(getStorageDomainId(), id));
    }
}
