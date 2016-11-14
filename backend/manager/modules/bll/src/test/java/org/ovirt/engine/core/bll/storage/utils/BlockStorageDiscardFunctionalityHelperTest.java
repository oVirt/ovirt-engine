package org.ovirt.engine.core.bll.storage.utils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;

@RunWith(MockitoJUnitRunner.class)
public class BlockStorageDiscardFunctionalityHelperTest {

    private StorageDomain storageDomain;
    private List<DiskImage> storageDomainDisks;
    private List<DiskVmElement> storageDomainVmDisks;

    @InjectMocks
    public BlockStorageDiscardFunctionalityHelper discardHelper;

    @Mock
    public DiskImageDao diskImageDao;

    @Mock
    public DiskVmElementDao diskVmElementDao;

    @Before
    public void setUp() {
        createStorageDomain();

        storageDomainDisks = new LinkedList<>();
        when(diskImageDao.getAllForStorageDomain(storageDomain.getId())).thenReturn(storageDomainDisks);

        storageDomainVmDisks = new LinkedList<>();
        when(diskVmElementDao.getAllDiskVmElementsByDisksIds(anyCollectionOf(Guid.class)))
                .thenReturn(storageDomainVmDisks);
    }

    @Test
    public void testAllLunsSupportDiscardSucceeds() {
        assertTrue(discardHelper.allLunsSupportDiscard(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(2048L, false))));
    }

    @Test
    public void testAllLunsSupportDiscardFailsOneLunDoesNotSupport() {
        assertFalse(discardHelper.allLunsSupportDiscard(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(2048L, false),
                createLunWithDiscardFunctionality(0L, true)))); // This lun does not support discard.
    }

    @Test
    public void testAllLunsSupportDiscardFailsOneLunHasNullValue() {
        assertFalse(discardHelper.allLunsSupportDiscard(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(2048L, false),
                createLunWithDiscardFunctionality(null, true)))); // This lun does not support discard.
    }

    @Test
    public void testAllLunsHaveDiscardZeroesTheDataSupportSucceeds() {
        assertTrue(discardHelper.allLunsHaveDiscardZeroesTheDataSupport(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(0L, true))));
    }

    @Test
    public void testAllLunsHaveDiscardZeroesTheDataSupportFailsOneLunDoesNotSupport() {
        assertFalse(discardHelper.allLunsHaveDiscardZeroesTheDataSupport(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(0L, true),
                createLunWithDiscardFunctionality(2048L, false)))); // This lun does not support discard zeroes the data.
    }

    @Test
    public void testAllLunsHaveDiscardZeroesTheDataSupportFailsOneLunHasNullValue() {
        assertFalse(discardHelper.allLunsHaveDiscardZeroesTheDataSupport(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(0L, true),
                createLunWithDiscardFunctionality(2048L, null)))); // This lun does not support discard zeroes the data.
    }

    @Test
    public void testVmDiskWithPassDiscardExistsSucceeds() {
        assertTrue(discardHelper.vmDiskWithPassDiscardExists(Arrays.asList(
                createVmDisk(Guid.newGuid(), false),
                createVmDisk(Guid.newGuid(), true))));
    }

    @Test
    public void testVmDiskWithPassDiscardExistsFails() {
        assertFalse(discardHelper.vmDiskWithPassDiscardExists(Arrays.asList(
                createVmDisk(Guid.newGuid(), false),
                createVmDisk(Guid.newGuid(), false))));
    }

    @Test
    public void testVmDiskWithPassDiscardAndWadExistsSucceeds() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, false);
        createVmDiskOnSd(false, true);
        createVmDiskOnSd(true, true);
        assertTrue(discardHelper.vmDiskWithPassDiscardAndWadExists(storageDomainDisks, storageDomainVmDisks));
    }

    @Test
    public void testVmDiskWithPassDiscardAndWadExistsFails() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, false);
        createVmDiskOnSd(false, true);
        assertFalse(discardHelper.vmDiskWithPassDiscardAndWadExists(storageDomainDisks, storageDomainVmDisks));
    }

    @Test
    public void testExistingDiscardFunctionalityIsPreservedSdHasNoDiscardFunctionality() {
        setSdDiscardFunctionality(false, true);
        assertTrue(discardHelper.isExistingDiscardFunctionalityPreserved(new LinkedList<>(), storageDomain));
    }

    @Test
    public void testExistingDiscardFunctionalityIsPreservedSdHasNoDisks() {
        setSdFullDiscardFunctionality();
        assertTrue(discardHelper.isExistingDiscardFunctionalityPreserved(new LinkedList<>(), storageDomain));
    }

    @Test
    public void testExistingDiscardFunctionalityIsPreservedAllLunsHaveDiscardFunctionality() {
        setSdFullDiscardFunctionality();
        storageDomainDisks.add(new DiskImage());
        assertTrue(discardHelper.isExistingDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(2048L, true)), storageDomain));
    }

    @Test
    public void testExistingDiscardFunctionalityIsPreservedNoDiskRequiresDiscardFunctionality() {
        setSdFullDiscardFunctionality();
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, false);
        assertTrue(discardHelper.isExistingDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(0L, true),
                createLunWithDiscardFunctionality(2048L, false)), storageDomain));
    }

    @Test
    public void testExistingDiscardFunctionalityIsNotPreservedSdDiscardSupportBreaks() {
        setSdDiscardFunctionality(true, false);
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(false, true); // This disk requires discard support from the storage domain.
        assertFalse(discardHelper.isExistingDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(0L, true), // This lun breaks the storage domain's discard support.
                createLunWithDiscardFunctionality(2048L, false)), storageDomain));
    }

    @Test
    public void testExistingDiscardFunctionalityIsNotPreservedSdDiscardZeroesTheDataSupportBreaks() {
        setSdDiscardFunctionality(true, true);
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, true); // This disk requires the storage domain's support for discard zeroes the data.
        assertFalse(discardHelper.isExistingDiscardFunctionalityPreserved(Arrays.asList(
                // This lun breaks the storage domain's support for discard zeroes the data.
                createLunWithDiscardFunctionality(1024L, false),
                createLunWithDiscardFunctionality(2048L, true)), storageDomain));
    }

    @Test
    public void testGetLunsThatBreakDiscardFunctionalityPassDiscardBreaks() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(false, true); // This disk requires pass discard support.
        LUNs lunThatBreaksDiscardSupport = createLunWithDiscardFunctionality(0L, false);
        assertGetLunsThatBreakDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, false), lunThatBreaksDiscardSupport),
                Collections.singletonList(lunThatBreaksDiscardSupport));
    }

    @Test
    public void testGetLunsThatBreakDiscardFunctionalityDiscardZeroesTheDataBreaks() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, true); // This disk requires discard zeroes the data support.
        LUNs lunThatBreaksDiscardZeroesTheDataSupport = createLunWithDiscardFunctionality(1024L, false);
        assertGetLunsThatBreakDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, true), lunThatBreaksDiscardZeroesTheDataSupport),
                Collections.singletonList(lunThatBreaksDiscardZeroesTheDataSupport));
    }

    @Test
    public void testGetLunsThatBreakDiscardFunctionalityFullDiscardFunctionalityBreaks() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, true); // This disk requires both pass discard and discard zeroes the data support.
        LUNs lunThatBreaksDiscardSupport = createLunWithDiscardFunctionality(0L, false);
        LUNs lunThatBreaksDiscardZeroesTheDataSupport = createLunWithDiscardFunctionality(1024L, false);
        assertGetLunsThatBreakDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, true), lunThatBreaksDiscardSupport,
                        lunThatBreaksDiscardZeroesTheDataSupport),
                Arrays.asList(lunThatBreaksDiscardSupport, lunThatBreaksDiscardZeroesTheDataSupport));
    }

    @Test
    public void testGetLunsThatBreakDiscardFunctionalityDiscardFunctionalityDoesntBreak() {
        createVmDiskOnSd(false, false); // This disk does not require any discard functionality.
        createVmDiskOnSd(true, true); // This disk requires both pass discard and discard zeroes the data support.
        assertGetLunsThatBreakDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, true),
                        createLunWithDiscardFunctionality(2048L, true)),
                Collections.emptyList());
    }

    private void createStorageDomain() {
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
    }

    private void setSdDiscardFunctionality(boolean sdSupportsDiscard, boolean sdDiscardZeroesData) {
        storageDomain.setSupportsDiscard(sdSupportsDiscard);
        storageDomain.setSupportsDiscardZeroesData(sdDiscardZeroesData);
    }

    private LUNs createLunWithDiscardFunctionality(Long discardMaxSize, Boolean discardZeroesData) {
        LUNs lun = new LUNs();
        lun.setDiscardMaxSize(discardMaxSize);
        lun.setDiscardZeroesData(discardZeroesData);
        return lun;
    }

    private void setSdFullDiscardFunctionality() {
        setSdDiscardFunctionality(true, true);
    }

    private DiskVmElement createVmDisk(Guid diskId, boolean passDiscard) {
        DiskVmElement diskVmElement = new DiskVmElement(diskId, Guid.newGuid());
        diskVmElement.setPassDiscard(passDiscard);
        return diskVmElement;
    }

    private void createVmDiskOnSd(boolean wipeAfterDelete, boolean passDiscard) {
        DiskImage disk = new DiskImage();
        Guid diskId = Guid.newGuid();
        disk.setId(diskId);
        disk.setWipeAfterDelete(wipeAfterDelete);
        storageDomainDisks.add(disk);
        storageDomainVmDisks.add(createVmDisk(diskId, passDiscard));
    }

    private void assertGetLunsThatBreakDiscardFunctionalityContainsExpectedLuns(Collection<LUNs> luns,
            Collection<LUNs> expectedLunsThatBreakDiscardFunctionality) {
        Collection<LUNs> lunsThatBreakDiscardFunctionality =
                discardHelper.getLunsThatBreakDiscardFunctionality(luns, storageDomain.getId());
        assertTrue(CollectionUtils.isEqualCollection(lunsThatBreakDiscardFunctionality,
                expectedLunsThatBreakDiscardFunctionality));
    }
}
