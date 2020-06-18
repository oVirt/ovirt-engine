package org.ovirt.engine.core.bll.network.vm.mac;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class VmNicFilterParameterAnnotationTest {

    static final String VALID_NAME = "IP";
    static final String INVALID_NAME0 = "BAD-PATTERN";
    static final String INVALID_NAME1 = "10.0.0.1";

    static final String VALID_VALUE = "10.0.0.1";
    static final String INVALID_VALUE0 = "BAD-PATTERN";
    static final String INVALID_VALUE1 = "BAD\\PATTERN";

    static final Guid VALID_VM_INTERFACE_ID = Guid.newGuid();

    public static Stream<Arguments> validate() {
        return Stream.of(
                Arguments.of(VALID_NAME, VALID_VALUE, VALID_VM_INTERFACE_ID, null),
                Arguments.of(INVALID_NAME0, VALID_VALUE, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_NAME"),
                Arguments.of(INVALID_NAME1, VALID_VALUE, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_NAME"),
                Arguments.of(null, VALID_VALUE, VALID_VM_INTERFACE_ID, "must not be null"),
                Arguments.of(VALID_NAME, INVALID_VALUE0, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_VALUE"),
                Arguments.of(VALID_NAME, INVALID_VALUE1, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_VALUE"),
                Arguments.of(VALID_NAME, null, VALID_VM_INTERFACE_ID, "must not be null"),
                Arguments.of(VALID_NAME, VALID_VALUE, null, "must not be null")
        );
    }

    private final Validator validator = ValidationUtils.getValidator();
    private static final Class<?>[] VALIDATE_GROUPS = { CreateEntity.class, UpdateEntity.class };

    @ParameterizedTest
    @MethodSource
    public void validate(String name, String value, Guid vmInterfaceId, String expectedErrorMessage) {
        VmNicFilterParameter vmNicFilterParameter = new VmNicFilterParameter();
        vmNicFilterParameter.setName(name);
        vmNicFilterParameter.setValue(value);
        vmNicFilterParameter.setVmInterfaceId(vmInterfaceId);

        Set<ConstraintViolation<VmNicFilterParameter>> violations =
                validator.validate(vmNicFilterParameter, VALIDATE_GROUPS);

        boolean expectViolations = expectedErrorMessage != null;
        assertTrue(expectViolations !=  violations.isEmpty());
        if (expectViolations) {
            assertEquals(expectedErrorMessage, violations.iterator().next().getMessage());
        }
    }
}
