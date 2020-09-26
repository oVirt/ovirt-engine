package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.DiskSnapshotsQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;


@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainDiskSnapshotsResourceTest extends
        AbstractBackendCollectionResourceTest<DiskSnapshot, Disk, BackendStorageDomainDiskSnapshotsResource> {

    protected static final Guid DOMAIN_ID = GUIDS[2];
    protected static final Guid DISK_ID = GUIDS[3];

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false)
        );
    }

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

    private List<Disk> getDisks() {
        List<Disk> disks = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            disks.add(getEntity(i));
        }
        return disks;
    }

    @Override
    @Test
    @Disabled
    public void testQuery() {
    }

    @Test
    @Override
    @MockedConfig("mockConfiguration")
    public void testList() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetAllDiskSnapshotsByStorageDomainId,
                DiskSnapshotsQueryParameters.class,
                new String[] { "Id" },
                new Object[] {DOMAIN_ID},
                getDisks());
        verifyCollection(getCollection());
    }

    @Test
    public void testListIncludingActive() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        uriInfo = addMatrixParameterExpectations(uriInfo, BackendStorageDomainDiskSnapshotsResource.INCLUDE_ACTIVE,
                "yes");
        setUriInfo(uriInfo);

        // TODO: How to verify query getIncludeActive()?
        setUpEntityQueryExpectations(QueryType.GetAllDiskSnapshotsByStorageDomainId,
                DiskSnapshotsQueryParameters.class,
                new String[] { "Id" },
                new Object[] {DOMAIN_ID},
                getDisks());
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
