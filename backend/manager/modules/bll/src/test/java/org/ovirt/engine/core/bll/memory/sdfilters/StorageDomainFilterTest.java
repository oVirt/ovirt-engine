package org.ovirt.engine.core.bll.memory.sdfilters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.utils.linq.Predicate;

@RunWith(MockitoJUnitRunner.class)
public class StorageDomainFilterTest extends StorageDomainFilterAbstractTest {

    private StorageDomainFilter filter;

    @Mock
    private Predicate<StorageDomain> predicate;

    @Test
    public void testThatFilterRemovesStorageDomainFromList() {
        List<StorageDomain> filteredDomains = filterStorageDomain(true);
        assertTrue(filteredDomains.isEmpty());
    }

    @Test
    public void testThatFilterDoesntRemoveStorageDomainFromList() {
        List<StorageDomain> filteredDomains = new ArrayList<>(filterStorageDomain(false));
        assertEquals(filteredDomains, Arrays.asList(storageDomain));
    }

    private List<StorageDomain> filterStorageDomain(boolean removeStorageDomainFromList) {
        filter = new StorageDomainFilter() {
            @Override
            protected Predicate<StorageDomain> getPredicate(List<DiskImage> disksList) {
                return predicate;
            }
        };

        when(predicate.eval(storageDomain)).thenReturn(!removeStorageDomainFromList);

        List<StorageDomain> storageDomains = Arrays.asList(storageDomain);
        return filter.filterStorageDomains(storageDomains, disksList);
    }
}
