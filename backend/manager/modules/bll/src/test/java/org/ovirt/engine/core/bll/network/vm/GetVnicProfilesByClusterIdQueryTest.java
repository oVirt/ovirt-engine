package org.ovirt.engine.core.bll.network.vm;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

@RunWith(MockitoJUnitRunner.class)
public class GetVnicProfilesByClusterIdQueryTest extends
        AbstractUserQueryTest<IdQueryParameters, GetVnicProfilesByClusterIdQuery<IdQueryParameters>> {

    private static final Guid clusterId = Guid.newGuid();

    @Mock
    private VnicProfileViewDao mockVnicProfileViewDao;

    private List<VnicProfileView> vnicProfileViews = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockVnicProfileViewDao.getAllForCluster(clusterId, getUser().getId(), true)).thenReturn(vnicProfileViews);
    }

    @Override
    protected void setUpMockQueryParameters() {
        super.setUpMockQueryParameters();
        when(getQueryParameters().getId()).thenReturn(clusterId);
    }

    @Test
    public void testExecuteQueryCommand() {
        getQuery().executeQueryCommand();
        final List<VnicProfileView> actual = getQuery().getQueryReturnValue().getReturnValue();

        assertThat(actual, Matchers.sameInstance(vnicProfileViews));
    }
}
