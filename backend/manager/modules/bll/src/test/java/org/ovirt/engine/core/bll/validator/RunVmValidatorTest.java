package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.IsoDomainListSyncronizer;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.exceptions.InitializationException;
import org.ovirt.engine.core.utils.vmproperties.VmPropertiesUtils;

@RunWith(MockitoJUnitRunner.class)
public class RunVmValidatorTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.VdsSelectionAlgorithm, "General", "0"),
            mockConfig(ConfigValues.PredefinedVMProperties, "3.0", "0"),
            mockConfig(ConfigValues.UserDefinedVMProperties, "3.0", "0")
            );

    @Spy
    private RunVmValidator runVmValidator = new RunVmValidator();

    @Before
    public void setup() {
        mockVmPropertiesUtils();
        mockIsoDomainListSyncronizer();
    }

    @Test
    public void testValidEmptyCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("");
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertTrue(runVmValidator.validateVmProperties(vm, messages));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testWrongFormatCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("sap_agent;"); // missing '= true'
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertFalse(runVmValidator.validateVmProperties(vm, messages));
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testNotValidCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("property=value;");
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertFalse(runVmValidator.validateVmProperties(vm, messages));
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testValidCustomProerties() {
        VM vm = new VM();
        vm.setCustomProperties("sap_agent=true;");
        vm.setVdsGroupCompatibilityVersion(Version.v3_3);
        List<String> messages = new ArrayList<String>();
        assertTrue(runVmValidator.validateVmProperties(vm, messages));
        assertTrue(messages.isEmpty());
    }

    @Test
    public void testVmFailNoDisks() {
        validateResult(runVmValidator.validateBootSequence(new VM(), null, new ArrayList<Disk>()),
                false,
                VdcBllMessages.VM_CANNOT_RUN_FROM_DISK_WITHOUT_DISK);
    }

    @Test
    public void testVmWithDisks() {
        List<Disk> disks = new ArrayList<Disk>();
        disks.add(new DiskImage());
        validateResult(runVmValidator.validateBootSequence(new VM(), null, disks),
                true,
                null);
    }

    @Test
    public void testNoIsoDomain() {
        validateResult(runVmValidator.validateBootSequence(new VM(), BootSequence.CD, new ArrayList<Disk>()),
                false,
                VdcBllMessages.VM_CANNOT_RUN_FROM_CD_WITHOUT_ACTIVE_STORAGE_DOMAIN_ISO);
    }

    @Test
    public void testNoDiskBootFromIsoDomain() {
        IsoDomainListSyncronizer mock = mockIsoDomainListSyncronizer();
        doReturn(new Guid()).when(mock).findActiveISODomain(any(Guid.class));
        validateResult(runVmValidator.validateBootSequence(new VM(), BootSequence.CD, new ArrayList<Disk>()),
                true,
                null);
    }

    @Test
    public void testBootFromNetworkNoNetwork() {
        VmNetworkInterfaceDao dao = mock(VmNetworkInterfaceDao.class);
        doReturn(new ArrayList<VmNetworkInterface>()).when(dao).getAllForVm(any(Guid.class));
        doReturn(dao).when(runVmValidator).getVmNetworkInterfaceDao();
        validateResult(runVmValidator.validateBootSequence(new VM(), BootSequence.N, new ArrayList<Disk>()),
                false,
                VdcBllMessages.VM_CANNOT_RUN_FROM_NETWORK_WITHOUT_NETWORK);
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

    private IsoDomainListSyncronizer mockIsoDomainListSyncronizer() {
        IsoDomainListSyncronizer isoDomainListSyncronizer = mock(MockIsoDomainListSyncronizer.class);
        doReturn(isoDomainListSyncronizer).when(runVmValidator).getIsoDomainListSyncronizer();
        return isoDomainListSyncronizer;
    }

    private static void validateResult(ValidationResult validationResult, boolean isValid, VdcBllMessages message) {
        assertEquals(isValid, validationResult.isValid());
        assertEquals(message, validationResult.getMessage());
    }

    class MockIsoDomainListSyncronizer extends IsoDomainListSyncronizer {
        @Override
        protected void init() {
            // empty impl
        }
    }

}
