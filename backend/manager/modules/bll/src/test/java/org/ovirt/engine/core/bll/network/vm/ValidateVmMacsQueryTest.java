package org.ovirt.engine.core.bll.network.vm;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.bll.network.vm.mac.VmMacsValidation;
import org.ovirt.engine.core.bll.network.vm.mac.VmMacsValidationsFactory;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.ValidateVmMacsParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class ValidateVmMacsQueryTest {

    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid VM_ID1 = Guid.newGuid();
    private static final Guid VM_ID2 = Guid.newGuid();
    private static final String REPLACEMENT1 = "replacement1";
    private static final String REPLACEMENT2 = "replacement2";
    private static final List<String> VALIDATION_MESSAGES =
            Arrays.asList(EngineMessage.Unassigned.name(), REPLACEMENT1, REPLACEMENT2);

    @Mock
    private MacPoolPerCluster macPoolPerCluster;
    @Mock
    private VmMacsValidationsFactory vmMacsValidationsFactory;
    @Mock
    private ReadMacPool readMacPool;
    @Mock
    private VmMacsValidation vmMacsValidation;

    private final HashMap<Guid, List<VM>> vmsByCluster = new HashMap<>();
    private final List<VM> vms = new ArrayList<>();
    private ValidateVmMacsParameters params = new ValidateVmMacsParameters(vmsByCluster);

    @InjectMocks
    private ValidateVmMacsQuery<ValidateVmMacsParameters> underTest = new ValidateVmMacsQuery<>(params);

    @Before
    public void setUp() {
        vmsByCluster.put(CLUSTER_ID, vms);

        when(macPoolPerCluster.getMacPoolForCluster(CLUSTER_ID)).thenReturn(readMacPool);
        when(vmMacsValidationsFactory.createVmMacsValidationList(CLUSTER_ID, readMacPool))
                .thenReturn(Collections.singletonList(vmMacsValidation));
    }

    @Test
    public void testExecuteQueryCommand() {
        final VM vm1 = createVm(VM_ID1);
        final VM vm2 = createVm(VM_ID2);
        vms.add(vm1);
        vms.add(vm2);

        when(vmMacsValidation.validate(vm1)).thenReturn(ValidationResult.VALID);
        when(vmMacsValidation.validate(vm2))
                .thenReturn(new ValidationResult(EngineMessage.Unassigned, REPLACEMENT1, REPLACEMENT2));

        underTest.executeQueryCommand();
        final VdcQueryReturnValue queryReturnValue = underTest.getQueryReturnValue();
        final Map<Guid, List<List<String>>> actual = queryReturnValue.getReturnValue();

        assertValidVmInOutput(actual, VM_ID1);
        assertInvalidVmInOutput(actual, VM_ID2, VALIDATION_MESSAGES);
    }

    private void assertInvalidVmInOutput(Map<Guid, List<List<String>>> returnValue,
            Guid vmId,
            List<String> validationMessages) {
        assertVmInOutput(returnValue, vmId, hasItem(validationMessages));
    }

    private VM createVm(Guid vmId) {
        final VM vm = new VM();
        vm.setId(vmId);
        return vm;
    }

    private void assertValidVmInOutput(Map<Guid, List<List<String>>> returnValue, Guid vmId) {
        assertVmInOutput(returnValue, vmId, empty());
    }

    private void assertVmInOutput(Map<Guid, List<List<String>>> returnValue,
            Guid vmId,
            Matcher violationsMatcher) {
        assertThat(returnValue, hasKey(vmId));
        final List<List<String>> vmViolations = returnValue.get(vmId);
        assertThat(vmViolations, violationsMatcher);
    }
}
