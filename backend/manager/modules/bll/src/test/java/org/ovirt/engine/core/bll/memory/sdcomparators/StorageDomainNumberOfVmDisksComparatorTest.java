package org.ovirt.engine.core.bll.memory.sdcomparators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class StorageDomainNumberOfVmDisksComparatorTest extends StorageDomainComparatorAbstractTest {

    private List<StorageDomain> storageDomains;
    private DiskImage vmDisk1;
    private DiskImage vmDisk2;
    private DiskImage vmDisk3;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        storageDomains = Arrays.asList(storageDomain1, storageDomain2);
        vmDisk1 = new DiskImage();
        vmDisk2 = new DiskImage();
        vmDisk3 = new DiskImage();
    }

    @Test
    public void compareWhenStorageDomainsHaveNoDisks() {
        initComparator();
        assertEqualsTo(storageDomain1, storageDomain2);
    }

    @Test
    public void compareWhenSizesAreEqual() {
        attachVmDisksToStorageDomain(storageDomain1, vmDisk1);
        attachVmDisksToStorageDomain(storageDomain2, vmDisk2);
        initComparator(vmDisk1, vmDisk2);

        assertEqualsTo(storageDomain1, storageDomain2);
    }

    @Test
    public void compareWhenSizesAreNotEqual() {
        attachVmDisksToStorageDomain(storageDomain1, vmDisk1, vmDisk2);
        attachVmDisksToStorageDomain(storageDomain2, vmDisk3);
        initComparator(vmDisk1, vmDisk2, vmDisk3);

        assertBiggerThan(storageDomain1, storageDomain2);
        assertSmallerThan(storageDomain2, storageDomain1);
    }

    private void attachVmDisksToStorageDomain(StorageDomain storageDomain, DiskImage... vmDisks) {
        for (DiskImage diskImage : vmDisks) {
            diskImage.setStorageIds(new ArrayList<>(Arrays.asList(storageDomain.getId())));
        }
    }

    private void initComparator(DiskImage... vmDisks) {
        comparator = new StorageDomainNumberOfVmDisksComparator(storageDomains, Arrays.asList(vmDisks));
    }
}
