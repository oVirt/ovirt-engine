package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractUserQueryTest;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/** A test case for the {@link GetStorageDomainsByStoragePoolIdQuery} class. */
public class GetStorageDomainsByStoragePoolIdQueryTest extends AbstractUserQueryTest<IdQueryParameters, GetStorageDomainsByStoragePoolIdQuery<IdQueryParameters>> {
    @Mock
    private StorageDomainDao storageDomainDaoMock;

    @Test
    public void testExecuteQuery() {
        Guid storagePoolID = Guid.newGuid();
        when(getQueryParameters().getId()).thenReturn(storagePoolID);

        StorageDomain domain = new StorageDomain();

        when(storageDomainDaoMock.getAllForStoragePool
                (storagePoolID,
                        getUser().getId(),
                        getQueryParameters().isFiltered())).
                thenReturn(Collections.singletonList(domain));

        getQuery().executeQueryCommand();

        List<StorageDomain> result = getQuery().getQueryReturnValue().getReturnValue();
        assertEquals(1, result.size(), "Wrong number of domains returned");
        assertEquals(domain, result.get(0), "Wrong domain returned");
    }
}
