package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendTemplateDisksResourceTest
        extends AbstractBackendCollectionResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendTemplateDisksResource> {

    private static final Guid TEMPLATE_ID = GUIDS[1];

    public BackendTemplateDisksResourceTest() {
        super(new BackendTemplateDisksResource(TEMPLATE_ID), null, null );
    }

    @Override
    @Test
    @Disabled
    public void testQuery() {
        // skip test inherited from base class as searching
        // over DiskImages is unsupported by the backend
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
                QueryType.GetVmTemplatesDisks,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { TEMPLATE_ID },
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
    public void testSubResourceLocatorBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> collection.getDiskResource("foo")));
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpEntityQueryExpectations(1, null);
        collection.setUriInfo(uriInfo);
        List<Disk> disks = getCollection();
        for (Disk disk : disks) {
            assertNotNull(disk.getStorageDomains());
            List<StorageDomain> storageDomains = disk.getStorageDomains().getStorageDomains();
            assertEquals(1, storageDomains.size());
            assertEquals(storageDomains.get(0).getId(), GUIDS[0].toString());
        }
        verifyCollection(disks);
    }

    @Override
    @Test
    public void testListFailure() {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpEntityQueryExpectations(1, FAILURE);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Override
    @Test
    public void testListCrash() {
        UriInfo uriInfo = setUpUriExpectations(null);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(1, t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_SERVER_LOCALE, t);
    }

    @Override
    @Test
    public void testListCrashClientLocale() {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpEntityQueryExpectations(1, t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }
}
