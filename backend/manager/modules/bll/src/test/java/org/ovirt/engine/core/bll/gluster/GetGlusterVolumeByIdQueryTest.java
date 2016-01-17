package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class GetGlusterVolumeByIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetGlusterVolumeByIdQuery<IdQueryParameters>> {

    GlusterVolumeEntity expected;
    GlusterVolumeDao glusterVolumeDaoMock;

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
    }

    private void setupMock() {
        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(expected.getId());

        // Mock the Dao
        glusterVolumeDaoMock = mock(GlusterVolumeDao.class);
        when(glusterVolumeDaoMock.getById(expected.getId())).thenReturn(expected);
        doReturn(glusterVolumeDaoMock).when(getQuery()).getGlusterVolumeDao();
    }

    @Test
    public void testExecuteQueryCommnad() {
        getQuery().executeQueryCommand();
        GlusterVolumeEntity actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("wrong Gluster Volume", expected.getId(), actual.getId());
    }
}
