package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.RegisterDiskParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.GetUnregisteredDisksQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainDisksResourceTest extends AbstractBackendCollectionResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendStorageDomainDisksResource> {

    protected static final Guid storagePoolId = new Guid("44444444-4444-4444-4444-444444444444");

    public BackendStorageDomainDisksResourceTest() {
        super(new BackendStorageDomainDisksResource(GUIDS[3]), SearchType.Disk, "Disks : ");
    }

    @Override
    protected List<Disk> getCollection() {
        return collection.list().getDisks();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        DiskImage entity = new DiskImage();
        entity.setId(GUIDS[index]);
        entity.setVolumeFormat(VolumeFormat.RAW);
        entity.setImageStatus(ImageStatus.OK);
        entity.setVolumeType(VolumeType.Sparse);
        entity.setShareable(false);
        entity.setPropagateErrors(PropagateErrors.On);
        return setUpStatisticalEntityExpectations(entity);    }

    static org.ovirt.engine.core.common.businessentities.storage.Disk setUpStatisticalEntityExpectations(DiskImage entity) {
        entity.setReadRate(1);
        entity.setReadOps(2L);
        entity.setWriteRate(3);
        entity.setWriteOps(4L);
        entity.setReadLatency(5.0);
        entity.setWriteLatency(6.0);
        entity.setFlushLatency(7.0);
        return entity;
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
        Disk model = getModel();
        setUpCreationExpectations(ActionType.AddDisk,
                AddDiskParameters.class,
                new String[] {"StorageDomainId"},
                new Object[] {GUIDS[3]},
                true,
                true,
                GUIDS[0],
                asList(storagePoolId),
                asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] {"Id"},
                new Object[] {GUIDS[0]},
                getEntity(0));
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddUnregistered() {
        setUriInfo(addMatrixParameterExpectations(setUpBasicUriExpectations(), BackendStorageDomainDisksResource.UNREGISTERED));
        setUpHttpHeaderExpectations("Expect", "201-created");
        StoragePool storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] {GUIDS[3]},
                Collections.singletonList(storagePool));
        setUpEntityQueryExpectations(QueryType.GetUnregisteredDisk,
                GetUnregisteredDiskQueryParameters.class,
                new String[] {"DiskId", "StorageDomainId", "StoragePoolId"},
                new Object[] {GUIDS[0], GUIDS[3], storagePoolId},
                getEntity(0));
        Disk model = getModel();
        org.ovirt.engine.core.common.businessentities.storage.Disk imageToRegister = new DiskMapper().map(model, getEntity(0));

        // imageToRegister.setDiskAlias("alias");
        setUpCreationExpectations(ActionType.RegisterDisk,
                RegisterDiskParameters.class,
                new String[] { "DiskImage" },
                new Object[] { imageToRegister },
                true,
                true,
                GUIDS[0],
                QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] {"Id"},
                new Object[] {GUIDS[0]},
                getEntity(0));
        model.setId(GUIDS[0].toString());
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Test
    public void testAddIdentifyStorageDomainByName() {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
        Disk model = getModel();
        model.getStorageDomains().getStorageDomains().get(0).setId(null);
        model.getStorageDomains().getStorageDomains().get(0).setName("Storage_Domain_1");
        setUpCreationExpectations(ActionType.AddDisk,
                AddDiskParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                asList(storagePoolId),
                asList(new AsyncTaskStatus(AsyncTaskStatusEnum.finished)),
                QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] {"Id"},
                new Object[] {GUIDS[0]},
                getEntity(0));
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Disk);
        verifyModel((Disk)response.getEntity(), 0);
        assertNull(((Disk)response.getEntity()).getCreationStatus());
    }

    @Override
    @Test
    @Disabled
    public void testQuery() {
    }

    @Test
    @Override
    public void testList() throws Exception {
        collection.setUriInfo(setUpBasicUriExpectations());

        List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        setUpEntityQueryExpectations(QueryType.GetAllDisksByStorageDomainId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] {GUIDS[3]},
                entities);
        verifyCollection(getCollection());
    }

    @Test
    public void testListUnregistered() throws Exception {
        setUriInfo(addMatrixParameterExpectations(setUpBasicUriExpectations(), BackendStorageDomainDisksResource.UNREGISTERED));

        StoragePool storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        setUpEntityQueryExpectations(QueryType.GetStoragePoolsByStorageDomainId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] {GUIDS[3]},
                Collections.singletonList(storagePool));

        List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        setUpEntityQueryExpectations(QueryType.GetUnregisteredDisks,
                GetUnregisteredDisksQueryParameters.class,
                new String[] {"StorageDomainId", "StoragePoolId"},
                new Object[] {GUIDS[3], storagePoolId},
                entities);
        verifyCollection(getCollection());
    }

    @Test
    @Override
    @Disabled
    public void testListFailure() {

    }

    @Test
    @Override
    @Disabled
    public void testListCrash() {

    }

    @Test
    @Override
    @Disabled
    public void testListCrashClientLocale() {

    }

    static Disk getModel() {
        Disk model = new Disk();
        model.setProvisionedSize(1024 * 1024L);
        model.setFormat(DiskFormat.COW);
        model.setSparse(true);
        model.setShareable(false);
        model.setPropagateErrors(true);
        model.setStorageDomains(new StorageDomains());
        model.getStorageDomains().getStorageDomains().add(new StorageDomain());
        model.getStorageDomains().getStorageDomains().get(0).setId(GUIDS[2].toString());
        return model;
    }

    @Override
    protected void verifyModel(Disk model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(Disk model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertFalse(model.isSetVm());
        assertTrue(model.isSparse());
        assertTrue(model.isPropagateErrors());
    }
}
