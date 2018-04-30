package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @Mock
    private StorageDomainDao storageDoaminDaoMock;

    @Test
    public void testExecuteQuery() {
        // Create a storage domain for the test
        Guid storageDomainId = Guid.newGuid();
        StorageDomain expected = new StorageDomain();
        expected.setId(storageDomainId);

        when(getQueryParameters().getId()).thenReturn(storageDomainId);

        // Mock the Daos
        when(storageDoaminDaoMock.get(storageDomainId, getUser().getId(), getQueryParameters().isFiltered())).thenReturn(expected);

        getQuery().executeQueryCommand();

        // Assert we got the correct storage domain back
        StorageDomain actual = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expected, actual, "Wrong storage domain returned");
    }
}
