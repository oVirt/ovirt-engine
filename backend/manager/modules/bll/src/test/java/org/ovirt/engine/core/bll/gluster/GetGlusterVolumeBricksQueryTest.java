package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

public class GetGlusterVolumeBricksQueryTest extends
AbstractQueryTest<IdQueryParameters, GetGlusterVolumeBricksQuery<IdQueryParameters>> {

    GlusterVolumeEntity expected;
    GlusterBrickDao glusterBrickDaoMock;
    GlusterBrickEntity brickEntity;
    List<GlusterBrickEntity> bricks = new ArrayList<>();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupExpectedVolume();
        setupMock();
    }

    private void setupExpectedVolume() {
        expected = new GlusterVolumeEntity();
        expected.setId(Guid.newGuid());
        brickEntity = new GlusterBrickEntity();
        brickEntity.setId(Guid.newGuid());
        brickEntity.setServerId(Guid.newGuid());
        brickEntity.setBrickDirectory("/tmp/b1");
        brickEntity.setStatus(GlusterStatus.DOWN);
        bricks.add(brickEntity);
        expected.setBricks(bricks);
    }

    private void setupMock() {
        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(expected.getId());

        // Mock the Dao
        glusterBrickDaoMock = mock(GlusterBrickDao.class);
        when(glusterBrickDaoMock.getBricksOfVolume(expected.getId())).thenReturn(bricks);
        doReturn(glusterBrickDaoMock).when(getQuery()).getGlusterBrickDao();
    }

    @Test
    public void testExecuteQueryCommnad() {
        getQuery().executeQueryCommand();
        List<GlusterBrickEntity> actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("wrong Gluster Volume", expected.getBricks(), actual);
    }
}
