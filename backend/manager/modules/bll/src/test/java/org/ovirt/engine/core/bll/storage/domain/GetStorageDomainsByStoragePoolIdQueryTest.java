package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StorageDomainDao;

/** A test case for the {@link GetStorageDomainsByStoragePoolIdQuery} class. */
public class GetStorageDomainsByStoragePoolIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetStorageDomainsByStoragePoolIdQuery<IdQueryParameters>> {

    @Test
    public void testExecuteQuery() {
        Guid storagePoolID = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(storagePoolID);

        StorageDomain domain = new StorageDomain();

        StorageDomainDao storageDomainDaoMock = mock(StorageDomainDao.class);
        when(storageDomainDaoMock.getAllForStoragePool
                (storagePoolID,
                        getUser().getId(),
                        getQueryParameters().isFiltered())).
                thenReturn(Collections.singletonList(domain));

        DbFacade dbFacadeMock = getDbFacadeMockInstance();
        when(dbFacadeMock.getStorageDomainDao()).thenReturn(storageDomainDaoMock);

        getQuery().executeQueryCommand();

        List<StorageDomain> result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals("Wrong number of domains returned", 1, result.size());
        assertEquals("Wrong domain returned", domain, result.get(0));
    }
}
