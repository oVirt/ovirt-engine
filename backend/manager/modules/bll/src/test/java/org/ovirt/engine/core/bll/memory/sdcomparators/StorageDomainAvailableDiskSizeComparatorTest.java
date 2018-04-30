package org.ovirt.engine.core.bll.memory.sdcomparators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith(RandomUtilsSeedingExtension.class)
public class StorageDomainAvailableDiskSizeComparatorTest extends StorageDomainComparatorAbstractTest {
    public StorageDomainAvailableDiskSizeComparatorTest() {
        comparator = MemoryStorageHandler.AVAILABLE_SIZE_COMPARATOR;
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
        setStorageDomainsAvailableDiskSize(availableDiskSize / 2, availableDiskSize);

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
