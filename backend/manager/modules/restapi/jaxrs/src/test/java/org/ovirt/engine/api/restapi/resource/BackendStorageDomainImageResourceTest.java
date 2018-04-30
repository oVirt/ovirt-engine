package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImageByIdParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainImageResourceTest extends AbstractBackendSubResourceTest<Image, RepoImage, BackendStorageDomainImageResource> {

    protected static final Guid DOMAIN_ID = GUIDS[0];
    protected static final Guid IMAGE_ID = GUIDS[1];
    protected static final Guid STORAGE_POOL_ID = GUIDS[2];
    protected static final Guid DESTINATION_DOMAIN_ID = GUIDS[3];

    public BackendStorageDomainImageResourceTest() {
        super(new BackendStorageDomainImageResource(IMAGE_ID.toString(),
                new BackendStorageDomainImagesResource(DOMAIN_ID)));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetImageById, GetImageByIdParameters.class,
                new String[]{"StorageDomainId", "RepoImageId"}, new Object[]{DOMAIN_ID, IMAGE_ID.toString()},
                getEntity(1));

        Image image = resource.get();
        verifyModelSpecific(image, 1);
        verifyLinks(image);
    }

    @Test
    public void testImport() {
        setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId, IdQueryParameters.class,
                new String[]{"Id"}, new Object[]{DESTINATION_DOMAIN_ID}, getStoragePoolList());

        setUriInfo(setUpActionExpectations(ActionType.ImportRepoImage, ImportRepoImageParameters.class,
                new String[]{"SourceRepoImageId", "SourceStorageDomainId", "StoragePoolId", "StorageDomainId"},
                new Object[]{IMAGE_ID.toString(), DOMAIN_ID, STORAGE_POOL_ID, DESTINATION_DOMAIN_ID},
                true, true, null, null, true));

        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(DESTINATION_DOMAIN_ID.toString());

        verifyActionResponse(resource.doImport(action), "storagedomains/" + DOMAIN_ID + "/images/" + IMAGE_ID, false);
    }

    public static List<StoragePool> getStoragePoolList() {
        return new ArrayList<StoragePool>() {
            private static final long serialVersionUID = 4817230014440543623L;
            {
                StoragePool storagePool = new StoragePool();
                storagePool.setId(STORAGE_POOL_ID);
                add(storagePool);
            }
        };
    }

    @Override
    protected RepoImage getEntity(int index) {
        RepoImage entity = new RepoImage();
        entity.setRepoImageId(GUIDS[index].toString());
        entity.setFileType(ImageFileType.Disk);
        entity.setRepoImageName("RepoImage " + entity.getRepoImageId());
        return entity;
    }

    @Override
    protected void verifyModel(Image model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Image model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
    }

}
