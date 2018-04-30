package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostsForStorageOperationParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public class GetHostsForStorageOperationQueryTest extends AbstractQueryTest<GetHostsForStorageOperationParameters, GetHostsForStorageOperationQuery<GetHostsForStorageOperationParameters>> {
    @Mock
    private VdsDao vdsDaoMock;

    @Test
    public void testExecuteQueryCommand() {
        // Prepare the parameters
        Guid spId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(spId);
        when(getQueryParameters().isLocalFsOnly()).thenReturn(false);

        // Prepare the result
        VDS vds = new VDS();
        vds.setStoragePoolId(spId);
        List<VDS> result = Collections.singletonList(vds);

        // Mock the Dao
        when(vdsDaoMock.getHostsForStorageOperation(spId, getQueryParameters().isLocalFsOnly())).thenReturn(result);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals(result, getQuery().getQueryReturnValue().getReturnValue(), "Wrong Vds returned");
    }
}
