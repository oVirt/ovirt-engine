package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.action.MigrateMultipleVmsParameters;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MigrateMultipleVmsCommandTest extends BaseCommandTest {

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        Map<String, String> migrationMap = new HashMap<>();
        migrationMap.put("undefined", "true");
        migrationMap.put("x86", "true");
        migrationMap.put("ppc", "true");

        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.IsMigrationSupported, Version.getLast(), migrationMap)
        );
    }

    @Mock
    private VmDao vmDao;
    @Mock
    private SchedulingManager schedulingManager;
    @Mock
    private VmValidator vmValidator;

    private Cluster cluster;
    private List<Guid> vmIds = new ArrayList<>();
    private List<VM> vms = new ArrayList<>();
    private Map<Guid, List<VDS>> possibleHosts = new HashMap<>();

    @InjectMocks
    @Spy
    private MigrateMultipleVmsCommand<MigrateMultipleVmsParameters> command = new MigrateMultipleVmsCommand<>(
            new MigrateMultipleVmsParameters(vmIds, false),
            null);

    @BeforeEach
    public void setUp() {
        cluster = new Cluster();
        cluster.setId(Guid.newGuid());
        cluster.setArchitecture(ArchitectureType.x86_64);
        cluster.setCompatibilityVersion(Version.getLast());
        command.setCluster(cluster);

        createAndAddVm();
        createAndAddVm();
        createAndAddVm();

        when(vmDao.getVmsByIds(any())).thenAnswer(invocation -> {
            Collection<Guid> ids = invocation.getArgument(0);
            return vms.stream()
                    .filter(vm -> ids.contains(vm.getId()))
                    .collect(Collectors.toList());
        });

        when(vmValidator.canMigrate(anyBoolean())).thenReturn(ValidationResult.VALID);
        doReturn(vmValidator).when(command).getVmValidator(any(VM.class));

        when(schedulingManager.canSchedule(any(), any(), any(), any(), anyBoolean(), anyBoolean(), any())).thenReturn(possibleHosts);
        when(schedulingManager.prepareCall(any())).thenCallRealMethod();
    }

    @Test
    public void testVmsEmpty() {
        vmIds.clear();
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_NO_VMS_SPECIFIED);
    }

    @Test
    public void testVmsInMultipleClusters() {
        vms.forEach(vm -> vm.setClusterId(Guid.newGuid()));
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VMS_NOT_RUNNING_ON_SINGLE_CLUSTER);
    }

    @Test
    public void testSomeVmsNotExist() {
        vms.remove(0);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VMS_NOT_FOUND);
    }

    @Test
    public void testSomeVmsFailValidator() {
        VmValidator failingValidator = Mockito.mock(VmValidator.class);
        when(failingValidator.canMigrate(anyBoolean()))
                .thenReturn(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_PINNED_TO_HOST));

        doAnswer(invocation -> {
            VM vm = invocation.getArgument(0);
            return vm.getId().equals(vmIds.get(0)) ?
                    failingValidator :
                    vmValidator;
        }).when(command).getVmValidator(any(VM.class));

        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_VM_IS_PINNED_TO_HOST);
    }

    @Test
    public void testSomeVmsCannotBeScheduled() {
        possibleHosts.remove(vmIds.get(0));
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testAllVmsCannotBeScheduled() {
        possibleHosts.clear();
        assertFalse(command.validate());
    }

    private void createAndAddVm() {
        VM vm = new VM();
        vm.setId(Guid.newGuid());
        vm.setClusterId(cluster.getId());

        vms.add(vm);
        vmIds.add(vm.getId());

        VDS host = new VDS();
        host.setId(Guid.newGuid());
        possibleHosts.put(vm.getId(), Collections.singletonList(host));
    }
}
