package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.model.Images;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.ImageResource;
import org.ovirt.engine.api.resource.ImagesResource;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;


public class BackendStorageDomainImagesResource
        extends AbstractBackendCollectionResource<Image, RepoImage>
        implements ImagesResource {

    Guid storageDomainId;

    public BackendStorageDomainImagesResource(Guid storageDomainId, String... subCollections) {
        super(Image.class, RepoImage.class);
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Images list() {
        GetImagesListParameters getImagesListParams = new GetImagesListParameters(storageDomainId, ImageFileType.All);
        getImagesListParams.setForceRefresh(true);
        return mapCollection(getBackendCollection(VdcQueryType.GetImagesList, getImagesListParams));
    }

    @Override
    @SingleEntityResource
    public ImageResource getDeviceSubResource(String id) {
        return inject(new BackendStorageDomainImageResource(id, this));
    }

    protected Guid getStorageDomainId() {
        return storageDomainId;
    }

    @Override
    protected Response performRemove(String id) {
        throw new UnsupportedOperationException(); // TODO: removal is not implemented yet
    }

    @Override
    protected Image doPopulate(Image model, RepoImage entity) {
        return model;
    }

    protected Images mapCollection(List<RepoImage> entities) {
        Images collection = new Images();
        for (RepoImage image : entities) {
            collection.getImages().add(addLinks(populate(map(image), image)));
        }
        return collection;
    }

    @Override
    protected Image addParents(Image image) {
        StorageDomain sd = new StorageDomain();
        sd.setId(storageDomainId.toString());
        image.setStorageDomain(sd);
        return super.addParents(image);
    }
}
