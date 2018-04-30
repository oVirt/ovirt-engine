package org.ovirt.engine.core.bll.network.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetVnicProfilesByClusterIdQueryTest extends
        AbstractUserQueryTest<IdQueryParameters, GetVnicProfilesByClusterIdQuery<IdQueryParameters>> {

    private static final Guid clusterId = Guid.newGuid();

    @Mock
    private VnicProfileViewDao mockVnicProfileViewDao;

    private List<VnicProfileView> vnicProfileViews = new ArrayList<>();

    @BeforeEach
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
