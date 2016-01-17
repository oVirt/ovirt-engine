package org.ovirt.engine.core.bll.gluster;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;

public class GetGlusterBrickByIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetGlusterBrickByIdQuery<IdQueryParameters>> {

    GlusterBrickEntity expected;
    GlusterBrickDao glusterBrickDaoMock;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupExpectedBrick();
        setupMock();
    }

    private void setupExpectedBrick() {
        expected = new GlusterBrickEntity();
        expected.setId(Guid.newGuid());
    }

    private void setupMock() {
        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(expected.getId());

        // Mock the Dao
        glusterBrickDaoMock = mock(GlusterBrickDao.class);
        when(glusterBrickDaoMock.getById(expected.getId())).thenReturn(expected);
        doReturn(glusterBrickDaoMock).when(getQuery()).getGlusterBrickDao();
    }

    @Test
    public void testExecuteQueryCommnad() {
        getQuery().executeQueryCommand();
        GlusterBrickEntity actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("wrong Gluster Brick", expected.getId(), actual.getId());
    }
}
