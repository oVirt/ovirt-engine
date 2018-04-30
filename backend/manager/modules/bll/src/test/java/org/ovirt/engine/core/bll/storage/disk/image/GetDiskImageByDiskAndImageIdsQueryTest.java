package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.GetDiskImageByDiskAndImageIdsParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;

public class GetDiskImageByDiskAndImageIdsQueryTest extends AbstractUserQueryTest<GetDiskImageByDiskAndImageIdsParameters,
                GetDiskImageByDiskAndImageIdsQuery<GetDiskImageByDiskAndImageIdsParameters>> {

    @Mock
    private DiskImageDao diskImageDao;

    private Guid diskId;
    private Guid imageId;
    private DiskImage diskImage;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        diskId = Guid.newGuid();
        imageId = Guid.newGuid();

        diskImage = new DiskImage();
        diskImage.setId(diskId);
        diskImage.setImageId(imageId);
    }

    @Test
    public void testExecuteQueryCommand() {
        GetDiskImageByDiskAndImageIdsParameters params = getQueryParameters();
        when(params.getDiskId()).thenReturn(diskId);
        when(params.getImageId()).thenReturn(imageId);
        when(diskImageDao.getDiskImageByDiskAndImageIds(
                diskId, imageId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(diskImage);

        GetDiskImageByDiskAndImageIdsQuery<GetDiskImageByDiskAndImageIdsParameters> query = getQuery();
        query.executeQueryCommand();

        DiskImage returnDiskImage = query.getQueryReturnValue().getReturnValue();
        assertEquals(returnDiskImage, diskImage);
    }
}
