package org.ovirt.engine.core.bll.memory.sdfilters;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;

@RunWith(Theories.class)
public class StorageDomainStatusFilterTest extends StorageDomainFilterAbstractTest {

    private StorageDomainStatusFilter filter;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        filter = new StorageDomainStatusFilter();
    }

    @DataPoints
    public static StorageDomainStatus[] storageDomainStatuses = StorageDomainStatus.values();

    @Theory
    public void testStorageDomainForMemoryIsValidOnlyForActiveStatus(StorageDomainStatus storageDomainStatus) {
        storageDomain.setStatus(storageDomainStatus);
        assertEquals(filter.getPredicate(memoryDisks).test(storageDomain),
                storageDomainStatus == StorageDomainStatus.Active);
    }
}
