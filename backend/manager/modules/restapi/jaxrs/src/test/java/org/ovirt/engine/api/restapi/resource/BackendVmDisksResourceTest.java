package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.VmDiskResource;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDisksResourceTest
        extends AbstractBackendDisksResourceTest<BackendVmDisksResource> {

    private static final Guid DISK_ID = GUIDS[0];

    public BackendVmDisksResourceTest() {
        super(new BackendVmDisksResource(PARENT_ID,
                                       VdcQueryType.GetAllDisksByVmId,
                                       new GetAllDisksByVmIdParameters(PARENT_ID)),
              VdcQueryType.GetAllDisksByVmId,
              new GetAllDisksByVmIdParameters(PARENT_ID),
              "VmId");
    }

    @Test
    public void testListIncludeStatistics() throws Exception {
        try {
            accepts.add("application/xml; detail=statistics");
            setUriInfo(setUpUriExpectations(null));
            setUpQueryExpectations("");

            List<Disk> disks = getCollection();
            assertTrue(disks.get(0).isSetStatistics());
            verifyCollection(disks);
        } finally {
            accepts.clear();
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveDisk,
                                           RemoveDiskParameters.class,
                                           new String[] { "EntityId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    private void setUpGetEntityExpectations() {
        setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                GetAllDisksByVmIdParameters.class,
                new String[] { "VmId" },
                new Object[] { PARENT_ID },
                getEntityList());
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
        setUpGetEntityExpectations();
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

    @Test
    public void testAddAsyncPending() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testAddAsyncInProgress() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testAddAsyncFinished() throws Exception {
        doTestAddAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    @Test
    public void testAttachDisk() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpActionExpectations (VdcActionType.AttachDiskToVm,
                AttachDettachVmDiskParameters.class,
                new String[] { "VmId", "EntityId" },
                new Object[] { PARENT_ID, DISK_ID },
                true,
                true);
        Disk model = getModel(0);
        model.setId(DISK_ID.toString()); //means this is an existing disk --> attach
        model.setSize(1024 * 1024L);
        Response response = collection.add(model);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDetachDisk() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.DetachDiskFromVm,
                                           AttachDettachVmDiskParameters.class,
                                           new String[] { "VmId", "EntityId" },
                                           new Object[] { PARENT_ID, DISK_ID },
                                           true,
                                           true));
        Action action = new Action();
        action.setDetach(true);
        verifyRemove(collection.remove(DISK_ID.toString(), action));
    }

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddDisk,
                                  AddDiskParameters.class,
                                  new String[] { "VmId" },
                                  new Object[] { PARENT_ID },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[3]),
                                  asList(new AsyncTaskStatus(asyncStatus)),
                                  VdcQueryType.GetAllDisksByVmId,
                                  GetAllDisksByVmIdParameters.class,
                                  new String[] { "VmId" },
                                  new Object[] { PARENT_ID },
                                  asList(getEntity(0)));
        Disk model = getModel(0);
        model.setSize(1024 * 1024L);

        Response response = collection.add(model);
        assertEquals(202, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk) response.getEntity(), 0);
        Disk created = (Disk)response.getEntity();
        assertNotNull(created.getCreationStatus());
        assertEquals(creationStatus.value(), created.getCreationStatus().getState());
    }

    @Test
    public void testAddDisk() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                                     GetAllDisksByVmIdParameters.class,
                                     new String[] { "VmId" },
                                     new Object[] { PARENT_ID },
                                     asList(getEntity(0)));
        setUpCreationExpectations(VdcActionType.AddDisk,
                                  AddDiskParameters.class,
                                  new String[] { "VmId" },
                                  new Object[] { PARENT_ID },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[3]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetAllDisksByVmId,
                                  GetAllDisksByVmIdParameters.class,
                                  new String[] { "VmId" },
                                  new Object[] { PARENT_ID },
                                  asList(getEntity(0)));
        Disk model = getModel(0);
        model.setSize(1024 * 1024L);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddDiskWithinStorageDomain() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(VdcQueryType.GetAllDisksByVmId,
                                     GetAllDisksByVmIdParameters.class,
                                     new String[] { "VmId" },
                                     new Object[] { PARENT_ID },
                                     asList(getEntity(0)));
        setUpCreationExpectations(VdcActionType.AddDisk,
                                  AddDiskParameters.class,
                                  new String[] { "VmId", "StorageDomainId" },
                                  new Object[] { PARENT_ID, GUIDS[3] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  asList(GUIDS[3]),
                                  asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                                  VdcQueryType.GetAllDisksByVmId,
                                  GetAllDisksByVmIdParameters.class,
                                  new String[] { "VmId" },
                                  new Object[] { PARENT_ID },
                                  asList(getEntity(0)));
        Disk model = getModel(0);
        model.setStorageDomains(new StorageDomains());
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[3].toString());
        model.getStorageDomains().getStorageDomains().add(storageDomain);
        model.setSize(1024 * 1024L);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddDiskCantDo() throws Exception {
        doTestBadAddDisk(false, true, CANT_DO);
    }

    @Test
    public void testAddDiskFailure() throws Exception {
        doTestBadAddDisk(true, false, FAILURE);
    }

    private void doTestBadAddDisk(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddDisk,
                                           AddDiskParameters.class,
                                           new String[] { "VmId" },
                                           new Object[] { PARENT_ID },
                                           canDo,
                                           success));
        Disk model = getModel(0);
        model.setSize(1024 * 1024L);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Disk model = new Disk();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Disk", "add", "size");
        }
    }

    @Test
    public void testSubResourceLocator() throws Exception {
        control.replay();
        assertTrue(collection.getDeviceSubResource(GUIDS[0].toString()) instanceof VmDiskResource);
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
}
