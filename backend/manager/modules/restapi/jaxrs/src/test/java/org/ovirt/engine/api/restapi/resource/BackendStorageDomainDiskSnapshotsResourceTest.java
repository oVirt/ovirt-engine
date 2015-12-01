package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainDiskSnapshotsResourceTest extends
        AbstractBackendCollectionResourceTest<DiskSnapshot, Disk, BackendStorageDomainDiskSnapshotsResource> {

    protected static final Guid DOMAIN_ID = GUIDS[2];
    protected static final Guid DISK_ID = GUIDS[3];

    public BackendStorageDomainDiskSnapshotsResourceTest() {
        super(new BackendStorageDomainDiskSnapshotsResource(DOMAIN_ID), null, null);
    }

    @Override
    protected List<DiskSnapshot> getCollection() {
        return collection.list().getDiskSnapshots();
    }

    @Override
    protected Disk getEntity(int index) {
        DiskImage entity = new DiskImage();
        entity.setImageId(GUIDS[index]);
        entity.setId(DISK_ID);
        return entity;
    }

    @Override
    @Test
    @Ignore
    public void testQuery() throws Exception {
    }

    @Test
    @Override
    public void testList() throws Exception {
        collection.setUriInfo(setUpBasicUriExpectations());

        List<Disk> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        setUpEntityQueryExpectations(VdcQueryType.GetAllDiskSnapshotsByStorageDomainId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] {DOMAIN_ID},
                entities);
        control.replay();
        verifyCollection(getCollection());
    }

    @Test
    @Override
    @Ignore
    public void testListFailure() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrash() throws Exception {

    }

    @Test
    @Override
    @Ignore
    public void testListCrashClientLocale() throws Exception {

    }

    @Override
    protected void verifyModel(DiskSnapshot model, int index) {
        verifyModelSpecific(model, index);
        verifyLinks(model);
    }

    static void verifyModelSpecific(DiskSnapshot model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(DISK_ID.toString(), model.getDisk().getId());
    }
}
