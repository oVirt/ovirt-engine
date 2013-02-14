package org.ovirt.engine.core.bll.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.GetImageByImageIdParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDAO;

/** A test case for the {@link GetStorageDomainsByImageIdQuery} class. */
public class GetStorageDomainsByImageIdQueryTest extends AbstractQueryTest<GetImageByImageIdParameters, GetStorageDomainsByImageIdQuery<GetImageByImageIdParameters>> {

    @Test
    public void testExecuteQueryCommandWithEmptyList() {
        // Set up the query parameters
        Guid imageId = Guid.NewGuid();
        when(params.getImageId()).thenReturn(imageId);

        // Set up the DAOs
        List<StorageDomain> expected = Collections.singletonList(new StorageDomain());
        StorageDomainDAO storageDomainDAOMock = mock(StorageDomainDAO.class);
        when(getDbFacadeMockInstance().getStorageDomainDao()).thenReturn(storageDomainDAOMock);
        when(storageDomainDAOMock.getAllStorageDomainsByImageId(imageId)).thenReturn(expected);

        // Run the query
        GetStorageDomainsByImageIdQuery<GetImageByImageIdParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testExecuteQueryCommandWithMultipleStorageList() {
        // Set up the query parameters
        Guid imageId = Guid.NewGuid();
        when(params.getImageId()).thenReturn(imageId);

        StorageDomain firstStorageDomain =
                new StorageDomain(Guid.NewGuid(),
                        Guid.NewGuid().toString(),
                        "FirstStorage",
                        Guid.NewGuid(),
                        0,
                        0,
                        StorageDomainStatus.Active,
                        "StoragePoolName",
                        0,
                        0,
                        "First Storage Description");
        StorageDomain secondStorageDomain =
                new StorageDomain(Guid.NewGuid(),
                        Guid.NewGuid().toString(),
                        "SecondStorage",
                        Guid.NewGuid(),
                        0,
                        0,
                        StorageDomainStatus.Active,
                        "StoragePoolName",
                        0,
                        0,
                        "Second Storage Description");
        List<StorageDomain> expected = new ArrayList<StorageDomain>();
        expected.add(firstStorageDomain);
        expected.add(secondStorageDomain);

        // Set up the DAOs
        StorageDomainDAO storageDomainDAOMock = mock(StorageDomainDAO.class);
        when(getDbFacadeMockInstance().getStorageDomainDao()).thenReturn(storageDomainDAOMock);
        when(storageDomainDAOMock.getAllStorageDomainsByImageId(imageId)).thenReturn(expected);

        // Run the query
        GetStorageDomainsByImageIdQuery<GetImageByImageIdParameters> query = getQuery();
        query.executeQueryCommand();

        // Assert the result
        assertEquals("Wrong result returned", expected, getQuery().getQueryReturnValue().getReturnValue());
    }
}
