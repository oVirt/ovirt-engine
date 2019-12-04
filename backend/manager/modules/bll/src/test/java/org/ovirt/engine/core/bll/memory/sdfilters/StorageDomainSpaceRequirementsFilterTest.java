package org.ovirt.engine.core.bll.memory.sdfilters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.memory.MemoryDisks;
import org.ovirt.engine.core.bll.memory.MemoryStorageHandler;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class StorageDomainSpaceRequirementsFilterTest {

    private StorageDomainSpaceRequirementsFilter filter;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    private StorageDomain storageDomain;
    private MemoryDisks memoryDisks;

    @BeforeEach
    public void setUp() {
        storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        memoryDisks = new MemoryDisks(null, null);
        initFilter();
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
        when(storageDomainValidator.hasSpaceForClonedDisks(memoryDisks.asList()))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        assertFalse(filter.test(storageDomain));
    }

    private void initFilter() {
        filter = spy(new StorageDomainSpaceRequirementsFilter(mock(MemoryStorageHandler.class), memoryDisks));
        doReturn(storageDomainValidator).when(filter).getStorageDomainValidator(storageDomain);
    }
}
