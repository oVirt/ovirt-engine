package org.ovirt.engine.core.bll.network.vm.mac;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

@RunWith(Parameterized.class)
public class VmNicFilterParameterAnnotationTest {

    static final String VALID_NAME = "IP";
    static final String INVALID_NAME0 = "BAD-PATTERN";
    static final String INVALID_NAME1 = "10.0.0.1";

    static final String VALID_VALUE = "10.0.0.1";
    static final String INVALID_VALUE0 = "BAD-PATTERN";
    static final String INVALID_VALUE1 = "BAD\\PATTERN";

    static final Guid VALID_VM_INTERFACE_ID = Guid.newGuid();

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public String value;

    @Parameterized.Parameter(2)
    public Guid vmInterfaceId;

    @Parameterized.Parameter(3)
    public String expectedErrorMessage;

    @Parameterized.Parameters
    public static Object[][] vmNicFilterParameters() {
        return new Object[][] {
                { VALID_NAME, VALID_VALUE, VALID_VM_INTERFACE_ID, null },
                { INVALID_NAME0, VALID_VALUE, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_NAME" },
                { INVALID_NAME1, VALID_VALUE, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_NAME" },
                { null, VALID_VALUE, VALID_VM_INTERFACE_ID, "may not be null" },
                { VALID_NAME, INVALID_VALUE0, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_VALUE" },
                { VALID_NAME, INVALID_VALUE1, VALID_VM_INTERFACE_ID,
                        "ACTION_TYPE_FAILED_INVALID_NIC_FILTER_PARAMETER_VALUE" },
                { VALID_NAME, null, VALID_VM_INTERFACE_ID, "may not be null" },
                { VALID_NAME, VALID_VALUE, null, "may not be null" },
        };
    }

    private final Validator validator = ValidationUtils.getValidator();
    private static final Class<?>[] VALIDATE_GROUPS = { CreateEntity.class, UpdateEntity.class };

    @Test
    public void runTest() {
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
