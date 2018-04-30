package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

/** A test case for the {@link GetAllVdsByStoragePoolQuery} class. */
public class GetAllVdsByStoragePoolQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetAllVdsByStoragePoolQuery<IdQueryParameters>> {
    @Mock
    private VdsDao vdsDaoMock;

    @Test
    public void testExecuteQueryCommand() {
        // Prepare the parameters
        Guid spId = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(spId);

        // Prepare the result
        VDS vds = new VDS();
        vds.setStoragePoolId(spId);
        List<VDS> result = Collections.singletonList(vds);

        // Mock the Dao
        when(vdsDaoMock.getAllForStoragePool(spId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(result);

        // Execute the query
        getQuery().executeQueryCommand();

        // Check the result
        assertEquals(result, getQuery().getQueryReturnValue().getReturnValue(), "Wrong roles returned");
    }
}
