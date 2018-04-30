package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

public class GetGlusterVolumeBricksQueryTest extends
AbstractQueryTest<IdQueryParameters, GetGlusterVolumeBricksQuery<IdQueryParameters>> {

    @Mock
    GlusterBrickDao glusterBrickDaoMock;

    GlusterVolumeEntity expected;
    GlusterBrickEntity brickEntity;
    List<GlusterBrickEntity> bricks = new ArrayList<>();

    @BeforeEach
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
        when(glusterBrickDaoMock.getBricksOfVolume(expected.getId())).thenReturn(bricks);
    }

    @Test
    public void testExecuteQueryCommnad() {
        getQuery().executeQueryCommand();
        List<GlusterBrickEntity> actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expected.getBricks(), actual, "wrong Gluster Volume");
    }
}
