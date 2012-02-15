package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.utils.RandomUtils;

public class GetAllVmSnapshotsByDriveQueryTest extends
        AbstractQueryTest<GetAllVmSnapshotsByDriveParameters, GetAllVmSnapshotsByDriveQuery<GetAllVmSnapshotsByDriveParameters>> {

    @Test
    public void testExecuteQuery() {
        Guid vmID = Guid.NewGuid();
        Guid parentID = Guid.NewGuid();
        String drive = RandomUtils.instance().nextNumericString(3);
        when(getQueryParameters().getId()).thenReturn(vmID);
        when(getQueryParameters().getDrive()).thenReturn(drive);

        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        DiskImageDAO diskImageDAOMock = mock(DiskImageDAO.class);
        when(dbFacadeMock.getDiskImageDAO()).thenReturn(diskImageDAOMock);

        DiskImage activeImage = new DiskImage();
        activeImage.setactive(true);
        activeImage.setinternal_drive_mapping(drive);
        activeImage.setId(new Guid(UUID.randomUUID()));
        activeImage.setParentId(parentID);

        DiskImage inactiveImage = new DiskImage();
        inactiveImage.setactive(false);
        inactiveImage.setinternal_drive_mapping(drive);
        inactiveImage.setId(new Guid(UUID.randomUUID()));
        inactiveImage.setParentId(parentID);

        DiskImage parent = new DiskImage();
        parent.setinternal_drive_mapping(drive);
        parent.setId(parentID);
        parent.setParentId(Guid.Empty);

        when(diskImageDAOMock.getAllForVm(vmID, getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn(Arrays.asList(activeImage,
                inactiveImage));
        when(diskImageDAOMock.getSnapshotById(inactiveImage.getId())).thenReturn(inactiveImage);
        when(diskImageDAOMock.getSnapshotById(parentID)).thenReturn(parent);

        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        List<DiskImage> result = (List<DiskImage>) getQuery().getQueryReturnValue().getReturnValue();
        Guid tryingImage = getQuery().getQueryReturnValue().getTryingImage();

        assertArrayEquals("Wrong snapshots",
                result.toArray(new DiskImage[result.size()]),
                new DiskImage[] { inactiveImage, parent });

        assertEquals("Wrong trying image", parentID, tryingImage);
    }
}
