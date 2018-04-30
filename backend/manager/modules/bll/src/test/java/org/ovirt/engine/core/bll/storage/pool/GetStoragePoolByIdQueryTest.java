package org.ovirt.engine.core.bll.storage.pool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * A test case for {@link GetStoragePoolByIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetStoragePoolByIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetStoragePoolByIdQuery<IdQueryParameters>> {

    private static final Guid STORAGE_POOL_ID = Guid.newGuid();
    private static final Guid MAC_POOL_ID = Guid.newGuid();

    @Mock
    private StoragePoolDao storagePoolDaoMock;

    @Mock
    private DcSingleMacPoolFinder dcSingleMacPoolFinderMock;

    @Test
    public void testExecuteQuery() {
        StoragePool expectedResult = createStoragePool();

        when(getQueryParameters().getId()).thenReturn(STORAGE_POOL_ID);
        when(storagePoolDaoMock.get(STORAGE_POOL_ID, getUser().getId(), getQueryParameters().isFiltered()))
                .thenReturn(expectedResult);
        when(dcSingleMacPoolFinderMock.find(STORAGE_POOL_ID)).thenReturn(MAC_POOL_ID);

        getQuery().executeQueryCommand();
        StoragePool result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(expectedResult, result, "Wrong storage pool returned");
        assertThat(result.getMacPoolId(), is(MAC_POOL_ID));
    }

    private StoragePool createStoragePool() {
        StoragePool expectedResult = new StoragePool();
        expectedResult.setId(STORAGE_POOL_ID);
        return expectedResult;
    }

}
