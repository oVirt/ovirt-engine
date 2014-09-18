package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

/**
 * A test case for {@link AddVmTemplateCommand}
 */
@RunWith(MockitoJUnitRunner.class)
public class AddVmTemplateCommandTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(mockConfig(ConfigValues.VmPriorityMaxValue, 100));
    private AddVmTemplateCommand<AddVmTemplateParameters> cmd;
    private VM vm;
    private VDSGroup vdsGroup;
    private Guid spId;
    @Mock
    private VmDAO vmDao;
    @Mock
    private VdsGroupDAO vdsGroupDao;
    @Mock
    private StoragePoolDAO storagePoolDao;
    @Mock
    private OsRepository osRepository;
    @Mock
    private MultipleStorageDomainsValidator multipleSdValidator;
    @Mock
    private DiskImagesValidator diskImagesValidator;

    @Before
    public void setUp() {
        // The VM to use
        Guid vmId = Guid.newGuid();
        Guid vdsGroupId = Guid.newGuid();
        spId = Guid.newGuid();

        vm = new VM();
        vm.setId(vmId);
        vm.setVdsGroupId(vdsGroupId);
        vm.setStoragePoolId(spId);
        vm.setVmOs(14);
        when(vmDao.get(vmId)).thenReturn(vm);

        // The cluster to use
        vdsGroup = new VDSGroup();
        vdsGroup.setcpu_name("Intel Conroe Family");
        vdsGroup.setArchitecture(ArchitectureType.x86_64);
        vdsGroup.setId(vdsGroupId);
        vdsGroup.setStoragePoolId(spId);
        vdsGroup.setcompatibility_version(Version.v3_2);
        when(vdsGroupDao.get(vdsGroupId)).thenReturn(vdsGroup);
        AddVmTemplateParameters params = new AddVmTemplateParameters(vm, "templateName", "Template for testing");

        mockOsRepository();

        // Using the compensation constructor since the normal one contains DB access
        cmd = spy(new AddVmTemplateCommand<AddVmTemplateParameters>(params) {

            @Override
            protected List<DiskImage> getVmDisksFromDB() {
                return getDisksList(spId);
            }

            @Override
            protected void updateVmDevices() {
            }

            @Override
            public VM getVm() {
                return vm;
            }
        });
        doReturn(vmDao).when(cmd).getVmDAO();
        doReturn(vdsGroupDao).when(cmd).getVdsGroupDAO();
        cmd.setVmId(vmId);
        cmd.setVdsGroupId(vdsGroupId);
    }

    protected void mockOsRepository() {
        SimpleDependecyInjector.getInstance().bind(OsRepository.class, osRepository);
        VmHandler.init();
        when(osRepository.isWindows(0)).thenReturn(true);
        when(osRepository.getMinimumRam(vm.getVmOsId(), Version.v3_2)).thenReturn(0);
        when(osRepository.getMaximumRam(vm.getVmOsId(), Version.v3_2)).thenReturn(100);
        when(osRepository.getArchitectureFromOS(14)).thenReturn(ArchitectureType.x86_64);
    }

    @Test
    public void testCanDoAction() {
        doReturn(true).when(cmd).validateVmNotDuringSnapshot();
        vm.setStatus(VMStatus.Up);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.VMT_CANNOT_CREATE_TEMPLATE_FROM_DOWN_VM);
    }

    @Test
    public void sufficientStorageSpace() {
        setupForStorageTests();
        assertTrue(cmd.imagesRelatedChecks());
    }

    @Test
    public void storageSpaceNotWithinThreshold() {
        setupForStorageTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsWithinThresholds();
        assertFalse(cmd.imagesRelatedChecks());
    }

    @Test
    public void insufficientStorageSpace() {
        setupForStorageTests();
        doReturn(new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN)).
                when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        assertFalse(cmd.imagesRelatedChecks());
    }


    @Test
    public void testBeanValidations() {
        assertTrue(cmd.validateInputs());
    }

    @Test
    public void testPatternBasedNameFails() {
        cmd.getParameters().setName("aa-??bb");
        assertFalse("Pattern-based name should not be supported for Template", cmd.validateInputs());
    }

    private void setupForStorageTests() {
        doReturn(true).when(cmd).validateVmNotDuringSnapshot();
        vm.setStatus(VMStatus.Down);
        doReturn(multipleSdValidator).when(cmd).getStorageDomainsValidator(any(Guid.class), anySet());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsHaveSpaceForClonedDisks(anyList());
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsWithinThresholds();
        doReturn(ValidationResult.VALID).when(multipleSdValidator).allDomainsExistAndActive();
        doReturn(ValidationResult.VALID).when(diskImagesValidator).diskImagesNotIllegal();

        setupStoragePool();
    }

    private void setupStoragePool() {
        StoragePool storagePool = new StoragePool();
        storagePool.setId(spId);
        storagePool.setStatus(StoragePoolStatus.Up);
        doReturn(storagePoolDao).when(cmd).getStoragePoolDAO();
        when(storagePoolDao.get(spId)).thenReturn(storagePool);
    }

    private List<DiskImage> getDisksList(Guid spId) {
        List disksList = new ArrayList(1);
        DiskImage disk = new DiskImage();
        disk.setStorageIds(new ArrayList<>(Collections.singletonList(spId)));
        disksList.add(disk);
        return disksList;
    }
}
