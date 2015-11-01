package org.ovirt.engine.core.bll.memory.sdfilters;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;

@RunWith(Theories.class)
public class StorageDomainTypeFilterTest extends StorageDomainFilterAbstractTest {

    private StorageDomainTypeFilter filter;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        filter = new StorageDomainTypeFilter();
    }

    @DataPoints
    public static StorageDomainType[] storageDomainTypes = StorageDomainType.values();

    @Theory
    public void testStorageDomainForMemoryIsValidOnlyForDataTypes(StorageDomainType storageDomainType) {
        storageDomain.setStorageDomainType(storageDomainType);
        assertEquals(filter.getPredicate(memoryDisks).test(storageDomain), storageDomainType.isDataDomain());
    }
}
