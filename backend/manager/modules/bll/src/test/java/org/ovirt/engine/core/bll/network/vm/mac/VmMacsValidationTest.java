package org.ovirt.engine.core.bll.network.vm.mac;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

@RunWith(MockitoJUnitRunner.class)
public class VmMacsValidationTest {

    @Mock
    private ProblematicVmMacsFinder mockProblematicVmMacsFinder;

    @Mock
    private VM mockVM;

    private VmMacsValidation underTest;

    @Before
    public void setUp() {
        underTest = new VmMacsValidation(EngineMessage.Unassigned, mockProblematicVmMacsFinder);
    }

    @Test
    public void testValidateValid() {
        when(mockProblematicVmMacsFinder.findProblematicMacs(mockVM)).thenReturn(Collections.emptyList());

        final ValidationResult actual = underTest.validate(mockVM);

        assertThat(actual, isValid());
    }

    @Test
    public void testValidateInvalid() {
        final List<String> invalidMacs = Arrays.asList("mac1", "mac2");
        when(mockProblematicVmMacsFinder.findProblematicMacs(mockVM)).thenReturn(invalidMacs);

        final ValidationResult actual = underTest.validate(mockVM);

        assertThat(actual,
                failsWith(EngineMessage.Unassigned,
                        ReplacementUtils.getListVariableAssignmentString(EngineMessage.Unassigned, invalidMacs)));
    }

}
