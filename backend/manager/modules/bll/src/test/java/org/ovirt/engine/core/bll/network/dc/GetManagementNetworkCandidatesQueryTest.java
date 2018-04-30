package org.ovirt.engine.core.bll.network.dc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GetManagementNetworkCandidatesQueryTest extends
        AbstractQueryTest<IdQueryParameters, GetManagementNetworkCandidatesQuery<IdQueryParameters>> {
    public static final Guid DC_ID = Guid.newGuid();

    @Mock
    private NetworkDao mockNetworkDao;
    @Mock
    private Predicate<Network> mockNetworkPredicate;
    @Mock
    private Network mockExternalNetwork;
    @Mock
    private Network mockManagementNetworkCandidate;

    private List<Network> dcNetworks = new ArrayList<>();

    @Override
    protected void initQuery(GetManagementNetworkCandidatesQuery<IdQueryParameters> query) {
        super.initQuery(query);

        when(query.getParameters().getId()).thenReturn(DC_ID);

        when(mockNetworkPredicate.test(mockExternalNetwork)).thenReturn(false);
        when(mockNetworkPredicate.test(mockManagementNetworkCandidate)).thenReturn(true);
        when(mockNetworkDao.getAllForDataCenter(DC_ID)).thenReturn(dcNetworks);
    }

    @Test
    public void testExecuteQueryCommand() {
        dcNetworks.addAll(Arrays.asList(mockExternalNetwork, mockManagementNetworkCandidate));

        getQuery().executeQueryCommand();

        final List<Network> actual = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(actual);
        assertThat(actual, hasSize(1));
        assertTrue(actual.contains(mockManagementNetworkCandidate));
    }

    @Test
    public void testExecuteQueryCommandNoNetworksDefined() {

        getQuery().executeQueryCommand();

        final List<Network> actual = getQuery().getQueryReturnValue().getReturnValue();

        assertTrue(actual.isEmpty());
    }
}
