package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/** A test case for the {@link GetStorageDomainsByImageIdQuery} class. */
public class GetStorageDomainsByImageIdQueryTest extends AbstractQueryTest<IdQueryParameters, GetStorageDomainsByImageIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQueryCommandWithEmptyList() {
        // Set up the query parameters
        Guid imageId = Guid.newGuid();
        when(params.getId()).thenReturn(imageId);

        // Set up the Daos
        List<StorageDomain> expected = Collections.singletonList(new StorageDomain());
        StorageDomainDao storageDomainDaoMock = mock(StorageDomainDao.class);
        when(getDbFacadeMockInstance().getStorageDomainDao()).thenReturn(storageDomainDaoMock);
        when(storageDomainDaoMock.getAllStorageDomainsByImageId(imageId)).thenReturn(expected);

        // Run the query
        GetStorageDomainsByImageIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryCommandWithMultipleStorageList() {
        // Set up the query parameters
        Guid imageId = Guid.newGuid();
        when(params.getId()).thenReturn(imageId);

        StorageDomain firstStorageDomain = new StorageDomain();
        firstStorageDomain.setId(Guid.newGuid());
        StorageDomain secondStorageDomain = new StorageDomain();
        secondStorageDomain.setId(Guid.newGuid());

        List<StorageDomain> expected = new ArrayList<>();
        expected.add(firstStorageDomain);
        expected.add(secondStorageDomain);

        // Set up the Daos
        StorageDomainDao storageDomainDaoMock = mock(StorageDomainDao.class);
        when(getDbFacadeMockInstance().getStorageDomainDao()).thenReturn(storageDomainDaoMock);
        when(storageDomainDaoMock.getAllStorageDomainsByImageId(imageId)).thenReturn(expected);

        // Run the query
        GetStorageDomainsByImageIdQuery<IdQueryParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
