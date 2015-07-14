package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostsForStorageOperationParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public class GetHostsForStorageOperationQueryTest extends AbstractQueryTest<GetHostsForStorageOperationParameters, GetHostsForStorageOperationQuery<GetHostsForStorageOperationParameters>> {

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
        VdsDao vdsDaoMock = mock(VdsDao.class);
        when(vdsDaoMock.getHostsForStorageOperation(spId, getQueryParameters().isLocalFsOnly())).thenReturn(result);
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDaoMock);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals("Wrong Vds returned", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
