package org.ovirt.engine.core.bll.network.vm;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

@RunWith(MockitoJUnitRunner.class)
public class GetVnicProfilesByClusterIdQueryTest {

    private static final Guid clusterId = Guid.newGuid();
    private static final Guid userId = Guid.newGuid();

    @Mock
    private VnicProfileViewDao mockVnicProfileViewDao;

    @InjectMocks
    private GetVnicProfilesByClusterIdQuery underTest =
            new TestGetVnicProfilesByClusterIdQuery(createParams(clusterId), userId);

    private List<VnicProfileView> vnicProfileViews = new ArrayList<>();

    @Before
    public void setUp() {
        when(mockVnicProfileViewDao.getAllForCluster(clusterId, userId, true)).thenReturn(vnicProfileViews);
    }

    private IdQueryParameters createParams(Guid clusterId) {
        final IdQueryParameters parameters = new IdQueryParameters(clusterId);
        parameters.setFiltered(true);
        return parameters;
    }

    @Test
    public void testExecuteQueryCommand() {
        underTest.executeQueryCommand();
        final List<VnicProfileView> actual = underTest.getQueryReturnValue().getReturnValue();

        assertThat(actual, Matchers.sameInstance(vnicProfileViews));
    }

    private static class TestGetVnicProfilesByClusterIdQuery extends GetVnicProfilesByClusterIdQuery<IdQueryParameters> {

        private final Guid userId;

        public TestGetVnicProfilesByClusterIdQuery(IdQueryParameters parameters, Guid userId) {
            super(parameters);
            this.userId = userId;
        }

        @Override
        protected Guid getUserID() {
            return userId;
        }
    }
}
