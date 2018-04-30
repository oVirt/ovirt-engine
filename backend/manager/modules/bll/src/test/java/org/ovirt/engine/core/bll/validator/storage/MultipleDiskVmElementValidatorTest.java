package org.ovirt.engine.core.bll.validator.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class MultipleDiskVmElementValidatorTest {

    private DiskImage diskWithOneVmElement;
    private DiskImage diskWithTwoVmElements;
    private DiskVmElement diskVmElement1;
    private DiskVmElement diskVmElement2;
    private DiskVmElement diskVmElement3;
    private Map<Disk, Collection<DiskVmElement>> diskToDiskVmElements;
    private MultipleDiskVmElementValidator multipleDiskVmElementValidator;

    @BeforeEach
    public void setUp() {
        diskWithOneVmElement = createDiskImage();
        diskWithOneVmElement.setDiskAlias("diskWithOneVmElement");
        diskVmElement1 = new DiskVmElement(diskWithOneVmElement.getId(), Guid.newGuid());

        diskWithTwoVmElements = createDiskImage();
        diskWithOneVmElement.setDiskAlias("diskWithTwoVmElements");
        diskVmElement2 = new DiskVmElement(diskWithOneVmElement.getId(), Guid.newGuid());
        diskVmElement3 = new DiskVmElement(diskWithTwoVmElements.getId(), Guid.newGuid());

        diskToDiskVmElements = new HashMap<>();
    }

    @Test
    public void testCtorForSingleDiskWithOneOrMoreDiskVmElements() {
        Collection<DiskVmElement> diskVmElements = Arrays.asList(diskVmElement2, diskVmElement3);
        multipleDiskVmElementValidator = spy(new MultipleDiskVmElementValidator(diskWithTwoVmElements, diskVmElements));

        diskToDiskVmElements.put(diskWithTwoVmElements, diskVmElements);

        assertDiskToDiskVmElementsEquality();
    }

    @Test
    public void testCtorForAFewDisksWithTheirDiskVmElements() {
        Map<Disk, DiskVmElement> diskToDiskVmElement = new HashMap<>();
        diskToDiskVmElement.put(diskWithOneVmElement, diskVmElement1);
        diskToDiskVmElement.put(diskWithTwoVmElements, diskVmElement2);
        multipleDiskVmElementValidator = spy(new MultipleDiskVmElementValidator(diskToDiskVmElement));

        initMultipleDiskVmElementValidator();

        assertDiskToDiskVmElementsEquality();
    }

    @Test
    public void passDiscardSupportedForDestSd() {
        diskToDiskVmElements.put(diskWithTwoVmElements, Arrays.asList(diskVmElement2, diskVmElement3));
        initMultipleDiskVmElementValidator();
        mockDiskVmElementValidatorPassDiscardValidation(diskWithTwoVmElements, diskVmElement2, ValidationResult.VALID);
        mockDiskVmElementValidatorPassDiscardValidation(diskWithTwoVmElements, diskVmElement3, ValidationResult.VALID);

        assertThat(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSd(Guid.newGuid()), isValid());
    }

    @Test
    public void passDiscardNotSupportedForDestSd() {
        diskToDiskVmElements.put(diskWithTwoVmElements, Arrays.asList(diskVmElement2, diskVmElement3));
        initMultipleDiskVmElementValidator();
        mockDiskVmElementValidatorPassDiscardValidation(diskWithTwoVmElements, diskVmElement2, ValidationResult.VALID);
        mockDiskVmElementValidatorPassDiscardValidation(diskWithTwoVmElements, diskVmElement3,
                getPassDiscardNotSupportedByDiskInterfaceValResult(diskWithTwoVmElements));

        assertThat(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSd(Guid.newGuid()),
                failsWith(getPassDiscardNotSupportedByDiskInterfaceValResult(diskWithTwoVmElements)));
    }

    @Test
    public void passDiscardSupportedForDestSds() {
        diskToDiskVmElements.put(diskWithOneVmElement, Collections.singleton(diskVmElement1));
        diskToDiskVmElements.put(diskWithTwoVmElements, Collections.singleton(diskVmElement2));
        initMultipleDiskVmElementValidator();
        mockDiskVmElementValidatorPassDiscardValidation(diskWithOneVmElement, diskVmElement1, ValidationResult.VALID);
        mockDiskVmElementValidatorPassDiscardValidation(diskWithTwoVmElements, diskVmElement2, ValidationResult.VALID);

        Map<Guid, Guid> diskIdToDestSdId = new HashMap<>();
        diskIdToDestSdId.put(diskWithOneVmElement.getId(), Guid.newGuid());
        diskIdToDestSdId.put(diskWithTwoVmElements.getId(), Guid.newGuid());

        assertThat(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSds(diskIdToDestSdId), isValid());
    }

    @Test
    public void passDiscardNotSupportedForDestSds() {
        diskToDiskVmElements.put(diskWithOneVmElement, Collections.singleton(diskVmElement1));
        diskToDiskVmElements.put(diskWithTwoVmElements, Collections.singleton(diskVmElement2));
        initMultipleDiskVmElementValidator();
        mockDiskVmElementValidatorPassDiscardValidation(diskWithOneVmElement, diskVmElement1, ValidationResult.VALID);
        mockDiskVmElementValidatorPassDiscardValidation(diskWithTwoVmElements, diskVmElement2,
                getPassDiscardNotSupportedByDiskInterfaceValResult(diskWithTwoVmElements));

        Map<Guid, Guid> diskIdToDestSdId = new HashMap<>();
        diskIdToDestSdId.put(diskWithOneVmElement.getId(), Guid.newGuid());
        diskIdToDestSdId.put(diskWithTwoVmElements.getId(), Guid.newGuid());

        assertThat(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSds(diskIdToDestSdId),
                failsWith(getPassDiscardNotSupportedByDiskInterfaceValResult(diskWithTwoVmElements)));
    }

    private DiskImage createDiskImage() {
        DiskImage diskImage = new DiskImage();
        diskImage.setId(Guid.newGuid());
        return diskImage;
    }

    private void assertDiskToDiskVmElementsEquality() {
        assertTrue(CollectionUtils.isEqualCollection(multipleDiskVmElementValidator.diskToDiskVmElements.entrySet(),
                diskToDiskVmElements.entrySet()));
    }

    private void initMultipleDiskVmElementValidator() {
        multipleDiskVmElementValidator = spy(new MultipleDiskVmElementValidator());
        multipleDiskVmElementValidator.diskToDiskVmElements = diskToDiskVmElements;
    }

    private void mockDiskVmElementValidatorPassDiscardValidation(Disk disk, DiskVmElement diskVmElement,
            ValidationResult validationResult) {
        DiskVmElementValidator diskVmElementValidator = spy(new DiskVmElementValidator(disk, diskVmElement));
        doReturn(diskVmElementValidator).when(multipleDiskVmElementValidator)
                .createDiskVmElementValidator(disk, diskVmElement);
        doReturn(validationResult).when(diskVmElementValidator).isPassDiscardSupported(any());
    }

    private ValidationResult getPassDiscardNotSupportedByDiskInterfaceValResult(Disk disk) {
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE,
                String.format("$diskAlias %s", disk.getDiskAlias()));
    }
}
