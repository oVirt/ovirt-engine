package org.ovirt.engine.core.bll.memory.sdfilters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class StorageDomainSpaceRequirementsFilterTest extends StorageDomainFilterAbstractTest {

    @Spy
    private StorageDomainSpaceRequirementsFilter filter = new StorageDomainSpaceRequirementsFilter();

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        initFilter();
        initStorageDomainValidator();
    }

    @Test
    public void testStorageDomainForMemoryIsValid() {
        assertTrue(filter.getPredicate(memoryDisks).test(storageDomain));
    }

    @Test
    public void testStorageDomainForMemoryIsNotValidWhenItHasLowSpace() {
        when(storageDomainValidator.isDomainWithinThresholds())
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        assertFalse(filter.getPredicate(memoryDisks).test(storageDomain));
    }

    @Test
    public void testStorageDomainForMemoryIsNotValidWhenItHasNoSpaceForClonedDisks() {
        when(storageDomainValidator.hasSpaceForClonedDisks(memoryDisks))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        assertFalse(filter.getPredicate(memoryDisks).test(storageDomain));
    }

    private void initFilter() {
        doNothing().when(filter).updateDisksStorage(any(StorageDomain.class), anyListOf(DiskImage.class));
        doReturn(storageDomainValidator).when(filter).getStorageDomainValidator(storageDomain);
    }

    private void initStorageDomainValidator() {
        when(storageDomainValidator.isDomainWithinThresholds()).thenReturn(ValidationResult.VALID);
        when(storageDomainValidator.hasSpaceForClonedDisks(memoryDisks))
                .thenReturn(ValidationResult.VALID);
    }
}
