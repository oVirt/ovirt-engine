package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;


@RunWith(MockitoJUnitRunner.class)
public class ImportValidatorTest {
    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;

    @Test
    public void sufficientDiskSpace() {
        ImportValidator validator = setupDiskSpaceTest(createParameters());
        assertTrue(validator.validateSpaceRequirements(mockCreateDiskDummiesForSpaceValidations()).isValid());
        verify(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForDisksWithSnapshots(anyList());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void insufficientDiskSpaceWithCollapse() {
        ImportValidator validator = setupDiskSpaceTest(createParameters());
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN))
            .when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                validator.validateSpaceRequirements(Collections.<DiskImage>emptyList()).getMessage());
        verify(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForDisksWithSnapshots(anyList());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void insufficientDiskSpaceWithSnapshots() {
        ImportVmParameters parameters = createParameters();
        ImportValidator validator = setupDiskSpaceTest(parameters);
        parameters.setCopyCollapse(false);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForDisksWithSnapshots(anyList());
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                validator.validateSpaceRequirements(Collections.<DiskImage>emptyList()).getMessage());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForClonedDisks(anyList());
        verify(multipleSdValidator).allDomainsHaveSpaceForDisksWithSnapshots(anyList());
        verify(multipleSdValidator, never()).allDomainsHaveSpaceForNewDisks(anyList());
    }

    @Test
    public void lowThresholdStorageSpace() {
        ImportVmParameters parameters = createParameters();
        ImportValidator validator = setupDiskSpaceTest(parameters);
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertEquals(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                validator.validateSpaceRequirements(Collections.<DiskImage>emptyList()).getMessage());
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

        v.setDiskMap(Collections.<Guid, Disk> singletonMap(activeImage.getId(), activeImage));
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

        doReturn(multipleSdValidator).when(validator).createMultipleStorageDomainsValidator(anyList());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsHaveSpaceForDisksWithSnapshots(anyList());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsExistAndActive();
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
