package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskFormat;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendDisksResourceTest extends AbstractBackendCollectionResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendDisksResource> {

    public BackendDisksResourceTest() {
        super(new BackendDisksResource(), SearchType.Disk, "Disks : ");
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false));
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
    @MockedConfig("mockConfiguration")
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[]{ "Id" },
                new Object[]{ GUIDS[0] },
                getEntity(0));
        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getStorageDomains().get(0));
        Disk model = getModel();
        setUpCreationExpectations(ActionType.AddDisk,
                AddDiskParameters.class,
                new String[] {"StorageDomainId"},
                new Object[] {GUIDS[2]},
                true,
                true,
                GUIDS[0],
                asList(GUIDS[3]),
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
    @MockedConfig("mockConfiguration")
    public void testAddIdentifyStorageDomainByName() {
        setUriInfo(setUpBasicUriExpectations());
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpEntityQueryExpectations(QueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[]{ "Id" },
                new Object[]{ GUIDS[0] },
                getEntity(0));
        setUpEntityQueryExpectations(QueryType.GetStorageDomainById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[2] },
                getStorageDomains().get(0));
        Disk model = getModel();
        model.getStorageDomains().getStorageDomains().get(0).setId(null);
        model.getStorageDomains().getStorageDomains().get(0).setName("Storage_Domain_1");
        setUpEntityQueryExpectations(QueryType.GetAllStorageDomains,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getStorageDomains());
        setUpCreationExpectations(ActionType.AddDisk,
                AddDiskParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true,
                GUIDS[0],
                asList(GUIDS[3]),
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

    private List<org.ovirt.engine.core.common.businessentities.StorageDomain> getStorageDomains() {
        List<org.ovirt.engine.core.common.businessentities.StorageDomain> sds = new LinkedList<>();
        org.ovirt.engine.core.common.businessentities.StorageDomain sd = new org.ovirt.engine.core.common.businessentities.StorageDomain();
        sd.setStorageName("Storage_Domain_1");
        sd.setId(GUIDS[2]);
        sds.add(sd);
        return sds;
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

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
                QueryType.GetAllDisksWithSnapshots,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                getEntityList(),
                failure);

        if (!query.isEmpty()) {
            super.setUpQueryExpectations(query, failure);
        }

    }

    private List<org.ovirt.engine.core.common.businessentities.storage.Disk> getEntityList() {
        return IntStream.range(0, NAMES.length)
                .mapToObj(this::getEntity)
                .collect(Collectors.toList());
    }
}
