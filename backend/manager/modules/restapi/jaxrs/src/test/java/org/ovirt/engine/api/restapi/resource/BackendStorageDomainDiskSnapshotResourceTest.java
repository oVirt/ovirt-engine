package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;

import org.junit.Test;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;


public class BackendStorageDomainDiskSnapshotResourceTest
        extends AbstractBackendSubResourceTest<DiskSnapshot, Disk, BackendStorageDomainDiskSnapshotResource> {

    protected static final Guid DOMAIN_ID = GUIDS[0];
    protected static final Guid IMAGE_ID = GUIDS[1];
    protected static final Guid DISK_ID = GUIDS[2];

    public BackendStorageDomainDiskSnapshotResourceTest() {
        super(new BackendStorageDomainDiskSnapshotResource(IMAGE_ID.toString(),
                new BackendStorageDomainDiskSnapshotsResource(DOMAIN_ID)));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetDiskSnapshotByImageId, IdQueryParameters.class,
                new String[]{"Id"}, new Object[]{IMAGE_ID},
                getEntity(1));
        control.replay();

        DiskSnapshot diskSnapshot = resource.get();
        verifyModelSpecific(diskSnapshot, 1);
    }

    @Test
    public void testRemove() {
        // setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetDiskSnapshotByImageId, IdQueryParameters.class,
                new String[] { "Id" }, new Object[] { IMAGE_ID },
                getEntity(1));
        ArrayList<Guid> ids = new ArrayList<>();
        ids.add(GUIDS[1]);
        setUriInfo(setUpActionExpectations(
                VdcActionType.RemoveDiskSnapshots,
                RemoveDiskSnapshotsParameters.class,
                new String[] { "ImageIds" },
                new Object[] { ids },
                true,
                true));
        verifyRemove(resource.remove());

    }

    @Override
    protected Disk getEntity(int index) {
        DiskImage entity = new DiskImage();
        entity.setImageId(GUIDS[index]);
        entity.setId(DISK_ID);
        return entity;
    }

    @Override
    protected void verifyModel(DiskSnapshot model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(DiskSnapshot model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
    }

}
