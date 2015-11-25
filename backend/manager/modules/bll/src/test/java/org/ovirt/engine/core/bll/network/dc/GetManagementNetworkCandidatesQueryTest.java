package org.ovirt.engine.core.bll.network.dc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetManagementNetworkCandidatesQueryTest extends BaseCommandTest {

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
    private Network mockManagementNetworkCandidate;

    private GetManagementNetworkCandidatesQuery underTest;
    private List<Network> dcNetworks = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        when(mockUser.getId()).thenReturn(USER_ID);
        when(mockNetworkPredicate.test(mockExternalNetwork)).thenReturn(false);
        when(mockNetworkPredicate.test(mockManagementNetworkCandidate)).thenReturn(true);
        when(mockNetworkDao.getAllForDataCenter(DC_ID, USER_ID, true)).thenReturn(dcNetworks);

        final IdQueryParameters params = new IdQueryParameters(DC_ID);
        params.setFiltered(true);

        underTest = new TestGetManagementNetworkCandidatesQuery(params);
    }

    @Test
    public void testExecuteQueryCommand() {
        dcNetworks.addAll(Arrays.asList(mockExternalNetwork, mockManagementNetworkCandidate));

        underTest.executeQueryCommand();

        final List<Network> actual = underTest.getQueryReturnValue().getReturnValue();

        assertNotNull(actual);
        assertThat(actual, hasSize(1));
        assertTrue(actual.contains(mockManagementNetworkCandidate));
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
            postConstruct();
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
        public Predicate<Network> getManagementNetworkCandidatePredicate() {
            return mockNetworkPredicate;
        }
    }
}
