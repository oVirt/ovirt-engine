package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.toList;
import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainResource.isIsoDomain;

import java.util.List;

import org.ovirt.engine.api.model.File;
import org.ovirt.engine.api.model.Files;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.FileResource;
import org.ovirt.engine.api.resource.FilesResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendFilesResource
    extends AbstractBackendCollectionResource<File, String>
    implements FilesResource {

    private static final String FORCE_REFRESH = "refresh";

    protected String storageDomainId;

    public BackendFilesResource(String storageDomainId) {
        super(File.class, String.class);
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Files list() {
        if (isIsoDomain(getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                                  QueryType.GetStorageDomainById,
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
        Boolean forceRefresh = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, FORCE_REFRESH, null, null);
        queryParams.setForceRefresh(forceRefresh);
        List<RepoImage> files = getBackendCollection(RepoImage.class, QueryType.GetImagesList,
                                    queryParams);
        return files.stream().map(RepoImage::getRepoImageId).collect(toList());
    }
}
