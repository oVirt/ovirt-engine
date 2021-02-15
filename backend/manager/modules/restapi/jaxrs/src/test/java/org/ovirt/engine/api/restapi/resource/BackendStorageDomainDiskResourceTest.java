package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainDiskResourceTest
        extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendStorageDomainDiskResource> {

    protected static final Guid DISK_ID = GUIDS[1];
    protected static final Guid STORAGE_DOMAIN_ID = GUIDS[0];

    public BackendStorageDomainDiskResourceTest() {
        super(new BackendStorageDomainDiskResource(STORAGE_DOMAIN_ID, DISK_ID.toString()));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false));
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
                QueryType.GetDiskAndSnapshotsByDiskId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{DISK_ID},
                getEntity(1));

        Disk disk = resource.get();
        verifyModelSpecific(disk, 1);
        verifyLinks(disk);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
                QueryType.GetDiskAndSnapshotsByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DISK_ID },
                getEntity(1, true));

        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testExport() {
        setUriInfo(setUpActionExpectations(ActionType.ExportRepoImage,
                ExportRepoImageParameters.class,
                new String[]{"ImageGroupID", "DestinationDomainId"},
                new Object[]{DISK_ID, GUIDS[3]}, true, true, null, null, true));

        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(GUIDS[3].toString());

        verifyActionResponse(resource.export(action));
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "/disks/" + DISK_ID, false);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendStorageDomainVmResource(null, "foo")));
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testIncompleteExport() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.export(new Action())),
                "Action", "export", "storageDomain.id|name");
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index) {
        return getEntity(index, false);
    }

    protected org.ovirt.engine.core.common.businessentities.storage.Disk getEntity(int index, boolean noSD) {
        DiskImage entity = new DiskImage();
        entity.setId(GUIDS[index]);
        entity.setVolumeFormat(VolumeFormat.RAW);
        entity.setImageStatus(ImageStatus.OK);
        entity.setVolumeType(VolumeType.Sparse);
        entity.setShareable(false);
        entity.setPropagateErrors(PropagateErrors.On);
        ArrayList<Guid> storages = new ArrayList<>();
        if (!noSD) {
            storages.addAll(Arrays.asList(GUIDS));
        }
        entity.setStorageIds(storages);
        return setUpStatisticalEntityExpectations(entity);
    }
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
        assertEquals(model.getStorageDomains().getStorageDomains().get(0).getId(), STORAGE_DOMAIN_ID.toString());
    }
}
