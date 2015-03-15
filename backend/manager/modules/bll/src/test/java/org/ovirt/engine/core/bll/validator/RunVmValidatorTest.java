package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class RunVmValidatorTest {

    private static final int _64_BIT_OS = 13;

    public static final int MEMORY_LIMIT_32_BIT = 32000;
    public static final int MEMORY_LIMIT_64_BIT = 640000;
    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.VdsSelectionAlgorithm, "General", "0"),
            mockConfig(ConfigValues.PredefinedVMProperties, "3.0", "0"),
            mockConfig(ConfigValues.UserDefinedVMProperties, "3.0", "0"),
            mockConfig(ConfigValues.VM32BitMaxMemorySizeInMB, "general", MEMORY_LIMIT_32_BIT),
            mockConfig(ConfigValues.VM64BitMaxMemorySizeInMB, "3.3", MEMORY_LIMIT_64_BIT)
            );

    @Spy
    private RunVmValidator runVmValidator = new RunVmValidator();

    @Before
    public void setup() {
        mockVmPropertiesUtils();
    }

    @Test
    public void testValidEmptyCustomProerties() {
        VM vm = new VM();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertTrue(runVmValidator.validateVmProperties(vm, "", messages));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testWrongFormatCustomProerties() {
        VM vm = new VM();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertFalse(runVmValidator.validateVmProperties(vm, "sap_agent;", messages)); // missing '= true'
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testNotValidCustomProerties() {
        VM vm = new VM();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertFalse(runVmValidator.validateVmProperties(vm, "property=value;", messages));
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testValidCustomProerties() {
        VM vm = new VM();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertTrue(runVmValidator.validateVmProperties(vm, "sap_agent=true;", messages));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testVmFailNoDisks() {
        validateResult(runVmValidator.validateBootSequence(new VM(), null, new ArrayList<Disk>(), null),
                       false,
                       VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK);
    }

    @Test
    public void testVmWithDisks() {
        List<Disk> disks = new ArrayList<Disk>();
        disks.add(new DiskImage());
        validateResult(runVmValidator.validateBootSequence(new VM(), null, disks, null),
                true,
                null);
    }

    @Test
    public void testNoIsoDomain() {
        validateResult(runVmValidator.validateBootSequence(new VM(), BootSequence.CD, new ArrayList<Disk>(), null),
                false,
                VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
    }

    @Test
    public void testNoDiskBootFromIsoDomain() {
        validateResult(runVmValidator.validateBootSequence(new VM(), BootSequence.CD, new ArrayList<Disk>(), Guid.newGuid()),
                true,
                null);
    }

    @Test
    public void testBootFromNetworkNoNetwork() {
        VmNicDao dao = mock(VmNicDao.class);
        doReturn(new ArrayList<VmNic>()).when(dao).getAllForVm(any(Guid.class));
        doReturn(dao).when(runVmValidator).getVmNicDao();
        validateResult(runVmValidator.validateBootSequence(new VM(), BootSequence.N, new ArrayList<Disk>(), null),
                false,
                VdcBllMessages.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK);
    }

    @Test
    public void canRunVmFailVmRunning() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Up);
        doReturn(false).when(runVmValidator).isVmDuringInitiating(any(VM.class));
        validateResult(runVmValidator.vmDuringInitialization(vm),
                false,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
    }

    @Test
    public void canRunVmDuringInit() {
        final VM vm = new VM();
        doReturn(true).when(runVmValidator).isVmDuringInitiating(any(VM.class));
        validateResult(runVmValidator.vmDuringInitialization(vm),
                false,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
    }

    @Test
    public void canRunVmNotResponding() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.NotResponding);
        doReturn(false).when(runVmValidator).isVmDuringInitiating(any(VM.class));
        validateResult(runVmValidator.vmDuringInitialization(vm),
                false,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_RUNNING);
    }

    @Test
    public void testVmNotDuringInitialization() {
        final VM vm = new VM();
        vm.setStatus(VMStatus.Down);
        doReturn(false).when(runVmValidator).isVmDuringInitiating(any(VM.class));
        validateResult(runVmValidator.vmDuringInitialization(vm),
                true,
                null);
    }

    @Test
    public void passNotStatelessVM() {
        Random rand = new Random();
        canRunVmAsStateless(rand.nextBoolean(), rand.nextBoolean(), false, false, true, null);
        canRunVmAsStateless(rand.nextBoolean(), rand.nextBoolean(), false, null, true, null);
    }

    @Test
    public void failRunStatelessSnapshotInPreview() {
        Random rand = new Random();
        canRunVmAsStateless(rand.nextBoolean(),
                true,
                true,
                true,
                false,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
        canRunVmAsStateless(rand.nextBoolean(),
                true,
                true,
                null,
                false,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
        canRunVmAsStateless(rand.nextBoolean(),
                true,
                false,
                true,
                false,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
    }

    @Test
    public void failRunStatelessHA_VM() {
        canRunVmAsStateless(true,
                false,
                true,
                true,
                false,
                VdcBllMessages.VM_CANNOT_RUN_STATELESS_HA);
        canRunVmAsStateless(true,
                false,
                true,
                null,
                false,
                VdcBllMessages.VM_CANNOT_RUN_STATELESS_HA);
        canRunVmAsStateless(true,
                false,
                false,
                true,
                false,
                VdcBllMessages.VM_CANNOT_RUN_STATELESS_HA);
    }

    private void mockOsRepository() {
        OsRepository osRepository = mock(OsRepository.class);
        when(osRepository.get64bitOss()).thenReturn(new ArrayList<Integer>() {{ add(_64_BIT_OS); }});
        when(runVmValidator.getOsRepository()).thenReturn(osRepository);
    }

    @Test
    public void test32BitMemoryExceedsLimit() {
        VM vm = new VM();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        vm.setVmMemSizeMb(MEMORY_LIMIT_32_BIT + 1);
        mockOsRepository();
        validateResult(runVmValidator.validateMemorySize(vm), false, VdcBllMessages.ACTION_TYPE_FAILED_MEMORY_EXCEEDS_SUPPORTED_LIMIT);
    }

    @Test
    public void test64BitMemoryExceedsLimit() {
        VM vm = new VM();
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        vm.setVmMemSizeMb(MEMORY_LIMIT_64_BIT + 1);
        vm.setVmOs(_64_BIT_OS);
        mockOsRepository();
        validateResult(runVmValidator.validateMemorySize(vm), false, VdcBllMessages.ACTION_TYPE_FAILED_MEMORY_EXCEEDS_SUPPORTED_LIMIT);
    }

    private void canRunVmAsStateless(boolean autoStartUp,
            final boolean vmInPreview,
            boolean isVmStateless,
            Boolean isStatelessParam,
            boolean shouldPass,
            VdcBllMessages message) {
        runVmValidator = new RunVmValidator() {
            @Override
            protected SnapshotsValidator getSnapshotValidator() {
                return new SnapshotsValidator() {
                    @Override
                    public ValidationResult vmNotInPreview(Guid vmId) {
                        if (vmInPreview) {
                            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IN_PREVIEW);
                        }
                        return ValidationResult.VALID;
                    };
                };
            };

            @Override
            public ValidationResult hasSpaceForSnapshots() {
                return ValidationResult.VALID;
            }
        };
        VM vm = new VM();
        vm.setAutoStartup(autoStartUp);
        vm.setStateless(isVmStateless);
        validateResult(runVmValidator.validateStatelessVm(vm, isStatelessParam),
                shouldPass,
                message);
    }

    private VmPropertiesUtils mockVmPropertiesUtils() {
        VmPropertiesUtils utils = spy(new VmPropertiesUtils());
        doReturn("sap_agent=^(true|false)$;sndbuf=^[0-9]+$;" +
                "vhost=^(([a-zA-Z0-9_]*):(true|false))(,(([a-zA-Z0-9_]*):(true|false)))*$;" +
                "viodiskcache=^(none|writeback|writethrough)$").
                when(utils)
                .getPredefinedVMProperties(any(Version.class));
        doReturn("").
                when(utils)
                .getUserdefinedVMProperties(any(Version.class));
        doReturn(new HashSet<Version>(Arrays.asList(Version.v3_2, Version.v3_3))).
                when(utils)
                .getSupportedClusterLevels();
        doReturn(utils).when(runVmValidator).getVmPropertiesUtils();
        try {
            utils.init();
        } catch (InitializationException e) {
            e.printStackTrace();
        }
        return utils;
    }

    private static void validateResult(ValidationResult validationResult, boolean isValid, VdcBllMessages message) {
        assertEquals(isValid, validationResult.isValid());
        assertEquals(message, validationResult.getMessage());
    }

}
