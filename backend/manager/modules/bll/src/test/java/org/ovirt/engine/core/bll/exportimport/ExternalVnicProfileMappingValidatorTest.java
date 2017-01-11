package org.ovirt.engine.core.bll.exportimport;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class ExternalVnicProfileMappingValidatorTest {
    private static final Guid CLUSTER_ID = Guid.newGuid();
    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();

    @InjectMocks
    private ExternalVnicProfileMappingValidator underTest;

    @Mock
    private VnicProfileValidator mockVnicProfileValidator;

    @Before
    public void setUp() {
        when(mockVnicProfileValidator.validateTargetVnicProfileId(VNIC_PROFILE_ID, CLUSTER_ID))
                .thenReturn(ValidationResult.VALID);
    }

    @Test
    public void testValidateExternalVnicProfileMappingEmptyInputIsValid() {
        testListOfMappings(emptyList(), isValid());
    }

    @Test
    public void testValidateExternalVnicProfileMapping() {
        testListOfMappings(createMappings(VNIC_PROFILE_ID), isValid());
    }

    @Test
    public void testValidateExternalVnicProfileMappingInvalidVnicProfileId() {
        final Guid invalidId = Guid.newGuid();
        when(mockVnicProfileValidator.validateTargetVnicProfileId(invalidId, CLUSTER_ID))
                .thenReturn(new ValidationResult(EngineMessage.CAN_DO_ACTION_GENERAL_FAILURE));

        testListOfMappings(createMappings(VNIC_PROFILE_ID, invalidId),
                failsWith(EngineMessage.CAN_DO_ACTION_GENERAL_FAILURE));
    }

    @Test
    public void testValidateExternalVnicProfileMappingNullTargetProfileIsValid() {
        testListOfMappings(createMappings(VNIC_PROFILE_ID, null), isValid());
    }

    private List<ExternalVnicProfileMapping> createMappings(Guid... vnicProfileIds) {
        return stream(vnicProfileIds).map(this::createMapping).collect(toList());
    }

    private ExternalVnicProfileMapping createMapping(Guid vnicProfileId) {
        return new ExternalVnicProfileMapping("", "", vnicProfileId);
    }

    private void testListOfMappings(List<ExternalVnicProfileMapping> vnicProfileMappings, Matcher<ValidationResult> resultMatcher) {
        final ValidationResult actual =
                underTest.validateExternalVnicProfileMapping(vnicProfileMappings, CLUSTER_ID);

        assertThat(actual, resultMatcher);
    }

}
