package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageFileType;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.queries.GetImagesListParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

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
    @Ignore
    public void testQuery() throws Exception {
    }

    @Test
    @Override
    public void testList() throws Exception {
        collection.setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(null);
        control.replay();

        verifyCollection(getCollection());
    }

    @Test
    @Override
    public void testListFailure() throws Exception {
        collection.setUriInfo(setUpUriExpectations(null));

        setUpEntityQueryExpectations(FAILURE);

        control.replay();

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    @Test
    @Override
    public void testListCrash() throws Exception {
        collection.setUriInfo(setUpUriExpectations(null));

        setUpEntityQueryExpectations(FAILURE);

        control.replay();

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, new RuntimeException(FAILURE));
        }
    }

    @Test
    @Override
    public void testListCrashClientLocale() throws Exception {
        locales.add(CLIENT_LOCALE);
        collection.setUriInfo(setUpUriExpectations(null));

        setUpEntityQueryExpectations(FAILURE);

        control.replay();

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, new RuntimeException(FAILURE));
        } finally {
            locales.clear();
        }
    }

    @Override
    protected void verifyFault(WebApplicationException wae, String reason, Throwable t) {
        assertEquals(BAD_REQUEST, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertNotNull(fault.getDetail());
        assertTrue("expected detail to include: " + t.getMessage(), fault.getDetail().contains(t.getMessage()));
    }

    protected void setUpEntityQueryExpectations(String failure) {
        List<RepoImage> entities = new ArrayList<>();

        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }

        setUpEntityQueryExpectations( VdcQueryType.GetImagesList, GetImagesListParameters.class,
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
