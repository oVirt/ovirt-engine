package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toList;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainResource.isIsoDomain;

import java.util.List;

import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Files;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.FileResource;
import org.ovirt.engine.api.resource.FilesResource;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendFilesResource
    extends AbstractBackendCollectionResource<File, String>
    implements FilesResource {

    protected String storageDomainId;

    public BackendFilesResource(String storageDomainId) {
        super(File.class, String.class);
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Files list() {
        if (isIsoDomain(getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                                  VdcQueryType.GetStorageDomainById,
                                  new IdQueryParameters(asGuid(storageDomainId)),
                                  "storage_domain"))) {
            return mapCollection(listFiles());
        } else {
            return notFound(Files.class);
        }
    }

    @Override
    public FileResource getFileResource(String id) {
        return new BackendFileResource(id, this);
    }

    @Override
    protected File addParents(File file) {
        file.setStorageDomain(new StorageDomain());
        file.getStorageDomain().setId(storageDomainId);
        return file;
    }

    protected Files mapCollection(List<String> entities) {
        Files files = new Files();
        for (String name : entities) {
            files.getFiles().add(addLinks(map(name)));
        }
        return files;
    }

    public File lookupFile(String name) {
        if (listFiles().contains(name)) {
            return addLinks(map(name));
        } else {
            return notFound();
        }
    }

    protected List<String> listFiles() {
        GetImagesListParameters queryParams = new GetImagesListParameters(asGuid(storageDomainId), ImageFileType.All);
        queryParams.setForceRefresh(true);

        List<RepoImage> files = getBackendCollection(RepoImage.class, VdcQueryType.GetImagesList,
                                    queryParams);
        return files.stream().map(RepoImage::getRepoImageId).collect(toList());
    }
}
