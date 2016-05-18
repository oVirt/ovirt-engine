/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.HostStorage;
import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.model.LogicalUnits;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmDisksResourceTest
        extends AbstractBackendCollectionResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendVmDisksResource> {

    private static final String ISCSI_SERVER_ADDRESS = "1.1.1.1";
    private static final Guid VM_ID = GUIDS[1];
    private static final Guid DISK_ID = GUIDS[0];
    private static final int ISCSI_SERVER_CONNECTION_PORT = 4567;
    private static final String ISCSI_SERVER_TARGET = "iqn.1986-03.com.sun:02:ori01";

    public BackendVmDisksResourceTest() {
        super(new BackendVmDisksResource(VM_ID), null, null);
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }

    protected void setUpEntityQueryExpectations(int times) throws Exception {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetAllDisksByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getEntityList(),
                failure
            );
        }
    }

    protected List<org.ovirt.engine.core.common.businessentities.storage.Disk> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        return setUpEntityExpectations(control.createMock(DiskImage.class), index);
    }

    static org.ovirt.engine.core.common.businessentities.storage.Disk setUpEntityExpectations(DiskImage entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getVmSnapshotId()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getVolumeFormat()).andReturn(VolumeFormat.RAW).anyTimes();
        expect(entity.getImageStatus()).andReturn(ImageStatus.OK).anyTimes();
        expect(entity.getVolumeType()).andReturn(VolumeType.Sparse).anyTimes();
        expect(entity.isShareable()).andReturn(false).anyTimes();
        expect(entity.getPropagateErrors()).andReturn(PropagateErrors.On).anyTimes();
        expect(entity.getDiskStorageType()).andReturn(DiskStorageType.IMAGE).anyTimes();
        expect(entity.getImageId()).andReturn(GUIDS[1]).anyTimes();
        expect(entity.getReadOnly()).andReturn(true).anyTimes();
        ArrayList<Guid> sdIds = new ArrayList<>();
        sdIds.add(Guid.Empty);
        expect(entity.getStorageIds()).andReturn(sdIds).anyTimes();
        return setUpStatisticalEntityExpectations(entity);
    }

    static org.ovirt.engine.core.common.businessentities.storage.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        expect(entity.getReadRate()).andReturn(1).anyTimes();
        expect(entity.getWriteRate()).andReturn(2).anyTimes();
        expect(entity.getReadLatency()).andReturn(3.0).anyTimes();
        expect(entity.getWriteLatency()).andReturn(4.0).anyTimes();
        expect(entity.getFlushLatency()).andReturn(5.0).anyTimes();
        return entity;
    }

    @Override
    protected List<Disk> getCollection() {
        return collection.list().getDisks();
    }

    static Disk getModel() {
        Disk model = new Disk();
        model.setFormat(DiskFormat.COW);
        model.setSparse(true);
        model.setShareable(false);
        model.setPropagateErrors(true);
        model.setStorageDomains(new StorageDomains());
        model.getStorageDomains().getStorageDomains().add(new StorageDomain());
        model.getStorageDomains().getStorageDomains().get(0).setId(GUIDS[2].toString());
        model.setProvisionedSize(1000000000L);
        return model;
    }

    @Override
    protected void verifyModel(Disk model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Disk model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSparse());
        assertTrue(model.isPropagateErrors());
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
        setUpCreationExpectations(
            VdcActionType.AttachDiskToVm,
            AttachDetachVmDiskParameters.class,
            new String[] { "VmId", "EntityInfo", },
            new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID) },
            true,
            true,
            null,
            null,
            null,
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        Disk model = getModel();
        model.setId(DISK_ID.toString()); //means this is an existing disk --> attach
        model.setProvisionedSize(1024 * 1024L);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testAttachDiskSnapshot() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Guid snapshotId = Guid.newGuid();
        Disk model = getModel();
        model.setSnapshot(new Snapshot());
        model.getSnapshot().setId(snapshotId.toString());
        model.setId(DISK_ID.toString()); //means this is an existing disk --> attach
        setUpCreationExpectations(
            VdcActionType.AttachDiskToVm,
            AttachDetachVmDiskParameters.class,
            new String[] { "VmId", "EntityInfo", "SnapshotId" },
            new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID), snapshotId },
            true,
            true,
            null,
            null,
            null,
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
    }

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUpCreationExpectations(
            VdcActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId" },
            new Object[] { VM_ID },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(asyncStatus)),
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        Disk model = getModel();
        model.setProvisionedSize(1024 * 1024L);

        Response response = collection.add(model);
        assertEquals(202, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk) response.getEntity(), 0);
        Disk created = (Disk)response.getEntity();
        assertNotNull(created.getCreationStatus());
        assertEquals(creationStatus.value(), created.getCreationStatus());
    }

    @Test
    public void testAddDiskWithJobId() throws Exception {

        Disk model = getModel();

        setUriInfo(setUpBasicUriExpectations());

        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.JOB_ID_CONSTRAINT,
                true,
                GUIDS[1].toString(),
                collection.getUriInfo(),
                false));

        setUpGetDiskExpectations();
        setCommonExpectations(model);
        Response response = collection.add(getModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddDiskWithStepId() throws Exception {

        Disk model = getModel();

        setUriInfo(setUpBasicUriExpectations());

        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.STEP_ID_CONSTRAINT,
                true,
                GUIDS[1].toString(),
                collection.getUriInfo(),
                false));

        setUpGetDiskExpectations();
        setCommonExpectations(model);
        Response response = collection.add(getModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    private void setUpGetDiskExpectations() {
        setUpEntityQueryExpectations(
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
    }

    private void setCommonExpectations(Disk model) {
        setUpHttpHeaderExpectations("Expect", "201-created");
        /*
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllDisksByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            asList(getEntity(0))
        );
        */
        setUpEntityQueryExpectations(
            VdcQueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUpCreationExpectations(
            VdcActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId", "StorageDomainId" },
            new Object[] { VM_ID, GUIDS[2] },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        model.setProvisionedSize(1024 * 1024L);
    }

    @Test
    public void testAddDisk() throws Exception {
        testAddDiskImpl(getModel());
    }

    private void testAddDiskImpl(Disk model) {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetDiskExpectations();
        setCommonExpectations(model);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddDiskIdentifyStorageDomainByName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpGetDiskExpectations();
        int times = 2;
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                VdcQueryType.GetAllStorageDomains,
                VdcQueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getStorageDomains()
            );
        }
        setUpEntityQueryExpectations(
            VdcQueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUpCreationExpectations(
            VdcActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId", "StorageDomainId" },
            new Object[] { VM_ID, GUIDS[2] },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        Disk model = getModel();
        model.getStorageDomains().getStorageDomains().get(0).setId(null);
        model.getStorageDomains().getStorageDomains().get(0).setName("Storage_Domain_1");
        model.setProvisionedSize(1024 * 1024L);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    private List<org.ovirt.engine.core.common.businessentities.StorageDomain> getStorageDomains() {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> sds = new LinkedList<>();
        sds.add(getStorageDomain(GUIDS[2]));
        return sds;
    }

    private org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain(Guid guid) {
        org.ovirt.engine.core.common.businessentities.StorageDomain sd = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        sd.setStorageName("Storage_Domain_1");
        sd.setId(guid);
        return sd;
    }

    @Test
    public void testAddDiskWithinStorageDomain() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpGetDiskExpectations();
        setUpEntityQueryExpectations(
            VdcQueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[3] },
            getStorageDomain(GUIDS[3])
        );
        setUpCreationExpectations(
            VdcActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId", "StorageDomainId" },
            new Object[] { VM_ID, GUIDS[3] },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
            VdcQueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        Disk model = getModel();
        model.setStorageDomains(new StorageDomains());
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(GUIDS[3].toString());
        model.getStorageDomains().getStorageDomains().add(storageDomain);
        model.setProvisionedSize(1024 * 1024L);

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

    private void doTestBadAddDisk(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.AddDisk,
                AddDiskParameters.class,
                new String[] { "VmId" },
                new Object[] { VM_ID },
                valid,
                success
            )
        );
        Disk model = getModel();
        model.setProvisionedSize(1024 * 1024L);

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
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            // Because of extra frame offset used current method name in test, while in real world used "add" method name
            verifyIncompleteException(wae, "Disk", "testAddIncompleteParameters", "provisionedSize|size", "format");
        }
    }

    @Test
    public void testAddIncompleteParameters2() throws Exception {
        Disk model = getModel();
        model.setProvisionedSize(null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            // Because of extra frame offset used current method name in test, while in real world used "add" method name
            verifyIncompleteException(wae, "Disk", "testAddIncompleteParameters2", "provisionedSize|size");
        }
    }

    @Test
    public void testAddLunDiskMissingType() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().setType(null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            // Because of extra frame offset used current method name in test, while in real world used "add" method name
            verifyIncompleteException(wae, "HostStorage", "testAddLunDiskMissingType", "type");
        }
    }

    @Test
    public void testAddLunDiskMissingId() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setId(null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            // Because of extra frame offset used current method name in test, while in real world used "add" method name
            verifyIncompleteException(wae, "LogicalUnit", "testAddLunDiskMissingId", "id");
        }
    }

    @Test
    public void testAddIscsiLunDiskIncompleteParametersConnectionAddress() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setAddress(null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            // Because of extra frame offset used current method name in test, while in real world used "add" method name
            verifyIncompleteException(wae, "LogicalUnit", "testAddIscsiLunDiskIncompleteParametersConnectionAddress", "address");
        }
    }

    @Test
    public void testAddIscsiLunDiskIncompleteParametersConnectionTarget() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setTarget(null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            // Because of extra frame offset used current method name in test, while in real world used "add" method name
            verifyIncompleteException(wae, "LogicalUnit", "testAddIscsiLunDiskIncompleteParametersConnectionTarget", "target");
        }
    }

    @Test
    public void testAddIscsiLunDiskIncompleteParametersConnectionPort() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setPort(null);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            // Because of extra frame offset used current method name in test, while in real world used "add" method name
            verifyIncompleteException(wae, "LogicalUnit", "testAddIscsiLunDiskIncompleteParametersConnectionPort", "port");
        }
    }

    private Disk createIscsiLunDisk() {
        Disk model = getModel();
        model.setLunStorage(new HostStorage());
        model.getLunStorage().setType(StorageType.ISCSI);
        model.getLunStorage().setLogicalUnits(new LogicalUnits());
        model.getLunStorage().getLogicalUnits().getLogicalUnits().add(new LogicalUnit());
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setId(GUIDS[0].toString());
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setAddress(ISCSI_SERVER_ADDRESS);
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setTarget(ISCSI_SERVER_TARGET);
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setPort(ISCSI_SERVER_CONNECTION_PORT);
        model.setProvisionedSize(null);
        return model;
    }

    @Test
    /**
     * This test checks that addition of LUN-Disk is successful. There is no real difference in the
     * implementation of adding regular disk and adding a lun-disk; in both cases the disk entity
     * is mapped and passed to the Backend, and Backend infers the type of disk from the entity and creates it.
     *
     * So what this test actually checks is that it's OK for the user not to specify size|provisionedSize
     * when creating a LUN-Disk
     */
    public void testAddLunDisk() {
        Disk model = createIscsiLunDisk();
        testAddDiskImpl(model);
    }

    @Test
    public void testSubResourceLocatorBadGuid() throws Exception {
        control.replay();
        try {
            collection.getDiskResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }
}
