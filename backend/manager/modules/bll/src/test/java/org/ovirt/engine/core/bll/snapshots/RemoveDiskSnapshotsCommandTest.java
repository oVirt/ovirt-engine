package org.ovirt.engine.core.bll.snapshots;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskSnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

/** A test case for the {@link RemoveDiskSnapshotsCommand} class. */
public class RemoveDiskSnapshotsCommandTest extends BaseCommandTest {

    private RemoveDiskSnapshotsCommand<RemoveDiskSnapshotsParameters> cmd;

    @Mock
    private VmTemplateDao vmTemplateDao;

    @Mock
    private StorageDomainDao sdDao;

    @Mock
    private DiskImageDao diskImageDao;

    @Mock
    private DiskDao diskDao;

    @Mock
    private SnapshotDao snapshotDao;

    @Mock
    private StoragePoolDao spDao;

    @Mock
    private VmDao vmDao;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private SnapshotsValidator snapshotsValidator;

    @Mock
    private StorageDomainValidator storageDomainValidator;

    private VmValidator vmValidator;

    private DiskImagesValidator diskImagesValidator;

    private DiskSnapshotsValidator diskSnapshotsValidator;

    private static final Guid STORAGE_DOMAIN_ID = Guid.newGuid();
    private static final Guid STORAGE_POOLD_ID = Guid.newGuid();
    private static final Guid IMAGE_ID_1 = Guid.newGuid();
    private static final Guid IMAGE_ID_2 = Guid.newGuid();
    private static final Guid IMAGE_ID_3 = Guid.newGuid();

    @Before
    public void setUp() {
        RemoveDiskSnapshotsParameters params = new RemoveDiskSnapshotsParameters(
                new ArrayList<>(Arrays.asList(IMAGE_ID_1, IMAGE_ID_2)));
        Guid vmGuid = Guid.newGuid();
        params.setContainerId(vmGuid);

        cmd = spy(new RemoveDiskSnapshotsCommand<RemoveDiskSnapshotsParameters>(params, null) {
            protected List<DiskImage> getImages() {
                return mockImages();
            }

            protected List<DiskImage> getAllImagesForDisk() {
                return mockAllImages();
            }
        });

        doReturn(snapshotDao).when(cmd).getSnapshotDao();
        doReturn(spDao).when(cmd).getStoragePoolDao();
        doReturn(vmTemplateDao).when(cmd).getVmTemplateDao();
        doReturn(diskImageDao).when(cmd).getDiskImageDao();
        doReturn(diskDao).when(cmd).getDiskDao();
        doReturn(sdDao).when(cmd).getStorageDomainDao();
        doReturn(vmDeviceDao).when(cmd).getVmDeviceDao();
        doReturn(snapshotsValidator).when(cmd).getSnapshotsValidator();
        doReturn(storageDomainValidator).when(cmd).getStorageDomainValidator();
        doReturn(STORAGE_POOLD_ID).when(cmd).getStoragePoolId();
        doReturn(mockImages()).when(cmd).getImages();

        mockVm();

        vmValidator = spy(new VmValidator(cmd.getVm()));
        doReturn(vmValidator).when(cmd).createVmValidator(any(VM.class));

        diskImagesValidator = spy(new DiskImagesValidator(mockImages()));
        doReturn(diskImagesValidator).when(cmd).createDiskImageValidator(any(List.class));
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesNotExist();
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesSnapshotsNotAttachedToOtherVms(false);

        diskSnapshotsValidator = spy(new DiskSnapshotsValidator(mockImages()));
        doReturn(diskSnapshotsValidator).when(cmd).createDiskSnapshotsValidator(any(List.class));
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
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).vmNotInPreview(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotsValidator).snapshotExists(any(Guid.class), any(Guid.class));
        doReturn(ValidationResult.VALID).when(storageDomainValidator).isDomainExistAndActive();
        doReturn(ValidationResult.VALID).when(storageDomainValidator).hasSpaceForClonedDisks(any(Collection.class));
        doReturn(true).when(cmd).validateAllDiskImages();
        doReturn(sp).when(spDao).get(STORAGE_POOLD_ID);
    }

    private List<DiskImage> mockImages() {
        DiskImage image1 = new DiskImage();
        image1.setImageId(IMAGE_ID_1);
        image1.setStorageIds(new ArrayList<>(Arrays.asList(STORAGE_DOMAIN_ID)));

        DiskImage image2 = new DiskImage();
        image2.setImageId(IMAGE_ID_2);
        image2.setStorageIds(new ArrayList<>(Arrays.asList(STORAGE_DOMAIN_ID)));

        return new ArrayList<>(Arrays.asList(image1, image2));
    }

    private List<DiskImage> mockAllImages() {
        List<DiskImage> images = mockImages();

        DiskImage image3 = new DiskImage();
        image3.setImageId(IMAGE_ID_3);
        image3.setStorageIds(new ArrayList<>(Arrays.asList(STORAGE_DOMAIN_ID)));
        images.add(image3);
        return images;
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
