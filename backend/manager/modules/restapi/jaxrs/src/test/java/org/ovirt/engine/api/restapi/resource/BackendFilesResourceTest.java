package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;


import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.File;
import org.ovirt.engine.core.common.businessentities.RepoFileMetaData;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;

import org.ovirt.engine.core.common.queries.GetAllIsoImagesListParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendFilesResourceTest
    extends AbstractBackendCollectionResourceTest<File, String, BackendFilesResource> {

    public BackendFilesResourceTest() {
        super(new BackendFilesResource(GUIDS[0].toString()), null, null);
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testGet() throws Exception {
        BackendFileResource resource = new BackendFileResource(NAMES[0].toString(), collection);
        collection.setUriInfo(setUpUriExpectations(null));
        setUpQueryExpectations("", null);
        verifyModel(resource.get(), 0);
    }

    @Test
    public void testBadGet() throws Exception {
        BackendFileResource resource = new BackendFileResource("foo", collection);
        collection.setUriInfo(setUpUriExpectations(null));
        setUpQueryExpectations("", null);
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        setUpQueryExpectations("");

        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListNonIso() throws Exception {
        try {
            UriInfo uriInfo = setUpUriExpectations(null);

            setupGetStorageDomainExpectations(StorageDomainType.Data);
            control.replay();

            collection.setUriInfo(uriInfo);
            verifyCollection(getCollection());

            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Override
    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(VdcQueryType.GetAllImagesList,
                GetAllIsoImagesListParameters.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                setUpFiles(),
                AbstractBackendCollectionResourceTest.FAILURE);
        collection.setUriInfo(uriInfo);
        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        control.replay();
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }

    @Test
    @Override
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(VdcQueryType.GetAllImagesList,
                GetAllIsoImagesListParameters.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                setUpFiles(),
                AbstractBackendCollectionResourceTest.FAILURE);
        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        control.replay();
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    @Override
    protected void verifyFault(WebApplicationException wae, String reason, Throwable t) {
        assertEquals(BAD_REQUEST, wae.getResponse().getStatus());
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
        Fault fault = (Fault) wae.getResponse().getEntity();
        assertEquals(reason, fault.getReason());
        assertNotNull(fault.getDetail());
        assertTrue("expected detail to include: " + t.getMessage(),
                   fault.getDetail().indexOf(t.getMessage()) != -1);
    }

    @Test
    @Override
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpEntityQueryExpectations(VdcQueryType.GetAllImagesList,
                GetAllIsoImagesListParameters.class,
                new String[] { "StorageDomainId" },
                new Object[] { GUIDS[0] },
                setUpFiles(),
                AbstractBackendCollectionResourceTest.FAILURE);
        setupGetStorageDomainExpectations(StorageDomainType.ISO);
        control.replay();
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    private void setupGetStorageDomainExpectations(org.ovirt.engine.core.common.businessentities.StorageDomainType type) {
        setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainById,
                                     StorageDomainQueryParametersBase.class,
                                     new String[] { "StorageDomainId" },
                                     new Object[] { GUIDS[0] },
                                     getStorageDomain(GUIDS[0], type));
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain(Guid id, StorageDomainType type) {
        org.ovirt.engine.core.common.businessentities.StorageDomain sd =  new org.ovirt.engine.core.common.businessentities.StorageDomain();
        sd.setId(id);
        sd.setstorage_domain_type(type);
        return sd;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetAllImagesList,
                                     GetAllIsoImagesListParameters.class,
                                     new String[] { "StorageDomainId" },
                                     new Object[] { GUIDS[0] },
                                     setUpFiles(),
                                     failure);
        control.replay();
    }

    private List<RepoFileMetaData> setUpFiles() {
        List<RepoFileMetaData> isos = new ArrayList<RepoFileMetaData>();
        for (int i = 0; i < NAMES.length; i++) {
            RepoFileMetaData file = new RepoFileMetaData();
            file.setRepoFileName(NAMES[i]);
            isos.add(file);
        }
        return isos;
    }

    @Override
    protected String getEntity(int index) {
        return NAMES[index];
    }

    @Override
    protected void verifyModel(File model, int index) {
        assertEquals(NAMES[index].toString(), model.getId());
        assertEquals(NAMES[index].toString(), model.getName());
        assertNotNull(model.getStorageDomain());
        assertEquals(GUIDS[0].toString(), model.getStorageDomain().getId());
        verifyLinks(model);
    }

    @Override
    protected List<File> getCollection() {
        return collection.list().getFiles();
    }

    @Override
    protected void verifyCollection(List<File> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(NAMES.length, collection.size());
        for (int i = 0; i < NAMES.length; i++) {
            verifyModel(collection.get(i), i);
        }
    }
}
