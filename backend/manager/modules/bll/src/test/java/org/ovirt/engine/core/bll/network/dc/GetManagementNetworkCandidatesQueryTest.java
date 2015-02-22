package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.linq.Predicate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetManagementNetworkCandidatesQueryTest {

    public static final Guid DC_ID = Guid.newGuid();
    public static final Guid USER_ID = Guid.newGuid();

    @Mock
    private NetworkDao mockNetworkDao;
    @Mock
    private Predicate<Network> mockNetworkPredicate;
    @Mock
    private DbUser mockUser;
    @Mock
    private Network mockExternalNetwork;
    @Mock
    private Network mockNonExternalNetwork;

    private GetManagementNetworkCandidatesQuery underTest;
    private ArrayList<Network> dcNetworks = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        when(mockUser.getId()).thenReturn(USER_ID);
        when(mockNetworkPredicate.eval(mockExternalNetwork)).thenReturn(true);
        when(mockNetworkPredicate.eval(mockNonExternalNetwork)).thenReturn(false);
        when(mockNetworkDao.getAllForDataCenter(DC_ID, USER_ID, true)).thenReturn(dcNetworks);

        final IdQueryParameters params = new IdQueryParameters(DC_ID);
        params.setFiltered(true);

        underTest = new TestGetManagementNetworkCandidatesQuery(params);
    }

    @Test
    public void testExecuteQueryCommand() {
        dcNetworks.addAll(Arrays.asList(mockExternalNetwork, mockNonExternalNetwork));

        underTest.executeQueryCommand();

        final List<Network> actual = underTest.getQueryReturnValue().getReturnValue();

        assertThat(actual, hasSize(1));
        assertTrue(actual.contains(mockNonExternalNetwork));
    }

    @Test
    public void testExecuteQueryCommandNoNetworksDefined() {

        underTest.executeQueryCommand();

        final List<Network> actual = underTest.getQueryReturnValue().getReturnValue();

        assertTrue(actual.isEmpty());
    }

    private class TestGetManagementNetworkCandidatesQuery
            extends GetManagementNetworkCandidatesQuery<IdQueryParameters> {

        private TestGetManagementNetworkCandidatesQuery(IdQueryParameters params) {
            super(params);
        }

        @Override
        protected DbUser initUser() {
            return mockUser;
        }

        @Override
        NetworkDao getNetworkDao() {
            return mockNetworkDao;
        }

        @Override
        Predicate<Network> getExternalNetworkPredicate() {
            return mockNetworkPredicate;
        }
    }
}
