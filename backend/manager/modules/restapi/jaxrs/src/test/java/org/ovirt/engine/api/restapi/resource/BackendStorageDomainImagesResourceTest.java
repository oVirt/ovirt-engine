package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainImagesResourceTest extends AbstractBackendCollectionResourceTest<Image, RepoImage, BackendStorageDomainImagesResource> {

    public BackendStorageDomainImagesResourceTest() {
        super(new BackendStorageDomainImagesResource(GUIDS[3]), null, null);
    }

    @Override
    protected List<Image> getCollection() {
        return collection.list().getImages();
    }

    @Override
    protected RepoImage getEntity(int index) {
        RepoImage entity = new RepoImage();
        entity.setRepoImageId(GUIDS[index].toString());
        entity.setFileType(ImageFileType.Disk);
        entity.setRepoImageName("RepoImage " + entity.getRepoImageId());
        return entity;
    }

    @Test
    @Override
    @Disabled
    public void testQuery() {
    }

    @Test
    @Override
    public void testList() throws Exception {
        collection.setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(null);

        verifyCollection(getCollection());
    }

    @Test
    @Override
    public void testListFailure() {
        collection.setUriInfo(setUpUriExpectations(null));

        setUpEntityQueryExpectations(FAILURE);


        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Test
    @Override
    public void testListCrash() {
        collection.setUriInfo(setUpUriExpectations(null));

        setUpEntityQueryExpectations(FAILURE);

        verifyFault(
                assertThrows(WebApplicationException.class, this::getCollection),
                BACKEND_FAILED_SERVER_LOCALE,
                new RuntimeException(FAILURE));
    }

    @Test
    @Override
    public void testListCrashClientLocale() {
        locales.add(CLIENT_LOCALE);
        collection.setUriInfo(setUpUriExpectations(null));

        setUpEntityQueryExpectations(FAILURE);

        verifyFault(
                assertThrows(WebApplicationException.class, this::getCollection),
                BACKEND_FAILED_CLIENT_LOCALE,
                new RuntimeException(FAILURE));
    }

    @Override
    protected void verifyFault(WebApplicationException wae, String reason, Throwable t) {
        assertEquals(BAD_REQUEST, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertNotNull(fault.getDetail());
        assertTrue(fault.getDetail().contains(t.getMessage()), "expected detail to include: " + t.getMessage());
    }

    protected void setUpEntityQueryExpectations(String failure) {
        List<RepoImage> entities = new ArrayList<>();

        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }

        setUpEntityQueryExpectations( QueryType.GetImagesList, GetImagesListParameters.class,
                new String[]{"StorageDomainId", "ImageType"}, new Object[]{GUIDS[3], ImageFileType.All},
                entities, failure);
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
