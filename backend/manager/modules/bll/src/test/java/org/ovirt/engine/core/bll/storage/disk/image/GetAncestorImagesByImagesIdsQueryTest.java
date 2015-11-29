package org.ovirt.engine.core.bll.storage.disk.image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;

@RunWith(MockitoJUnitRunner.class)
public class GetAncestorImagesByImagesIdsQueryTest extends
        AbstractUserQueryTest<IdsQueryParameters, GetAncestorImagesByImagesIdsQuery<IdsQueryParameters>> {

    @Mock
    private DiskImageDao diskImageDao;

    private List<Guid> imagesIds;
    private DiskImage image1; // snapshot 0 (base snapshot)
    private DiskImage image2; // snapshot 1
    private DiskImage image3; // snapshot 2 (active image)

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initializeImages();
        when(getDbFacadeMockInstance().getDiskImageDao()).thenReturn(diskImageDao);
        imagesIds = new ArrayList<>();
        when(getQueryParameters().getIds()).thenReturn(imagesIds);
    }

    @Test
    public void executeQueryCommandWithEmptyMap() {
        assertTrue(runQuery().isEmpty());
    }

    @Test
    public void executeQueryCommandWithBaseSnapshotOnly() {
        imagesIds.add(image1.getImageId());

        Map<Guid, DiskImage> queryReturnValue = runQuery();
        assertEquals(1, queryReturnValue.size());
        assertEquals(image1, queryReturnValue.get(image1.getImageId()));
    }

    @Test
    public void executeQueryCommandWithSnapshots() {
        imagesIds.add(image1.getImageId());
        imagesIds.add(image2.getImageId());
        imagesIds.add(image3.getImageId());

        Map<Guid, DiskImage> queryReturnValue = runQuery();
        assertEquals(3, queryReturnValue.size());
        assertEquals(image1, queryReturnValue.get(image1.getImageId()));
        assertEquals(image1, queryReturnValue.get(image2.getImageId()));
        assertEquals(image1, queryReturnValue.get(image3.getImageId()));
    }

    private void initializeImages() {
        image1 = new DiskImage();
        image1.setImageId(Guid.newGuid());
        mockImageAncestor(image1, image1);

        image2 = new DiskImage();
        image2.setImageId(Guid.newGuid());
        mockImageAncestor(image2, image1);

        image3 = new DiskImage();
        image3.setImageId(Guid.newGuid());
        mockImageAncestor(image3, image1);
    }

    private void mockImageAncestor(DiskImage imageToMock, DiskImage imageAncestor) {
        when(diskImageDao.getAncestor(imageToMock.getImageId(), getUser().getId(), getQueryParameters().isFiltered()))
                .thenReturn(imageAncestor);
    }

    private Map<Guid, DiskImage> runQuery() {
        getQuery().executeQueryCommand();
        return getQuery().getQueryReturnValue().getReturnValue();
    }
}
