package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockedConfig;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendStorageDomainDiskSnapshotResourceTest
        extends AbstractBackendSubResourceTest<DiskSnapshot, Disk, BackendStorageDomainDiskSnapshotResource> {

    protected static final Guid DOMAIN_ID = GUIDS[0];
    protected static final Guid IMAGE_ID = GUIDS[1];
    protected static final Guid DISK_ID = GUIDS[2];

    public BackendStorageDomainDiskSnapshotResourceTest() {
        super(new BackendStorageDomainDiskSnapshotResource(IMAGE_ID.toString(),
                new BackendStorageDomainDiskSnapshotsResource(DOMAIN_ID)));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.PropagateDiskErrors, false));
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetDiskSnapshotByImageId, IdQueryParameters.class,
                new String[]{"Id"}, new Object[]{IMAGE_ID},
                getEntity(1));

        DiskSnapshot diskSnapshot = resource.get();
        verifyModelSpecific(diskSnapshot, 1);
    }

    @Test
    @MockedConfig("mockConfiguration")
    public void testRemove() {
        // setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetDiskSnapshotByImageId, IdQueryParameters.class,
                new String[] { "Id" }, new Object[] { IMAGE_ID },
                getEntity(1));
        ArrayList<Guid> ids = new ArrayList<>();
        ids.add(GUIDS[1]);
        setUriInfo(setUpActionExpectations(
                ActionType.RemoveDiskSnapshots,
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
