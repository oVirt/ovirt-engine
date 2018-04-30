package org.ovirt.engine.core.bll.storage.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BlockStorageDiscardFunctionalityHelperTest {

    private StorageDomain storageDomain;
    private List<DiskImage> storageDomainDisks;
    private List<DiskVmElement> storageDomainVmDisks;

    @InjectMocks
    private BlockStorageDiscardFunctionalityHelper discardHelper;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @BeforeEach
    public void setUp() {
        createStorageDomain();

        storageDomainDisks = new LinkedList<>();
        when(diskImageDao.getAllForStorageDomain(storageDomain.getId())).thenReturn(storageDomainDisks);

        storageDomainVmDisks = new LinkedList<>();
        when(diskVmElementDao.getAllDiskVmElementsByDisksIds(anyCollection())).thenReturn(storageDomainVmDisks);
    }

    @Test
    public void testAllLunsSupportDiscardSucceeds() {
        assertTrue(discardHelper.allLunsSupportDiscard(Arrays.asList(
                createLunWithDiscardSupport(1024L),
                createLunWithDiscardSupport(2048L))));
    }

    @Test
    public void testAllLunsSupportDiscardFailsOneLunDoesNotSupport() {
        assertFalse(discardHelper.allLunsSupportDiscard(Arrays.asList(
                createLunWithDiscardSupport(1024L),
                createLunWithDiscardSupport(2048L),
                createLunWithDiscardSupport(0L)))); // This lun does not support discard.
    }

    @Test
    public void testAllLunsSupportDiscardFailsOneLunHasNullValue() {
        assertFalse(discardHelper.allLunsSupportDiscard(Arrays.asList(
                createLunWithDiscardSupport(1024L),
                createLunWithDiscardSupport(2048L),
                createLunWithDiscardSupport(null)))); // This lun does not support discard.
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
    public void testExistingPassDiscardFunctionalityIsPreservedSdHasNoDiscardFunctionality() {
        storageDomain.setSupportsDiscard(false);
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(new LinkedList<>(), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsPreservedSdHasNoDisks() {
        storageDomain.setSupportsDiscard(true);
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(new LinkedList<>(), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsPreservedAllLunsHaveDiscardFunctionality() {
        storageDomain.setSupportsDiscard(true);
        storageDomainDisks.add(new DiskImage());
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardSupport(1024L),
                createLunWithDiscardSupport(2048L)), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsPreservedNoDiskRequiresPassDiscardFunctionality() {
        storageDomain.setSupportsDiscard(true);
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(true, false);
        assertTrue(discardHelper.isExistingPassDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardSupport(0L),
                createLunWithDiscardSupport(2048L)), storageDomain));
    }

    @Test
    public void testExistingPassDiscardFunctionalityIsNotPreservedSdDiscardSupportBreaks() {
        storageDomain.setSupportsDiscard(true);
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(false, true); // This disk requires discard support from the storage domain.
        assertFalse(discardHelper.isExistingPassDiscardFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardSupport(0L), // This lun breaks the storage domain's discard support.
                createLunWithDiscardSupport(2048L)), storageDomain));
    }

    @Test
    public void testExistingDiscardAfterDeleteFunctionalityPreservedNoDiscardAfterDeleteFunctionality() {
        storageDomain.setDiscardAfterDelete(false);
        assertTrue(discardHelper.isExistingDiscardAfterDeleteFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardSupport(0L),
                createLunWithDiscardSupport(0L)), storageDomain));
    }

    @Test
    public void testExistingDiscardAfterDeleteFunctionalityPreservedAllLunsHaveDiscardAfterDeleteFunctionality() {
        storageDomain.setDiscardAfterDelete(true);
        assertTrue(discardHelper.isExistingDiscardAfterDeleteFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardSupport(1024L),
                createLunWithDiscardSupport(2048L)), storageDomain));
    }

    @Test
    public void testExistingDiscardAfterDeleteFunctionalityPreservedDiscardAfterDeleteFunctionalityBreaks() {
        storageDomain.setDiscardAfterDelete(true);
        assertFalse(discardHelper.isExistingDiscardAfterDeleteFunctionalityPreserved(Arrays.asList(
                createLunWithDiscardSupport(1024L),
                createLunWithDiscardSupport(0L)), storageDomain));
    }

    @Test
    public void testGetLunsThatBreakPassDiscardFunctionalityPassDiscardBreaks() {
        createVmDiskOnSd(false, false);
        createVmDiskOnSd(false, true); // This disk requires pass discard support.
        LUNs lunThatBreaksDiscardSupport = createLunWithDiscardSupport(0L);
        assertGetLunsThatBreakPassDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardSupport(1024L), lunThatBreaksDiscardSupport),
                Collections.singletonList(lunThatBreaksDiscardSupport));
    }

    @Test
    public void testGetLunsThatBreakPassDiscardFunctionalityDiscardFunctionalityDoesntBreak() {
        createVmDiskOnSd(false, false); // This disk does not require any discard functionality.
        createVmDiskOnSd(false, true); // This disk requires pass discard support.
        assertGetLunsThatBreakPassDiscardFunctionalityContainsExpectedLuns(
                Arrays.asList(createLunWithDiscardSupport(1024L),
                        createLunWithDiscardSupport(2048L)),
                Collections.emptyList());
    }

    @Test
    public void testGetLunsThatBreakDiscardAfterDeleteSupportStorageDomainDiscardAfterDeleteDisabled() {
        storageDomain.setDiscardAfterDelete(false);
        assertGetLunsThatBreakDiscardAfterDeleteSupportContainsExpectedLuns(
                Arrays.asList(
                        createLunWithDiscardSupport(1024L),
                        createLunWithDiscardSupport(0L)),
                Collections.emptyList());
    }

    @Test
    public void testGetLunsThatBreakDiscardAfterDeleteSupportDiscardAfterDeleteBreaks() {
        storageDomain.setDiscardAfterDelete(true);
        LUNs lunThatBreaksDiscardSupport = createLunWithDiscardSupport(0L);
                assertGetLunsThatBreakDiscardAfterDeleteSupportContainsExpectedLuns(
                Arrays.asList(
                        createLunWithDiscardSupport(1024L),
                        lunThatBreaksDiscardSupport),
                        Collections.singletonList(lunThatBreaksDiscardSupport));
    }

    @Test
    public void testGetLunsThatBreakDiscardAfterDeleteSupportDiscardAfterDeleteDoesntBreak() {
        storageDomain.setDiscardAfterDelete(true);
        assertGetLunsThatBreakDiscardAfterDeleteSupportContainsExpectedLuns(
                Arrays.asList(
                        createLunWithDiscardSupport(1024L),
                        createLunWithDiscardSupport(2048L)),
                Collections.emptyList());
    }

    private void createStorageDomain() {
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
    }

    private LUNs createLunWithDiscardSupport(Long discardMaxSize) {
        LUNs lun = new LUNs();
        lun.setDiscardMaxSize(discardMaxSize);
        return lun;
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
                discardHelper.getLunsThatBreakDiscardAfterDeleteSupport(luns, storageDomain);
        assertTrue(CollectionUtils.isEqualCollection(lunsThatBreakDiscardAfterDeleteSupport,
                expectedLunsThatBreakDiscardAfterDeleteSupport));
    }
}
