/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
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
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    protected void setUpQueryExpectations(String query) {
        setUpEntityQueryExpectations(1);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(1, failure);
    }

    protected void setUpEntityQueryExpectations(int times) {
        setUpEntityQueryExpectations(times, null);
    }

    protected void setUpEntityQueryExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                QueryType.GetAllDisksByVmId,
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
        return setUpEntityExpectations(mock(DiskImage.class), index);
    }

    static org.ovirt.engine.core.common.businessentities.storage.Disk setUpEntityExpectations(DiskImage entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getVmSnapshotId()).thenReturn(GUIDS[2]);
        when(entity.getVolumeFormat()).thenReturn(VolumeFormat.RAW);
        when(entity.getImageStatus()).thenReturn(ImageStatus.OK);
        when(entity.getVolumeType()).thenReturn(VolumeType.Sparse);
        when(entity.isShareable()).thenReturn(false);
        when(entity.getPropagateErrors()).thenReturn(PropagateErrors.On);
        when(entity.getDiskStorageType()).thenReturn(DiskStorageType.IMAGE);
        when(entity.getImageId()).thenReturn(GUIDS[1]);
        ArrayList<Guid> sdIds = new ArrayList<>();
        sdIds.add(Guid.Empty);
        when(entity.getStorageIds()).thenReturn(sdIds);
        return setUpStatisticalEntityExpectations(entity);
    }

    static org.ovirt.engine.core.common.businessentities.storage.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        when(entity.getReadRate()).thenReturn(1);
        when(entity.getReadOps()).thenReturn(2L);
        when(entity.getWriteRate()).thenReturn(3);
        when(entity.getWriteOps()).thenReturn(4L);
        when(entity.getReadLatency()).thenReturn(5.0);
        when(entity.getWriteLatency()).thenReturn(6.0);
        when(entity.getFlushLatency()).thenReturn(7.0);
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
    public void testAddAsyncPending() {
        doTestAddAsync(AsyncTaskStatusEnum.init, CreationStatus.PENDING);
    }

    @Test
    public void testAddAsyncInProgress() {
        doTestAddAsync(AsyncTaskStatusEnum.running, CreationStatus.IN_PROGRESS);
    }

    @Test
    public void testAddAsyncFinished() {
        doTestAddAsync(AsyncTaskStatusEnum.finished, CreationStatus.COMPLETE);
    }

    @Test
    public void testAttachDisk() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(
            ActionType.AttachDiskToVm,
            AttachDetachVmDiskParameters.class,
            new String[] { "VmId", "EntityInfo", },
            new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID) },
            true,
            true,
            null,
            null,
            null,
            QueryType.GetDiskByDiskId,
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
    public void testAttachDiskSnapshot() {
        setUriInfo(setUpBasicUriExpectations());
        Guid snapshotId = Guid.newGuid();
        Disk model = getModel();
        model.setSnapshot(new Snapshot());
        model.getSnapshot().setId(snapshotId.toString());
        model.setId(DISK_ID.toString()); //means this is an existing disk --> attach
        setUpCreationExpectations(
            ActionType.AttachDiskToVm,
            AttachDetachVmDiskParameters.class,
            new String[] { "VmId", "EntityInfo", "SnapshotId" },
            new Object[] { VM_ID, new EntityInfo(VdcObjectType.Disk, DISK_ID), snapshotId },
            true,
            true,
            null,
            null,
            null,
            QueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
    }

    private void doTestAddAsync(AsyncTaskStatusEnum asyncStatus, CreationStatus creationStatus) {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            QueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUpCreationExpectations(
            ActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId" },
            new Object[] { VM_ID },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(asyncStatus)),
            QueryType.GetDiskByDiskId,
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
    public void testAddDiskWithJobId() {

        Disk model = getModel();

        setUriInfo(setUpBasicUriExpectations());

        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.JOB_ID_CONSTRAINT,
                true,
                GUIDS[1].toString(),
                collection.getUriInfo()
        ));

        setUpGetDiskExpectations();
        setCommonExpectations(model);
        Response response = collection.add(getModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddDiskWithStepId() {

        Disk model = getModel();

        setUriInfo(setUpBasicUriExpectations());

        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.STEP_ID_CONSTRAINT,
                true,
                GUIDS[1].toString(),
                collection.getUriInfo()
        ));

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
            QueryType.GetDiskByDiskId,
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
            QueryType.GetAllDisksByVmId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            asList(getEntity(0))
        );
        */
        setUpEntityQueryExpectations(
            QueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUpCreationExpectations(
            ActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId", "StorageDomainId" },
            new Object[] { VM_ID, GUIDS[2] },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
            QueryType.GetDiskByDiskId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DISK_ID },
            asList(getEntity(0))
        );
        model.setProvisionedSize(1024 * 1024L);
    }

    @Test
    public void testAddDisk() {
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
    public void testAddDiskIdentifyStorageDomainByName() {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpGetDiskExpectations();
        int times = 2;
        while (times-- > 0) {
            setUpEntityQueryExpectations(
                QueryType.GetAllStorageDomains,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getStorageDomains()
            );
        }
        setUpEntityQueryExpectations(
            QueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUpCreationExpectations(
            ActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId", "StorageDomainId" },
            new Object[] { VM_ID, GUIDS[2] },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
            QueryType.GetDiskByDiskId,
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
    public void testAddDiskWithinStorageDomain() {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpGetDiskExpectations();
        setUpEntityQueryExpectations(
            QueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[3] },
            getStorageDomain(GUIDS[3])
        );
        setUpCreationExpectations(
            ActionType.AddDisk,
            AddDiskParameters.class,
            new String[] { "VmId", "StorageDomainId" },
            new Object[] { VM_ID, GUIDS[3] },
            true,
            true,
            GUIDS[0],
            asList(GUIDS[3]),
            asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
            QueryType.GetDiskByDiskId,
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
    public void testAddDiskCantDo() {
        doTestBadAddDisk(false, true, CANT_DO);
    }

    @Test
    public void testAddDiskFailure() {
        doTestBadAddDisk(true, false, FAILURE);
    }

    private void doTestBadAddDisk(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(
            QueryType.GetStorageDomainById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[2] },
            getStorageDomain(GUIDS[2])
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.AddDisk,
                AddDiskParameters.class,
                new String[] { "VmId" },
                new Object[] { VM_ID },
                valid,
                success
            )
        );
        Disk model = getModel();
        model.setProvisionedSize(1024 * 1024L);

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        Disk model = new Disk();
        setUriInfo(setUpBasicUriExpectations());
        // Because of extra frame offset used current method name in test, while in real world used "add" method name
        verifyIncompleteException(assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "Disk", "lambda$testAddIncompleteParameters$1", "provisionedSize|size", "format");
    }

    @Test
    public void testAddIncompleteParameters2() {
        Disk model = getModel();
        model.setProvisionedSize(null);
        setUriInfo(setUpBasicUriExpectations());
        // Because of extra frame offset used current method name in test, while in real world used "add" method name
        verifyIncompleteException(assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "Disk", "lambda$testAddIncompleteParameters2$2", "provisionedSize|size");
    }

    @Test
    public void testAddLunDiskMissingType() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().setType(null);
        setUriInfo(setUpBasicUriExpectations());
        // Because of extra frame offset used current method name in test, while in real world used "add" method name
        verifyIncompleteException(assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "HostStorage", "lambda$testAddLunDiskMissingType$3", "type");
    }

    @Test
    public void testAddLunDiskMissingId() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setId(null);
        setUriInfo(setUpBasicUriExpectations());
        // Because of extra frame offset used current method name in test, while in real world used "add" method name
        verifyIncompleteException(assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "LogicalUnit", "lambda$testAddLunDiskMissingId$4", "id");
    }

    @Test
    public void testAddIscsiLunDiskIncompleteParametersConnectionAddress() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setAddress(null);
        setUriInfo(setUpBasicUriExpectations());
        // Because of extra frame offset used current method name in test, while in real world used "add" method name
        verifyIncompleteException(assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "LogicalUnit", "lambda$testAddIscsiLunDiskIncompleteParametersConnectionAddress$5", "address");
    }

    @Test
    public void testAddIscsiLunDiskIncompleteParametersConnectionTarget() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setTarget(null);
        setUriInfo(setUpBasicUriExpectations());
        // Because of extra frame offset used current method name in test, while in real world used "add" method name
        verifyIncompleteException(assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "LogicalUnit", "lambda$testAddIscsiLunDiskIncompleteParametersConnectionTarget$6", "target");
    }

    @Test
    public void testAddIscsiLunDiskIncompleteParametersConnectionPort() {
        Disk model = createIscsiLunDisk();
        model.getLunStorage().getLogicalUnits().getLogicalUnits().get(0).setPort(null);
        setUriInfo(setUpBasicUriExpectations());
        // Because of extra frame offset used current method name in test, while in real world used "add" method name
        verifyIncompleteException(assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "LogicalUnit", "lambda$testAddIscsiLunDiskIncompleteParametersConnectionPort$7", "port");
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
    public void testSubResourceLocatorBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> collection.getDiskResource("foo")));
    }
}
