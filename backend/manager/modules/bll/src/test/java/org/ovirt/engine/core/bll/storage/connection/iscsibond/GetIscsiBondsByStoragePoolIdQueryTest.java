package org.ovirt.engine.core.bll.storage.connection.iscsibond;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.IscsiBondDao;

public class GetIscsiBondsByStoragePoolIdQueryTest extends
        AbstractQueryTest<IdQueryParameters, GetIscsiBondsByStoragePoolIdQuery<IdQueryParameters>> {

    @Mock
    private IscsiBondDao iscsiBondDao;

    @Test
    public void testExecuteQueryCommand() {
        Guid storagePoolId = Guid.newGuid();
        Guid networkId = Guid.newGuid();
        String connectionId = Guid.newGuid().toString();

        IscsiBond iscsiBond = new IscsiBond();
        iscsiBond.setId(Guid.newGuid());

        when(getQueryParameters().getId()).thenReturn(storagePoolId);
        when(iscsiBondDao.getAllByStoragePoolId(storagePoolId)).thenReturn(Collections.singletonList(iscsiBond));
        when(iscsiBondDao.getNetworkIdsByIscsiBondId(iscsiBond.getId())).thenReturn(Collections.singletonList(networkId));
        when(iscsiBondDao.getStorageConnectionIdsByIscsiBondId(iscsiBond.getId())).thenReturn(Collections.singletonList(connectionId));

        getQuery().executeQueryCommand();

        List<IscsiBond> result = getQuery().getQueryReturnValue().getReturnValue();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(iscsiBond, result.get(0));

        assertNotNull(iscsiBond.getNetworkIds());
        assertEquals(1, iscsiBond.getNetworkIds().size());
        assertEquals(iscsiBond.getNetworkIds().get(0), networkId);

        assertNotNull(iscsiBond.getStorageConnectionIds());
        assertEquals(1, iscsiBond.getStorageConnectionIds().size());
        assertEquals(iscsiBond.getStorageConnectionIds().get(0), connectionId);
    }
}
