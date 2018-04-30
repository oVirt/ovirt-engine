package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;

public class GetIscsiBondByIdQueryTest extends
        AbstractQueryTest<IdQueryParameters, GetIscsiBondByIdQuery<IdQueryParameters>> {
    @Mock
    private IscsiBondDao iscsiBondDao;

    Guid iscsiBondId = Guid.newGuid();

    @Test
    public void testExecuteQueryCommand() {
        IscsiBond iscsiBond = mockIscsiBond();
        when(getQueryParameters().getId()).thenReturn(iscsiBondId);
        when(iscsiBondDao.get(iscsiBondId)).thenReturn(iscsiBond);

        // Mock Networks and Storage Server Connections
        List<Guid> networks = new ArrayList<>();
        List<String> storageServerConnections = new ArrayList<>();
        mockNetworksAndStorages(iscsiBondId, networks, storageServerConnections);

        getQuery().executeQueryCommand();
        IscsiBond result = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(result);

        assertNotNull(iscsiBond.getNetworkIds());
        assertEquals(0, iscsiBond.getNetworkIds().size());

        assertNotNull(iscsiBond.getStorageConnectionIds());
        assertEquals(0, iscsiBond.getStorageConnectionIds().size());
    }

    @Test
    public void testExecuteQueryWithStoragesAndNetworksCommand() {
        IscsiBond iscsiBond = mockIscsiBond();
        when(getQueryParameters().getId()).thenReturn(iscsiBondId);
        when(iscsiBondDao.get(iscsiBondId)).thenReturn(iscsiBond);

        // Mock Networks and Storage Server Connections
        List<Guid> networks = new ArrayList<>();
        Guid networkId = Guid.newGuid();
        networks.add(networkId);
        List<String> storageServerConnections = new ArrayList<>();
        String connectionId = Guid.newGuid().toString();
        storageServerConnections.add(connectionId);
        mockNetworksAndStorages(iscsiBondId, networks, storageServerConnections);

        getQuery().executeQueryCommand();
        IscsiBond result = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(result);

        assertNotNull(iscsiBond.getNetworkIds());
        assertEquals(1, iscsiBond.getNetworkIds().size());
        assertEquals(iscsiBond.getNetworkIds().get(0), networkId);

        assertNotNull(iscsiBond.getStorageConnectionIds());
        assertEquals(1, iscsiBond.getStorageConnectionIds().size());
        assertEquals(iscsiBond.getStorageConnectionIds().get(0), connectionId);
    }

    @Test
    public void testExecuteQueryWithNotExistingIscsiBond() {
        when(getQueryParameters().getId()).thenReturn(iscsiBondId);

        getQuery().executeQueryCommand();
        IscsiBond result = getQuery().getQueryReturnValue().getReturnValue();

        assertNull(result);
    }

    private IscsiBond mockIscsiBond() {
        IscsiBond iscsiBond = new IscsiBond();
        iscsiBond.setId(iscsiBondId);
        return iscsiBond;
    }

    private void mockNetworksAndStorages(Guid iscsiBondId, List<Guid> networks, List<String> storageServerConnections) {
        when(iscsiBondDao.getNetworkIdsByIscsiBondId(iscsiBondId)).thenReturn(networks);
        when(iscsiBondDao.getStorageConnectionIdsByIscsiBondId(iscsiBondId)).thenReturn(storageServerConnections);
    }

}
