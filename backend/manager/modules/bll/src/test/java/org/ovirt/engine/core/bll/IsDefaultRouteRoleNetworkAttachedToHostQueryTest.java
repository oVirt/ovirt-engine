package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.queries.IsDefaultRouteRoleNetworkAttachedToHostQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class IsDefaultRouteRoleNetworkAttachedToHostQueryTest
        extends AbstractQueryTest<IsDefaultRouteRoleNetworkAttachedToHostQueryParameters,
        IsDefaultRouteRoleNetworkAttachedToHostQuery<IsDefaultRouteRoleNetworkAttachedToHostQueryParameters>> {

    @Mock
    private NetworkDao networkDao;
    @Mock
    private NetworkAttachmentDao networkAttachmentDao;

    /**
     * @return parameters for test with same name
     */
    static Stream<Object[]> testExecuteQuery() {
        boolean ATTACHED = true;
        boolean DETACHED = !ATTACHED;
        Guid id1 = Guid.newGuid();
        Guid id2 = Guid.newGuid();
        return Stream.of(
            // expected, is-net-cluster-def-route, network id, network id on attachment
            new Object[] { ATTACHED, ATTACHED, id1, id1 },
            new Object[] { DETACHED, DETACHED, id1, id1 },
            new Object[] { DETACHED, ATTACHED, id1, id2 },
            new Object[] { DETACHED, DETACHED, id1, id2 }
        );
    }

    @ParameterizedTest
    @MethodSource
    void testExecuteQuery(boolean expected, boolean isDefaultRouteRole, Guid netId, Guid netIdOnAttachment) {

        when(getQueryParameters().getClusterId()).thenReturn(Guid.newGuid());
        when(getQueryParameters().getHostId()).thenReturn(Guid.newGuid());

        Network n = new Network();
        n.setId(netId);
        n.setCluster(new NetworkCluster());

        NetworkAttachment na = new NetworkAttachment();
        na.setNetworkId(netId);

        when(networkDao.getAllForCluster(any())).thenReturn(Collections.singletonList(n));
        when(networkAttachmentDao.getAllForHost(any())).thenReturn(Collections.singletonList(na));

        n.getCluster().setDefaultRoute((Boolean) isDefaultRouteRole);
        n.setId(netId);
        na.setNetworkId(netIdOnAttachment);

        getQuery().executeQueryCommand();
        assertEquals(expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
