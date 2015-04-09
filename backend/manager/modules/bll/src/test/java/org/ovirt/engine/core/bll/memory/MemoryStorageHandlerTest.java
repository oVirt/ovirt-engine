package org.ovirt.engine.core.bll.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class MemoryStorageHandlerTest {

    private StorageDomain validStorageDomain;
    private StorageDomain invalidStorageDomain1;
    private StorageDomain invalidStorageDomain2;
    private List<DiskImage> disksList;

    @Mock
    private StorageDomainValidator validStorageDomainValidator;

    @Mock
    private StorageDomainValidator invalidStorageDomainValidator1;

    @Mock
    private StorageDomainValidator invalidStorageDomainValidator2;

    @Spy
    private MemoryStorageHandler memoryStorageHandler = MemoryStorageHandler.getInstance();

    @Before
    public void setUp() {
        disksList = new LinkedList<>();
        doNothing().when(memoryStorageHandler).updateDisksStorage(any(StorageDomain.class), anyListOf(DiskImage.class));

        validStorageDomain = initStorageDomain();
        initStorageDomainValidator(validStorageDomainValidator, ValidationResult.VALID);
        doReturn(validStorageDomainValidator)
                .when(memoryStorageHandler).getStorageDomainValidator(validStorageDomain);

        invalidStorageDomain1 = initStorageDomain();
        initInvalidValidator(invalidStorageDomainValidator1);
        doReturn(invalidStorageDomainValidator1)
                .when(memoryStorageHandler).getStorageDomainValidator(invalidStorageDomain1);

        invalidStorageDomain2 = initStorageDomain();
        initInvalidValidator(invalidStorageDomainValidator2);
        doReturn(invalidStorageDomainValidator2)
                .when(memoryStorageHandler).getStorageDomainValidator(invalidStorageDomain2);
    }

    @Test
    public void verifyLastDomainForMemory() {
        verifyDomainForMemory(Arrays.asList(invalidStorageDomain1, invalidStorageDomain2, validStorageDomain));
    }

    @Test
    public void verifyNoDomainForMemoryWhenDomainHasLowSpace() {
        when(validStorageDomainValidator.isDomainWithinThresholds())
                .thenReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        verifyNoDomainForMemory(Collections.singletonList(validStorageDomain));
    }

    private StorageDomain initStorageDomain() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(Guid.newGuid());
        storageDomain.setStorageDomainType(StorageDomainType.Data);
        storageDomain.setStatus(StorageDomainStatus.Active);
        return storageDomain;
    }

    private void initInvalidValidator(StorageDomainValidator invalidStorageDomainValidator) {
        initStorageDomainValidator(invalidStorageDomainValidator,
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    private void initStorageDomainValidator(StorageDomainValidator storageDomainValidator,
            ValidationResult validationResult) {
        when(storageDomainValidator.isDomainWithinThresholds()).thenReturn(validationResult);
        when(storageDomainValidator.hasSpaceForClonedDisks(disksList))
                .thenReturn(validationResult);
    }

    private void verifyDomainForMemory(List<StorageDomain> storageDomains) {
        StorageDomain storageDomain = memoryStorageHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertEquals(storageDomain, validStorageDomain);
    }

    private void verifyNoDomainForMemory(List<StorageDomain> storageDomains) {
        StorageDomain storageDomain = memoryStorageHandler.findStorageDomainForMemory(storageDomains, disksList);
        assertNull(storageDomain);
    }
}
