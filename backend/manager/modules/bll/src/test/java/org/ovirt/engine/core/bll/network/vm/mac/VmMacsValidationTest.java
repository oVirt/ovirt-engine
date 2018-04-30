package org.ovirt.engine.core.bll.network.vm.mac;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ReplacementUtils;

@ExtendWith(MockitoExtension.class)
public class VmMacsValidationTest {

    @Mock
    private ProblematicVmMacsFinder mockProblematicVmMacsFinder;

    @Mock
    private VM mockVM;

    private VmMacsValidation underTest;

    @BeforeEach
    public void setUp() {
        underTest = new VmMacsValidation(EngineMessage.Unassigned, mockProblematicVmMacsFinder);
    }

    @Test
    public void testValidateValid() {
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
