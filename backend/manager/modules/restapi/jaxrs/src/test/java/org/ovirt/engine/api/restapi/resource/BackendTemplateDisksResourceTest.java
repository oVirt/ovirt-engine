package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DeviceResource;
import org.ovirt.engine.api.resource.ReadOnlyDeviceResource;

import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;

import org.ovirt.engine.core.common.queries.GetStorageDomainsByVmTemplateIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetVmTemplatesDisksParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendTemplateDisksResourceTest
        extends AbstractBackendDisksResourceTest<BackendTemplateDisksResource> {

    public BackendTemplateDisksResourceTest() {
        super(new BackendTemplateDisksResource(PARENT_ID,
                                               VdcQueryType.GetVmTemplatesDisks,
                                               new GetVmTemplatesDisksParameters(PARENT_ID)),
              VdcQueryType.GetVmTemplatesDisks,
              new GetVmTemplatesDisksParameters(PARENT_ID),
              "Id");
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        Object subResource = collection.getDeviceSubResource(GUIDS[0].toString());
        assertFalse(subResource instanceof DeviceResource);
        assertTrue(subResource instanceof ReadOnlyDeviceResource);
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getDeviceSubResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpEntityQueryExpectations(1, null);
        setUpGetStorageDomainsQueryExpectations(1);
        control.replay();
        collection.setUriInfo(uriInfo);
        List<Disk> disks = getCollection();
        for (Disk disk : disks) {
            assertNotNull(disk.getStorageDomains());
            List<StorageDomain> storageDomains = disk.getStorageDomains().getStorageDomains();
            assertEquals(storageDomains.size(), 2);
            assertEquals(storageDomains.get(0).getId(), GUIDS[2].toString());
            assertEquals(storageDomains.get(1).getId(), GUIDS[3].toString());
        }
        verifyCollection(disks);
    }

    @Test
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpEntityQueryExpectations(1, FAILURE);
        setUpGetStorageDomainsQueryExpectations(1);
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

    @Test
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(1, t);
        setUpGetStorageDomainsQueryExpectations(1);
        control.replay();
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(1, t);
        setUpGetStorageDomainsQueryExpectations(1);
        control.replay();
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }

    private int setUpGetStorageDomainsQueryExpectations(int times) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetStorageDomainsByVmTemplateId,
                    GetStorageDomainsByVmTemplateIdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { PARENT_ID },
                                         getStorageDomains(),
                                         null);
        }
        return times;
    }

    protected List<org.ovirt.engine.core.common.businessentities.StorageDomain> getStorageDomains() {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> storageDomains = new ArrayList<org.ovirt.engine.core.common.businessentities.StorageDomain>();
        org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        storageDomain.setId(GUIDS[2]);
        storageDomains.add(storageDomain);
        storageDomain = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        storageDomain.setId(GUIDS[3]);
        storageDomains.add(storageDomain);
        return storageDomains;
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveDisk,
                                           RemoveDiskParameters.class,
                                           new String[] { "EntityId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveByStorageDomain() throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveDisk,
                                           RemoveDiskParameters.class,
                                           new String[] { "EntityId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(GUIDS[0].toString());
        verifyRemove(collection.remove(GUIDS[0].toString(), action));
    }

    @Test
    public void testRemoveForced() throws Exception {
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveDisk,
                                           RemoveDiskParameters.class,
                                           new String[] { "EntityId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        Action action = new Action();
        action.setForce(true);
        verifyRemove(collection.remove(GUIDS[0].toString(), action));
    }

    private void setUpGetEntityExpectations(int times) {
        for(int i=0;i<times;i++){
            setUpEntityQueryExpectations(VdcQueryType.GetVmTemplatesDisks,
                    GetVmTemplatesDisksParameters.class,
                    new String[] { "Id" },
                    new Object[] { PARENT_ID },
                    getEntityList());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveDisk,
                                           RemoveDiskParameters.class,
                                           new String[] { "EntityId" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }
}
