package org.ovirt.engine.core.bll.memory.sdcomparators;

import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.junit.Before;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.Guid;

public class StorageDomainComparatorAbstractTest {

    protected Comparator<StorageDomain> comparator;
    protected StorageDomain storageDomain1;
    protected StorageDomain storageDomain2;

    @Before
    public void setUp() {
        storageDomain1 = initStorageDomain();
        storageDomain2 = initStorageDomain();
    }

    protected void assertSmallerThan(StorageDomain firstStorageDomain, StorageDomain secondStorageDomain) {
        assertTrue(compareStorageDomains(firstStorageDomain, secondStorageDomain) < 0);
    }

    protected void assertEqualsTo(StorageDomain firstStorageDomain, StorageDomain secondStorageDomain) {
        assertTrue(compareStorageDomains(firstStorageDomain, secondStorageDomain) == 0);
    }

    protected void assertBiggerThan(StorageDomain firstStorageDomain, StorageDomain secondStorageDomain) {
        assertTrue(compareStorageDomains(firstStorageDomain, secondStorageDomain) > 0);
    }

    private int compareStorageDomains(StorageDomain firstStorageDomain, StorageDomain secondStorageDomain) {
        return comparator.compare(firstStorageDomain, secondStorageDomain);
    }

    private StorageDomain initStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        return storageDomain;
    }
}
