package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;

/** A test case for the {@link GetAllVdsByStoragePoolQuery} class. */
public class GetAllVdsByStoragePoolQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetAllVdsByStoragePoolQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQueryCommand() {
        // Prepare the parameters
        Guid spId = Guid.NewGuid();
        when(getQueryParameters().getId()).thenReturn(spId);

        // Prepare the result
        VDS vds = new VDS();
        vds.setStoragePoolId(spId);
        List<VDS> result = Collections.singletonList(vds);

        // Mock the DAO
        VdsDAO vdsDAOMock = mock(VdsDAO.class);
        when(vdsDAOMock.getAllForStoragePool(spId, getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn(result);
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDAOMock);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals("Wrong roles returned", result, getQuery().getQueryReturnValue().getReturnValue());
    }
}
