package org.ovirt.engine.core.bll.memory.sdcomparators;

import org.junit.ClassRule;
import org.junit.Test;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

public class StorageDomainAvailableDiskSizeComparatorTest extends StorageDomainComparatorAbstractTest {

    @ClassRule
    public static RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    public StorageDomainAvailableDiskSizeComparatorTest() {
        comparator = new StorageDomainAvailableDiskSizeComparator();
    }

    @Test
    public void compareWhenSizesAreEqual() {
        int availableDiskSize = getRandomNumberForTest();
        setStorageDomainsAvailableDiskSize(availableDiskSize, availableDiskSize);

        assertEqualsTo(storageDomain1, storageDomain2);
    }

    @Test
    public void compareWhenSizesAreNotEqual() {
        int availableDiskSize = getRandomNumberForTest();
        setStorageDomainsAvailableDiskSize(availableDiskSize, availableDiskSize / 2);

        assertBiggerThan(storageDomain1, storageDomain2);
        assertSmallerThan(storageDomain2, storageDomain1);
    }

    private int getRandomNumberForTest() {
        return RandomUtils.instance().nextInt(2, Integer.MAX_VALUE);
    }

    private void setStorageDomainsAvailableDiskSize(int storageDomain1AvailableDiskSize,
            int storageDomain2AvailableDiskSize) {
        storageDomain1.setAvailableDiskSize(storageDomain1AvailableDiskSize);
        storageDomain2.setAvailableDiskSize(storageDomain2AvailableDiskSize);
    }
}
