package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsByVdsIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;

/**
 * A test case for {@link GetVdsCertificateSubjectByVdsIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the DAO occur.
 */
public class GetVdsCertificateSubjectByVdsIdQueryTest extends AbstractUserQueryTest<GetVdsByVdsIdParameters, GetVdsCertificateSubjectByVdsIdQuery<GetVdsByVdsIdParameters>> {
    @Test
    public void testExecuteQuery() {
        String hostName = RandomStringUtils.randomAlphabetic(10);
        String orgName = RandomStringUtils.randomAlphabetic(10);
        String expectedResult = "O=" + orgName + ",CN=" + hostName;

        doReturn(orgName).when(getQuery()).getOrganizationName();

        Guid vdsID = Guid.NewGuid();
        VDS vds = new VDS();
        vds.setId(vdsID);
        vds.setHostName(hostName);

        GetVdsByVdsIdParameters paramsMock = getQueryParameters();
        when(paramsMock.getVdsId()).thenReturn(vdsID);

        VdsDAO vdsDAOMock = mock(VdsDAO.class);
        when(vdsDAOMock.get(vdsID, getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn(vds);
        when(getDbFacadeMockInstance().getVdsDao()).thenReturn(vdsDAOMock);

        getQuery().executeQueryCommand();

        String result = (String) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong interfaces returned", expectedResult, result);
    }
}
