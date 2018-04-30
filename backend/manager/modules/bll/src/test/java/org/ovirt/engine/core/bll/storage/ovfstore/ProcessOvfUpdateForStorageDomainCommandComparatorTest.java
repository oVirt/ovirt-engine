package org.ovirt.engine.core.bll.storage.ovfstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.compat.Guid;

/**
 * A test case for {@link ProcessOvfUpdateForStorageDomainCommand#OVF_INFO_COMPARATOR}
 */
public class ProcessOvfUpdateForStorageDomainCommandComparatorTest {

    @Test
    public void sorting() {
        Guid sdId = Guid.newGuid();
        Guid diskId1 = Guid.createGuidFromString("00000000-0000-0000-0000-000000000001");
        Guid diskId2 = Guid.createGuidFromString("00000000-0000-0000-0000-000000000002");
        List<StorageDomainOvfInfo> expected = new LinkedList<>(Arrays.asList(
                new StorageDomainOvfInfo(sdId, null, diskId1, StorageDomainOvfInfoStatus.UPDATED, null),
                new StorageDomainOvfInfo(sdId, null, diskId2, StorageDomainOvfInfoStatus.UPDATED, null),
                new StorageDomainOvfInfo(sdId, null, diskId1, StorageDomainOvfInfoStatus.UPDATED, new Date(0L)),
                new StorageDomainOvfInfo(sdId, null, diskId2, StorageDomainOvfInfoStatus.UPDATED, new Date(0L)),
                new StorageDomainOvfInfo(sdId, null, diskId1, StorageDomainOvfInfoStatus.UPDATED, new Date(1L)),
                new StorageDomainOvfInfo(sdId, null, diskId2, StorageDomainOvfInfoStatus.UPDATED, new Date(1L)))
        );
        List<StorageDomainOvfInfo> actual = new LinkedList<>(expected);
        Collections.shuffle(actual);
        actual.sort(ProcessOvfUpdateForStorageDomainCommand.OVF_INFO_COMPARATOR);

        assertEquals(expected, actual);
    }
}
