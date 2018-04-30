package org.ovirt.engine.core.bll.memory.sdcomparators;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

public class StorageTypeFileFirstComparatorTest extends StorageDomainComparatorAbstractTest {

    public StorageTypeFileFirstComparatorTest() {
        comparator = MemoryStorageHandler.FILE_FIRST_COMPARATOR;
    }

    @ParameterizedTest
    @EnumSource(value = StorageType.class)
    public void testCompare(StorageType storageType) {
        storageDomain1.setStorageType(storageType);
        for (StorageType storageType2 : StorageType.values()) {
            storageDomain2.setStorageType(storageType2);
            int compareTypes = -1 * Boolean.compare(storageType.isFileDomain(), storageType2.isFileDomain());
            int comparatorReturnValue = comparator.compare(storageDomain1, storageDomain2);
            assertEquals(compareTypes < 0, comparatorReturnValue < 0);
            assertEquals(compareTypes == 0, comparatorReturnValue == 0);
            assertEquals(compareTypes > 0, comparatorReturnValue > 0);
        }
    }
}
