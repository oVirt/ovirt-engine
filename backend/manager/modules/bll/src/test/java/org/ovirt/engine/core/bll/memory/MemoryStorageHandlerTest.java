package org.ovirt.engine.core.bll.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.memory.sdfilters.StorageDomainFilter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.Predicate;

@RunWith(MockitoJUnitRunner.class)
public class MemoryStorageHandlerTest {

    private StorageDomain validStorageDomain;
    private StorageDomain invalidStorageDomain1;
    private StorageDomain invalidStorageDomain2;
    private StorageDomain invalidStorageDomain3;
    private List<DiskImage> disksList;

    @Spy
    private MemoryStorageHandler memoryStorageHandler = MemoryStorageHandler.getInstance();

    @Before
    public void setUp() {
        disksList = new LinkedList<>();
        initStorageDomains();
        initFilters();
    }

    @Test
    public void filterAllDomainsExceptForTheFirstOne() {
        filterAllStorageDomainsExceptOne(Arrays.asList(
                validStorageDomain, invalidStorageDomain1, invalidStorageDomain2), validStorageDomain);
    }

    @Test
    public void filterAllDomainsExceptForTheSecondOne() {
        filterAllStorageDomainsExceptOne(Arrays.asList(
                invalidStorageDomain1, validStorageDomain, invalidStorageDomain2), validStorageDomain);
    }

    @Test
    public void filterAllDomainsExceptForTheThirdOne() {
        filterAllStorageDomainsExceptOne(Arrays.asList(
                invalidStorageDomain1, invalidStorageDomain2, validStorageDomain), validStorageDomain);
    }

    @Test
    public void filterAllDomains() {
        List<StorageDomain> filteredStorageDomains = memoryStorageHandler.filterStorageDomains(
                Arrays.asList(invalidStorageDomain1, invalidStorageDomain2, invalidStorageDomain3), disksList);
        assertTrue(filteredStorageDomains.isEmpty());
    }

    @Test
    public void testFindStorageDomainForMemoryWithSingleDomain() {
        verifyDomainForMemory(Collections.singletonList(validStorageDomain));
    }

    @Test
    public void testFindStorageDomainForMemoryWithEmptyDomainsList() {
        verifyNoDomainForMemory(Collections.<StorageDomain>emptyList());
    }

    @Test
    public void testFindStorageDomainForMemoryWithSingleInvalidDomain() {
        verifyNoDomainForMemory(Collections.singletonList(invalidStorageDomain1));
    }

    private void initStorageDomains() {
        validStorageDomain = initStorageDomain();
        invalidStorageDomain1 = initStorageDomain();
        invalidStorageDomain2 = initStorageDomain();
        invalidStorageDomain3 = initStorageDomain();
    }

    private void initFilters() {
        List<StorageDomainRejectingFilter> storageDomainFilters = Arrays.asList(
                new StorageDomainRejectingFilter(invalidStorageDomain1),
                new StorageDomainRejectingFilter(invalidStorageDomain2),
                new StorageDomainRejectingFilter(invalidStorageDomain3));
        doReturn(storageDomainFilters).when(memoryStorageHandler).getStorageDomainFilters();
    }

    private StorageDomain initStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        return storageDomain;
    }

    private void filterAllStorageDomainsExceptOne(List<StorageDomain> storageDomains,
            StorageDomain expectedStorageDomain) {
        List<StorageDomain> filteredStorageDomains = new ArrayList<>(
                memoryStorageHandler.filterStorageDomains(storageDomains, disksList));
        assertEquals(filteredStorageDomains, Arrays.asList(expectedStorageDomain));
    }

    private void verifyDomainForMemory(List<StorageDomain> storageDomains) {
        StorageDomain storageDomain = memoryStorageHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertEquals(storageDomain, validStorageDomain);
    }

    private void verifyNoDomainForMemory(List<StorageDomain> storageDomains) {
        StorageDomain storageDomain = memoryStorageHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertNull(storageDomain);
    }

    private static class StorageDomainRejectingFilter extends StorageDomainFilter {

        private final StorageDomain sdToReject;

        private StorageDomainRejectingFilter(StorageDomain storageDomainToReject) {
            this.sdToReject = storageDomainToReject;
        }

        @Override
        protected Predicate<StorageDomain> getPredicate(List<DiskImage> disksList) {
            return new Predicate<StorageDomain>() {
                @Override
                public boolean eval(StorageDomain storageDomain) {
                    return !sdToReject.equals(storageDomain);
                }
            };
        }
    }
}
