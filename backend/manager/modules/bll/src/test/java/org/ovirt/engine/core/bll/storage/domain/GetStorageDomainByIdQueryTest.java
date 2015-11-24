package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/**
 * A test case for the {@link GetStorageDomainByIdQuery} class.
 * This test mocks away all the Daos, and just tests the flow of the query itself.
 */
public class GetStorageDomainByIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetStorageDomainByIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQuery() {
        // Create a storage domain for the test
        Guid storageDomainId = Guid.newGuid();
        StorageDomain expected = new StorageDomain();
        expected.setId(storageDomainId);

        when(getQueryParameters().getId()).thenReturn(storageDomainId);

        // Mock the Daos
        StorageDomainDao storageDoaminDaoMock = mock(StorageDomainDao.class);
        when(storageDoaminDaoMock.get(storageDomainId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(expected);
        when(getDbFacadeMockInstance().getStorageDomainDao()).thenReturn(storageDoaminDaoMock);

        getQuery().executeQueryCommand();

        // Assert we got the correct storage domain back
        StorageDomain actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong storage domain returned", expected, actual);
    }
}
