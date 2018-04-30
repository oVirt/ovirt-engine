package org.ovirt.engine.core.bll.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public class GetGlusterVolumeByIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetGlusterVolumeByIdQuery<IdQueryParameters>> {

    GlusterVolumeEntity expected;

    @Mock
    GlusterVolumeDao glusterVolumeDaoMock;

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
    }

    private void setupMock() {
        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(expected.getId());

        // Mock the Dao
        when(glusterVolumeDaoMock.getById(expected.getId())).thenReturn(expected);
    }

    @Test
    public void testExecuteQueryCommnad() {
        getQuery().executeQueryCommand();
        GlusterVolumeEntity actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expected.getId(), actual.getId(), "wrong Gluster Volume");
    }
}
