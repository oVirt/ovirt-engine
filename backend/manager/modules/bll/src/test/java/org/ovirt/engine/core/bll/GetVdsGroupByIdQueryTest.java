package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsGroupDao;

/**
 * A test case for {@link GetVdsGroupByIdQuery}.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetVdsGroupByIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetVdsGroupByIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQueryCommnad() {
        // Set up the expected data
        Guid vdsGroupID = Guid.newGuid();
        VDSGroup expected = new VDSGroup();
        expected.setId(vdsGroupID);

        // Mock the query's parameters
        when(getQueryParameters().getId()).thenReturn(vdsGroupID);

        // Mock the Daos
        VdsGroupDao vdsGropDaoMock = mock(VdsGroupDao.class);
        when(vdsGropDaoMock.get(vdsGroupID, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(expected);
        when(getDbFacadeMockInstance().getVdsGroupDao()).thenReturn(vdsGropDaoMock);

        getQuery().executeQueryCommand();

        VDSGroup actual = (VDSGroup) getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("wrong VDS Group", expected, actual);
    }
}
