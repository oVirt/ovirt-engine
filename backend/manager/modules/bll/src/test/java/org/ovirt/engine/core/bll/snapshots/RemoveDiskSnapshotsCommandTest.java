package org.ovirt.engine.core.bll.snapshots;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;

/** A test case for the {@link RemoveDiskSnapshotsCommand} class. */
public class RemoveDiskSnapshotsCommandTest extends BaseCommandTest {
    @Spy
    @InjectMocks
    private RemoveDiskSnapshotsCommand<RemoveDiskSnapshotsParameters> cmd = createCommand();

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private StoragePoolDao spDao;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    @Mock
    private SnapshotDao snapshotDao;

    private VmValidator vmValidator;

    private static final Guid STORAGE_DOMAIN_ID = Guid.newGuid();
    private static final Guid STORAGE_POOLD_ID = Guid.newGuid();
    private static final Guid IMAGE_ID_1 = Guid.newGuid();
    private static final Guid IMAGE_ID_2 = Guid.newGuid();

    private static RemoveDiskSnapshotsCommand<RemoveDiskSnapshotsParameters> createCommand() {
        RemoveDiskSnapshotsParameters params = new RemoveDiskSnapshotsParameters(
                new ArrayList<>(Arrays.asList(IMAGE_ID_1, IMAGE_ID_2)));
        Guid vmGuid = Guid.newGuid();
        params.setContainerId(vmGuid);

        return new RemoveDiskSnapshotsCommand<>(params, null);
    }

    @BeforeEach
    public void setUp() {
        mockStorageDomain();

        doReturn(storageDomainValidator).when(cmd).getStorageDomainValidator();
        doReturn(STORAGE_POOLD_ID).when(cmd).getStoragePoolId();
        doReturn(mockImages()).when(cmd).getImages();

        mockVm();

        vmValidator = spy(new VmValidator(cmd.getVm()));
        doReturn(vmValidator).when(cmd).createVmValidator(any());

        DiskImagesValidator diskImagesValidator = spy(new DiskImagesValidator(mockImages()));
        doReturn(diskImagesValidator).when(cmd).createDiskImageValidator(any());
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesSnapshotsNotAttachedToOtherVms(false);
    }

    private void mockStorageDomain(){
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setStorageType(StorageType.NFS);
        doReturn(storageDomain).when(cmd).getStorageDomain();
    }

    private void mockVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setStatus(VMStatus.Down);
        vm.setStoragePoolId(STORAGE_POOLD_ID);
        doReturn(vm).when(cmd).getVm();
    }

    private void prepareForVmValidatorTests() {
        StoragePool sp = new StoragePool();
        sp.setId(STORAGE_POOLD_ID);
        sp.setStatus(StoragePoolStatus.Up);

        cmd.setSnapshotName("someSnapshot");
        doReturn(true).when(cmd).validateAllDiskImages();
        doReturn(sp).when(spDao).get(STORAGE_POOLD_ID);
    }

    private List<DiskImage> mockImages() {
        DiskImage image1 = new DiskImage();
        image1.setImageId(IMAGE_ID_1);
        image1.setStorageIds(new ArrayList<>(Collections.singletonList(STORAGE_DOMAIN_ID)));

        DiskImage image2 = new DiskImage();
        image2.setImageId(IMAGE_ID_2);
        image2.setStorageIds(new ArrayList<>(Collections.singletonList(STORAGE_DOMAIN_ID)));

        return new ArrayList<>(Arrays.asList(image1, image2));
    }

    @Test
    public void testValidateVmUpLiveMergeSupported() {
        prepareForVmValidatorTests();

        cmd.getVm().setStatus(VMStatus.Up);
        doReturn(true).when(cmd).isDiskPlugged();
        doReturn(ValidationResult.VALID).when(vmValidator).vmQualifiedForSnapshotMerge();
        doReturn(true).when(cmd).validateStorageDomainAvailableSpace();
        ValidateTestUtils.runAndAssertValidateSuccess(cmd);
    }
}
