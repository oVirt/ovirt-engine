package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.action.ExportRepoImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendStorageDomainDiskResourceTest
        extends AbstractBackendSubResourceTest<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk, BackendDiskResource> {

    protected static final Guid DISK_ID = GUIDS[1];
    protected static final Guid STORAGE_DOMAIN_ID = GUIDS[0];

    public BackendStorageDomainDiskResourceTest() {
        super(new BackendStorageDomainDiskResource(DISK_ID.toString(), new BackendStorageDomainDisksResource(STORAGE_DOMAIN_ID)));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
                VdcQueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{DISK_ID},
                getEntity(1));
        setUpEntityQueryExpectations(
                VdcQueryType.GetVmsByDiskGuid,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{DISK_ID},
                Collections.emptyMap());
        control.replay();

        Disk disk = resource.get();
        verifyModelSpecific(disk, 1);
        verifyLinks(disk);
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
                VdcQueryType.GetDiskByDiskId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { DISK_ID },
                getEntity(1, true));
        setUpEntityQueryExpectations(
                VdcQueryType.GetVmsByDiskGuid,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{DISK_ID},
                Collections.emptyMap());
        control.replay();

        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testExport() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.ExportRepoImage,
                ExportRepoImageParameters.class,
                new String[]{"ImageGroupID", "DestinationDomainId"},
                new Object[]{DISK_ID, GUIDS[3]}, true, true, null, null, true));

        Action action = new Action();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(GUIDS[3].toString());

        verifyActionResponse(resource.export(action));
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "/disks/" + DISK_ID, false);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendStorageDomainVmResource(null, "foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testIncompleteExport() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        try {
            control.replay();
            resource.export(new Action());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Action", "export", "storageDomain.id|name");
        }
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
        entity.setWriteRate(2);
        entity.setReadLatency(3.0);
        entity.setWriteLatency(4.0);
        entity.setFlushLatency(5.0);
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
        assertEquals(model.getStorageDomain().getId(), STORAGE_DOMAIN_ID.toString());
    }
}
