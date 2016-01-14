package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class AddVmFromTemplateCommandTest extends AddVmCommandTest {

    /**
     * The command under test.
     */
    protected AddVmFromTemplateCommand<AddVmParameters> command;

    @Override
    @Test
    public void validateSpaceAndThreshold() {
        mockOsRepository();
        initCommand();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        mockGetAllSnapshots();
        assertTrue(command.validateSpaceRequirements());
        verify(storageDomainValidator, times(TOTAL_NUM_DOMAINS)).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Override
    @Test
    public void validateSpaceNotEnough() throws Exception {
        mockOsRepository();
        initCommand();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        mockGetAllSnapshots();
        assertFalse(command.validateSpaceRequirements());
        //The following is mocked to fail, should happen only once.
        verify(storageDomainValidator).hasSpaceForClonedDisks(anyList());
        verify(storageDomainValidator, never()).hasSpaceForNewDisks(anyList());
    }

    @Override
    @Test
    public void validateSpaceNotWithinThreshold() throws Exception {
        mockOsRepository();
        initCommand();
        doReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(storageDomainValidator).isDomainWithinThresholds();
        doReturn(storageDomainValidator).when(command).createStorageDomainValidator(any(StorageDomain.class));
        assertFalse(command.validateSpaceRequirements());
    }

    @Override
    protected List<DiskImage> createDiskSnapshot(Guid diskId, int numOfImages) {
        List<DiskImage> disksList = new ArrayList<>();
        for (int i = 0; i < numOfImages; ++i) {
            DiskImage diskImage = new DiskImage();
            diskImage.setActive(false);
            diskImage.setId(diskId);
            diskImage.setImageId(Guid.newGuid());
            diskImage.setParentId(Guid.newGuid());
            diskImage.setImageStatus(ImageStatus.OK);
            disksList.add(diskImage);
        }
        return disksList;
    }

    private void mockGetAllSnapshots() {
        doAnswer(new Answer<List<DiskImage>>() {
            @Override
            public List<DiskImage> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                DiskImage arg = (DiskImage) args[0];
                List<DiskImage> list  = createDiskSnapshot(arg.getId(), 3);
                return list;
            }
        }).when(command).getAllImageSnapshots(any(DiskImage.class));
    }

    private void initCommand() {
        VM vm = createVm();
        command = createVmFromTemplateCommand(vm);
        generateStorageToDisksMap(command);
        initDestSDs(command);
        storageDomainValidator = mock(StorageDomainValidator.class);
    }
}
