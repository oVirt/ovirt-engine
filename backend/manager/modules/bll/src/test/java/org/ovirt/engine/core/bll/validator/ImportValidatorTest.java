package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@ExtendWith(MockitoExtension.class)
public class ImportValidatorTest {
    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;

    @Test
    public void sufficientDiskSpace() {
        ImportValidator validator = setupDiskSpaceTest(createParameters());
        assertTrue(validator.validateSpaceRequirements(mockCreateDiskDummiesForSpaceValidations()).isValid());
        verify(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(any());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForDisksWithSnapshots(any());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(any());
    }

    @Test
    public void insufficientDiskSpaceWithCollapse() {
        ImportValidator validator = setupDiskSpaceTest(createParameters());
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN))
            .when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(any());
        assertThat(validator.validateSpaceRequirements(Collections.emptyList()),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        verify(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(any());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForDisksWithSnapshots(any());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(any());
    }

    @Test
    public void insufficientDiskSpaceWithSnapshots() {
        ImportVmParameters parameters = createParameters();
        ImportValidator validator = setupDiskSpaceTest(parameters);
        parameters.setCopyCollapse(false);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForDisksWithSnapshots(any());
        assertThat(validator.validateSpaceRequirements(Collections.emptyList()),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForClonedDisks(any());
        verify(multipleSdValidator).allDomainsHaveSpaceForDisksWithSnapshots(any());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(any());
    }

    @Test
    public void lowThresholdStorageSpace() {
        ImportVmParameters parameters = createParameters();
        ImportValidator validator = setupDiskSpaceTest(parameters);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertThat(validator.validateSpaceRequirements(Collections.emptyList()),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN));
    }

    protected ImportVmParameters createParameters() {
        final VM v = createVM();
        v.setName("testVm");
        return new ImportVmParameters(v, Guid.newGuid(), Guid.newGuid(), Guid.newGuid(), Guid.newGuid());
    }

    protected VM createVM() {
        final VM v = new VM();
        v.setId(Guid.newGuid());

        Guid imageGroupId = Guid.newGuid();
        DiskImage baseImage = new DiskImage();
        baseImage.setId(imageGroupId);
        baseImage.setImageId(Guid.newGuid());
        baseImage.setSizeInGigabytes(1);
        baseImage.setVmSnapshotId(Guid.newGuid());
        baseImage.setActive(false);

        DiskImage activeImage = new DiskImage();
        activeImage.setId(imageGroupId);
        activeImage.setImageId(Guid.newGuid());
        activeImage.setSizeInGigabytes(1);
        activeImage.setVmSnapshotId(Guid.newGuid());
        activeImage.setActive(true);
        activeImage.setParentId(baseImage.getImageId());

        v.setDiskMap(Collections.singletonMap(activeImage.getId(), activeImage));
        v.setImages(new ArrayList<>(Arrays.asList(baseImage, activeImage)));
        v.setClusterId(Guid.Empty);

        return v;
    }

    private ImportValidator setupDiskSpaceTest(ImportVmParameters parameters) {
        ImportValidator validator = spy(new ImportValidator(parameters));
        parameters.setCopyCollapse(true);

        ArrayList<Guid> sdIds = new ArrayList<>(Collections.singletonList(Guid.newGuid()));
        for (DiskImage image : parameters.getVm().getImages()) {
            image.setStorageIds(sdIds);
        }

        doReturn(multipleSdValidator).when(validator).createMultipleStorageDomainsValidator(any());
        return validator;
    }

    protected List<DiskImage> mockCreateDiskDummiesForSpaceValidations() {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            DiskImage diskImage = new DiskImage();
            diskImage.setActive(false);
            diskImage.setId(Guid.newGuid());
            diskImage.setImageId(Guid.newGuid());
            diskImage.setParentId(Guid.newGuid());
            diskImage.setImageStatus(ImageStatus.OK);
            disksList.add(diskImage);
        }
        return disksList;
    }
}
