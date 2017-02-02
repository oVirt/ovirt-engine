package org.ovirt.engine.core.bll.memory.sdfilters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class StorageDomainSpaceRequirementsFilterTest {

    private StorageDomainSpaceRequirementsFilter filter;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    private StorageDomain storageDomain;
    private List<DiskImage> memoryDisks;

    @Before
    public void setUp() {
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        memoryDisks = new LinkedList<>();
        initFilter();
        initStorageDomainValidator();
    }

    @Test
    public void testStorageDomainForMemoryIsValid() {
        assertTrue(filter.test(storageDomain));
    }

    @Test
    public void testStorageDomainForMemoryIsNotValidWhenItHasLowSpace() {
        when(storageDomainValidator.isDomainWithinThresholds())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        assertFalse(filter.test(storageDomain));
    }

    @Test
    public void testStorageDomainForMemoryIsNotValidWhenItHasNoSpaceForClonedDisks() {
        when(storageDomainValidator.hasSpaceForClonedDisks(memoryDisks))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        assertFalse(filter.test(storageDomain));
    }

    private void initFilter() {
        filter = spy(new StorageDomainSpaceRequirementsFilter(mock(MemoryStorageHandler.class), memoryDisks));
        doReturn(storageDomainValidator).when(filter).getStorageDomainValidator(storageDomain);
    }

    private void initStorageDomainValidator() {
        when(storageDomainValidator.isDomainWithinThresholds()).thenReturn(ValidationResult.VALID);
        when(storageDomainValidator.hasSpaceForClonedDisks(memoryDisks))
                .thenReturn(ValidationResult.VALID);
    }
}
