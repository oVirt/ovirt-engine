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
import org.ovirt.engine.core.dao.StorageDomainDao;

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

    @Mock
    private StorageDomainDao storageDomainDao;

    @Before
    public void setUp() {
        createStorageDomain();

        storageDomainDisks = new LinkedList<>();
        when(diskImageDao.getAllForStorageDomain(storageDomain.getId())).thenReturn(storageDomainDisks);

        storageDomainVmDisks = new LinkedList<>();
        when(diskVmElementDao.getAllDiskVmElementsByDisksIds(anyCollectionOf(Guid.class)))
                .thenReturn(storageDomainVmDisks);

        when(storageDomainDao.get(storageDomain.getId())).thenReturn(storageDomain);
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
    public void testExistingPassDiscardFunctionalityIsPreservedSdHasNoDiscardFunctionality() {
        setSdDiscardSupport(false, true);
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(new LinkedList<>(), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsPreservedSdHasNoDisks() {
        setSdFullDiscardSupport();
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(new LinkedList<>(), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsPreservedAllLunsHaveDiscardFunctionality() {
        setSdFullDiscardSupport();
        storageDomainDisks.add(new DiskImage());
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, true),
                createLunWithDiscardFunctionality(2048L, true)), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsPreservedNoDiskRequiresPassDiscardFunctionality() {
        setSdFullDiscardSupport();
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, false);
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(0L, true),
                createLunWithDiscardFunctionality(2048L, false)), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsNotPreservedSdDiscardSupportBreaks() {
        setSdDiscardSupport(true, false);
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(false, true); // This disk requires discard support from the storage domain.
        assertFalse(discardHelper.isExistingPassDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(0L, true), // This lun breaks the storage domain's discard support.
                createLunWithDiscardFunctionality(2048L, false)), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsNotPreservedSdDiscardZeroesTheDataSupportBreaks() {
        setSdDiscardSupport(true, true);
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, true); // This disk requires the storage domain's support for discard zeroes the data.
        assertFalse(discardHelper.isExistingPassDiscardFunctionalityPreserved(Arrays.asList(
                // This lun breaks the storage domain's support for discard zeroes the data.
                createLunWithDiscardFunctionality(1024L, false),
                createLunWithDiscardFunctionality(2048L, true)), storageDomain));
    }

    @Test
    public void testExistingDiscardAfterDeleteFunctionalityPreservedNoDiscardAfterDeleteFunctionality() {
        storageDomain.setDiscardAfterDelete(false);
        assertTrue(discardHelper.isExistingDiscardAfterDeleteFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(0L, false),
                createLunWithDiscardFunctionality(0L, true)), storageDomain));
    }

    @Test
    public void testExistingDiscardAfterDeleteFunctionalityPreservedAllLunsHaveDiscardAfterDeleteFunctionality() {
        storageDomain.setDiscardAfterDelete(true);
        assertTrue(discardHelper.isExistingDiscardAfterDeleteFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, false),
                createLunWithDiscardFunctionality(2048L, true)), storageDomain));
    }

    @Test
    public void testExistingDiscardAfterDeleteFunctionalityPreservedDiscardAfterDeleteFunctionalityBreaks() {
        storageDomain.setDiscardAfterDelete(true);
        assertFalse(discardHelper.isExistingDiscardAfterDeleteFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardFunctionality(1024L, false),
                createLunWithDiscardFunctionality(0L, true)), storageDomain));
    }

    @Test
    public void testGetLunsThatBreakPassDiscardFunctionalityPassDiscardBreaks() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(false, true); // This disk requires pass discard support.
        LUNs lunThatBreaksDiscardSupport = createLunWithDiscardFunctionality(0L, false);
        assertGetLunsThatBreakPassDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, false), lunThatBreaksDiscardSupport),
                Collections.singletonList(lunThatBreaksDiscardSupport));
    }

    @Test
    public void testGetLunsThatBreakPassDiscardFunctionalityDiscardZeroesTheDataBreaks() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, true); // This disk requires discard zeroes the data support.
        LUNs lunThatBreaksDiscardZeroesTheDataSupport = createLunWithDiscardFunctionality(1024L, false);
        assertGetLunsThatBreakPassDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, true), lunThatBreaksDiscardZeroesTheDataSupport),
                Collections.singletonList(lunThatBreaksDiscardZeroesTheDataSupport));
    }

    @Test
    public void testGetLunsThatBreakPassDiscardFunctionalityFullDiscardFunctionalityBreaks() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, true); // This disk requires both pass discard and discard zeroes the data support.
        LUNs lunThatBreaksDiscardSupport = createLunWithDiscardFunctionality(0L, false);
        LUNs lunThatBreaksDiscardZeroesTheDataSupport = createLunWithDiscardFunctionality(1024L, false);
        assertGetLunsThatBreakPassDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, true), lunThatBreaksDiscardSupport,
                        lunThatBreaksDiscardZeroesTheDataSupport),
                Arrays.asList(lunThatBreaksDiscardSupport, lunThatBreaksDiscardZeroesTheDataSupport));
    }

    @Test
    public void testGetLunsThatBreakPassDiscardFunctionalityDiscardFunctionalityDoesntBreak() {
        createVmDiskOnSd(false, false); // This disk does not require any discard functionality.
        createVmDiskOnSd(true, true); // This disk requires both pass discard and discard zeroes the data support.
        assertGetLunsThatBreakPassDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardFunctionality(1024L, true),
                        createLunWithDiscardFunctionality(2048L, true)),
                Collections.emptyList());
    }

    @Test
    public void testGetLunsThatBreakDiscardAfterDeleteSupportStorageDomainDiscardAfterDeleteDisabled() {
        storageDomain.setDiscardAfterDelete(false);
        assertGetLunsThatBreakDiscardAfterDeleteSupportContainsExpectedLuns(
                Arrays.asList(
                        createLunWithDiscardFunctionality(1024L, true),
                        createLunWithDiscardFunctionality(0L, false)),
                Collections.emptyList());
    }

    @Test
    public void testGetLunsThatBreakDiscardAfterDeleteSupportDiscardAfterDeleteBreaks() {
        storageDomain.setDiscardAfterDelete(true);
        LUNs lunThatBreaksDiscardSupport = createLunWithDiscardFunctionality(0L, true);
                assertGetLunsThatBreakDiscardAfterDeleteSupportContainsExpectedLuns(
                Arrays.asList(
                        createLunWithDiscardFunctionality(1024L, true),
                        lunThatBreaksDiscardSupport),
                        Collections.singletonList(lunThatBreaksDiscardSupport));
    }

    @Test
    public void testGetLunsThatBreakDiscardAfterDeleteSupportDiscardAfterDeleteDoesntBreak() {
        storageDomain.setDiscardAfterDelete(true);
        assertGetLunsThatBreakDiscardAfterDeleteSupportContainsExpectedLuns(
                Arrays.asList(
                        createLunWithDiscardFunctionality(1024L, true),
                        createLunWithDiscardFunctionality(2048L, false)),
                Collections.emptyList());
    }

    private void createStorageDomain() {
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
    }

    private void setSdDiscardSupport(boolean sdSupportsDiscard, boolean sdDiscardZeroesData) {
        storageDomain.setSupportsDiscard(sdSupportsDiscard);
        storageDomain.setSupportsDiscardZeroesData(sdDiscardZeroesData);
    }

    private LUNs createLunWithDiscardFunctionality(Long discardMaxSize, Boolean discardZeroesData) {
        LUNs lun = new LUNs();
        lun.setDiscardMaxSize(discardMaxSize);
        lun.setDiscardZeroesData(discardZeroesData);
        return lun;
    }

    private void setSdFullDiscardSupport() {
        setSdDiscardSupport(true, true);
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

    private void assertGetLunsThatBreakPassDiscardFunctionalityContainsExpectedLuns(Collection<LUNs> luns,
            Collection<LUNs> expectedLunsThatBreakPassDiscardFunctionality) {
        Collection<LUNs> lunsThatBreakPassDiscardFunctionality =
                discardHelper.getLunsThatBreakPassDiscardSupport(luns, storageDomain.getId());
        assertTrue(CollectionUtils.isEqualCollection(lunsThatBreakPassDiscardFunctionality,
                expectedLunsThatBreakPassDiscardFunctionality));
    }

    private void assertGetLunsThatBreakDiscardAfterDeleteSupportContainsExpectedLuns(Collection<LUNs> luns,
            Collection<LUNs> expectedLunsThatBreakDiscardAfterDeleteSupport) {
        Collection<LUNs> lunsThatBreakDiscardAfterDeleteSupport =
                discardHelper.getLunsThatBreakDiscardAfterDeleteSupport(luns, storageDomain.getId());
        assertTrue(CollectionUtils.isEqualCollection(lunsThatBreakDiscardAfterDeleteSupport,
                expectedLunsThatBreakDiscardAfterDeleteSupport));
    }
}
