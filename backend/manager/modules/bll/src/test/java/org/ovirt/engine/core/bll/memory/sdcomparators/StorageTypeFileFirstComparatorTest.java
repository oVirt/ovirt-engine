package org.ovirt.engine.core.bll.memory.sdcomparators;

import static org.junit.Assert.assertEquals;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;

@RunWith(Theories.class)
public class StorageTypeFileFirstComparatorTest extends StorageDomainComparatorAbstractTest {

    @DataPoints
    public static StorageType[] storageTypes = StorageType.values();

    public StorageTypeFileFirstComparatorTest() {
        comparator = MemoryStorageHandler.FILE_FIRST_COMPARATOR;
    }

    @Theory
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
