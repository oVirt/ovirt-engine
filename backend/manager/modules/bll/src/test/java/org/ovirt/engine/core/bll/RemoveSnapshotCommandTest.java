package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

/** A test case for the {@link RemoveSnapshotCommand} class. */
@RunWith(MockitoJUnitRunner.class)
public class RemoveSnapshotCommandTest {

    /** The command to test */
    private RemoveSnapshotCommand<RemoveSnapshotParameters> cmd;

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    @Mock
    private VmTemplateDAO vmTemplateDAO;

    @Mock
    StorageDomainDAO sdDAO;

    @Mock
    private DiskImageDAO diskImageDAO;

    @Mock
    private StoragePoolDAO spDao;

    @Mock
    private SnapshotsValidator snapshotValidator;

    private static final Guid STORAGE_DOMAIN_ID = Guid.NewGuid();
    private static final Guid STORAGE_DOMAIN_ID2 = Guid.NewGuid();
    private static final Guid STORAGE_POOLD_ID = Guid.NewGuid();

    private static final int USED_SPACE_GB = 4;
    private static final int IMAGE_ACTUAL_SIZE_GB = 4;

    @Before
    public void setUp() {
        Guid vmGuid = Guid.NewGuid();
        Guid snapGuid = Guid.NewGuid();

        RemoveSnapshotParameters params = new RemoveSnapshotParameters(snapGuid, vmGuid);
        cmd = spy(new RemoveSnapshotCommand<RemoveSnapshotParameters>(params));
        doReturn(spDao).when(cmd).getStoragePoolDAO();
        doReturn(vmTemplateDAO).when(cmd).getVmTemplateDAO();
        doReturn(diskImageDAO).when(cmd).getDiskImageDao();
        doReturn(sdDAO).when(cmd).getStorageDomainDAO();
        doReturn(snapshotValidator).when(cmd).createSnapshotValidator();
        doReturn(STORAGE_POOLD_ID).when(cmd).getStoragePoolId();
        mockConfigSizeDefaults();
    }

    private void mockConfigSizeRequirements(int requiredSpaceBufferInGB) {
        mcr.mockConfigValue(ConfigValues.FreeSpaceCriticalLowInGB, requiredSpaceBufferInGB);
    }

    private void mockConfigSizeDefaults() {
        int requiredSpaceBufferInGB = 5;
        mockConfigSizeRequirements(requiredSpaceBufferInGB);
    }

    private void mockStorageDomainDAOGetForStoragePool(int domainSpaceGB, Guid storageDomainId) {
        when(sdDAO.getForStoragePool(storageDomainId, STORAGE_POOLD_ID)).thenReturn(createStorageDomain(domainSpaceGB,
                storageDomainId));
    }

    @Test
    public void testValidateImageNotInTemplateTrue() {
        when(vmTemplateDAO.get(mockSourceImage())).thenReturn(null);
        assertTrue("validation should succeed", cmd.validateImageNotInTemplate());
    }

    @Test
    public void testValidateImageNotInTemplateFalse() {
        when(vmTemplateDAO.get(mockSourceImage())).thenReturn(new VmTemplate());
        assertFalse("validation should succeed", cmd.validateImageNotInTemplate());
    }

    @Test
    public void testValidateImageNotActiveTrue() {
        when(diskImageDAO.get(mockSourceImage())).thenReturn(null);
        assertTrue("validation should succeed", cmd.validateImageNotActive());
    }

    @Test
    public void testValidateImageNotActiveFalse() {
        when(diskImageDAO.get(mockSourceImage())).thenReturn(new DiskImage());
        assertFalse("validation should succeed", cmd.validateImageNotActive());
    }

    @Test
    public void testEnoughSpaceToMergeSnapshotsWithOneDisk() {
        when(diskImageDAO.get(mockSourceImage())).thenReturn(new DiskImage());
        mockStorageDomainDAOGetForStoragePool(10, STORAGE_DOMAIN_ID);
        assertTrue("Validation should succeed. Free space minus threshold should be bigger then disk size",
                cmd.validateStorageDomains());
    }

    @Test
    public void testNotEnoughSpaceToMergeSnapshotsWithOneDisk() {
        when(diskImageDAO.get(mockSourceImage())).thenReturn(new DiskImage());
        mockStorageDomainDAOGetForStoragePool(3, STORAGE_DOMAIN_ID);
        assertFalse("Validation should fail. Free space minus threshold should be smaller then disk size",
                cmd.validateStorageDomains());
    }

    @Test
    public void testEnoughSpaceToMergeSnapshotsWithMultipleDisk() {
        List<DiskImage> imagesDisks = mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID, IMAGE_ACTUAL_SIZE_GB);
        doReturn(imagesDisks).when(cmd).getSourceImages();
        mockStorageDomainDAOGetForStoragePool(22, STORAGE_DOMAIN_ID);
        assertTrue("Validation should succeed. Free space minus threshold should be bigger then summarize all disks size",
                cmd.validateStorageDomains());
    }

    @Test
    public void testNotEnoughSpaceToMergeSnapshotsWithMultipleDisk() {
        List<DiskImage> imagesDisks = mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID, IMAGE_ACTUAL_SIZE_GB);
        doReturn(imagesDisks).when(cmd).getSourceImages();
        mockStorageDomainDAOGetForStoragePool(15, STORAGE_DOMAIN_ID);
        assertFalse("Validation should fail. Free space minus threshold should be smaller then summarize all disks size",
                cmd.validateStorageDomains());
    }

    @Test
    public void testEnoughSpaceToMergeSnapshotsWithMultipleDiskAndDomains() {
        List<DiskImage> imagesDisks = mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID, IMAGE_ACTUAL_SIZE_GB);
        imagesDisks.addAll(mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID2, IMAGE_ACTUAL_SIZE_GB));
        doReturn(imagesDisks).when(cmd).getSourceImages();
        mockStorageDomainDAOGetForStoragePool(22, STORAGE_DOMAIN_ID);
        mockStorageDomainDAOGetForStoragePool(22, STORAGE_DOMAIN_ID2);
        assertTrue("Validation should succeed. Free space minus threshold should be bigger then summarize all disks size for each domain",
                cmd.validateStorageDomains());
    }

    @Test
    public void testNotEnoughForMultipleDiskAndDomainsFirstDomainFails() {
        List<DiskImage> imagesDisks = mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID, IMAGE_ACTUAL_SIZE_GB);
        imagesDisks.addAll(mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID2, IMAGE_ACTUAL_SIZE_GB));
        doReturn(imagesDisks).when(cmd).getSourceImages();
        mockStorageDomainDAOGetForStoragePool(15, STORAGE_DOMAIN_ID);
        mockStorageDomainDAOGetForStoragePool(22, STORAGE_DOMAIN_ID2);
        assertFalse("Validation should fail. First domain should not have enough free space for request.",
                cmd.validateStorageDomains());
    }

    @Test
    public void testNotEnoughForMultipleDiskAndDomainsSecondDomainFails() {
        List<DiskImage> imagesDisks = mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID, IMAGE_ACTUAL_SIZE_GB);
        imagesDisks.addAll(mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID2, IMAGE_ACTUAL_SIZE_GB));
        doReturn(imagesDisks).when(cmd).getSourceImages();
        mockStorageDomainDAOGetForStoragePool(22, STORAGE_DOMAIN_ID);
        mockStorageDomainDAOGetForStoragePool(10, STORAGE_DOMAIN_ID2);
        assertFalse("Validation should fail. Second domain should not have enough free space for request.",
                cmd.validateStorageDomains());
    }

    @Test
    public void testNotEnoughForMultipleDiskAndDomainsAllDomainsFail() {
        List<DiskImage> imagesDisks = mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID, IMAGE_ACTUAL_SIZE_GB);
        imagesDisks.addAll(mockMultipleSourceImagesForDomain(4, STORAGE_DOMAIN_ID2, IMAGE_ACTUAL_SIZE_GB));
        doReturn(imagesDisks).when(cmd).getSourceImages();
        mockStorageDomainDAOGetForStoragePool(10, STORAGE_DOMAIN_ID);
        mockStorageDomainDAOGetForStoragePool(10, STORAGE_DOMAIN_ID2);
        assertFalse("Validation should fail. Second domain should not have enough free space for request.",
                cmd.validateStorageDomains());
    }

    @Test
    public void testCanDoActionVmUp() {
        VM vm = new VM();
        vm.setId(Guid.NewGuid());
        vm.setStatus(VMStatus.Up);
        vm.setStoragePoolId(STORAGE_POOLD_ID);

        storage_pool sp = new storage_pool();
        sp.setId(STORAGE_POOLD_ID);
        sp.setstatus(StoragePoolStatus.Up);

        cmd.setSnapshotName("someSnapshot");
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotInPreview(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotValidator).snapshotExists(any(Guid.class), any(Guid.class));
        doReturn(true).when(cmd).validateImages();
        doReturn(vm).when(cmd).getVm();
        doReturn(sp).when(spDao).get(STORAGE_POOLD_ID);
        doReturn(Collections.emptyList()).when(cmd).getSourceImages();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    /** Mocks a call to {@link RemoveSnapshotCommand#getSourceImages()} and returns its image guid */
    private Guid mockSourceImage() {
        Guid imageId = Guid.NewGuid();
        DiskImage image = new DiskImage();
        image.setImageId(imageId);
        ArrayList<Guid> list = new ArrayList<Guid>();
        list.add(STORAGE_DOMAIN_ID);
        image.setStorageIds(list);
        image.setActualSize(IMAGE_ACTUAL_SIZE_GB);
        image.setSize(40);
        doReturn(Collections.singletonList(image)).when(cmd).getSourceImages();
        return imageId;
    }

    /** Mocks a call to {@link RemoveSnapshotCommand#getSourceImages()} and returns list of images */
    private static List<DiskImage> mockMultipleSourceImagesForDomain(int numberOfDisks, Guid storageDomainId, int actualDiskSize) {
        List<DiskImage> listDisks = new ArrayList<DiskImage>();
        for (int index=0; index < numberOfDisks; index++) {
            Guid imageId = Guid.NewGuid();
            DiskImage image = new DiskImage();
            image.setImageId(imageId);
            ArrayList<Guid> list = new ArrayList<Guid>();
            list.add(storageDomainId);
            image.setStorageIds(list);
            image.setActualSize(actualDiskSize);
            listDisks.add(image);
        }
        return listDisks;
    }

    private static StorageDomain createStorageDomain(int availableSpace, Guid storageDomainId) {
        StorageDomain sd = new StorageDomain();
        sd.setStorageDomainType(StorageDomainType.Master);
        sd.setStatus(StorageDomainStatus.Active);
        sd.setAvailableDiskSize(availableSpace);
        sd.setUsedDiskSize(USED_SPACE_GB);
        sd.setStoragePoolId(STORAGE_POOLD_ID);
        sd.setId(storageDomainId);
        return sd;
    }
}
