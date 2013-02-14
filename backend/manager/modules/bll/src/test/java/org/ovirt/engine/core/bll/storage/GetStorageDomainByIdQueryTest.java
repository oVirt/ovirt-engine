package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;

/**
 * A test case for the {@link GetStorageDomainByIdQuery} class.
 * This test mocks away all the DAOs, and just tests the flow of the query itself.
 */
public class GetStorageDomainByIdQueryTest extends AbstractUserQueryTest<StorageDomainQueryParametersBase, GetStorageDomainByIdQuery<StorageDomainQueryParametersBase>> {

    @Test
    public void testExecuteQuery() {
        // Create a storage domain for the test
        Guid storageDomainId = Guid.NewGuid();
        StorageDomain expected = new StorageDomain();
        expected.setId(storageDomainId);

        when(getQueryParameters().getStorageDomainId()).thenReturn(storageDomainId);

        // Mock the DAOs
        StorageDomainDAO storageDoaminDAOMock = mock(StorageDomainDAO.class);
        when(storageDoaminDAOMock.get(storageDomainId, getUser().getUserId(), getQueryParameters().isFiltered())).thenReturn(expected);
        when(getDbFacadeMockInstance().getStorageDomainDao()).thenReturn(storageDoaminDAOMock);

        getQuery().executeQueryCommand();

        // Assert we got the correct storage domain back
        StorageDomain actual = (StorageDomain) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong storage domain returned", expected, actual);
    }
}
