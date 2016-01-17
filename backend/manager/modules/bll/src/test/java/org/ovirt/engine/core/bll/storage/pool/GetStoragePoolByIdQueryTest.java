package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * A test case for {@link GetStoragePoolByIdQuery}.
 * It does not test database implementation, but rather tests that the right delegations to the Dao occur.
 */
public class GetStoragePoolByIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetStoragePoolByIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQuery() {
        Guid storagePoolID = Guid.newGuid();
        StoragePool expectedResult = mock(StoragePool.class);

        IdQueryParameters paramsMock = getQueryParameters();
        when(paramsMock.getId()).thenReturn(storagePoolID);

        StoragePoolDao storagePoolDaoMock = mock(StoragePoolDao.class);
        when(storagePoolDaoMock.get(storagePoolID, getUser().getId(), paramsMock.isFiltered())).thenReturn(expectedResult);

        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        when(dbFacadeMock.getStoragePoolDao()).thenReturn(storagePoolDaoMock);

        getQuery().executeQueryCommand();

        StoragePool result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong storage pool returned", expectedResult, result);
    }
}
