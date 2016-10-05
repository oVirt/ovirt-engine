package org.ovirt.engine.core.bll.exportimport;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.network.ExternalVnicProfileMapping;
import org.ovirt.engine.core.compat.Guid;

public class ExternalVnicProfileMappingValidator {

    private final VnicProfileValidator vnicProfileValidator;

    @Inject
    ExternalVnicProfileMappingValidator(VnicProfileValidator vnicProfileValidator) {
        this.vnicProfileValidator = Objects.requireNonNull(vnicProfileValidator);
    }

    public ValidationResult validateExternalVnicProfileMapping(
            Collection<ExternalVnicProfileMapping> externalVnicProfileMappings,
            Guid clusterId) {
        return externalVnicProfileMappings
                .stream()
                .map(ExternalVnicProfileMapping::getVnicProfileId)
                .filter(Objects::nonNull)
                .map(vnicProfileId -> vnicProfileValidator.validateTargetVnicProfileId(vnicProfileId, clusterId))
                .filter(((Predicate<ValidationResult>) ValidationResult::isValid).negate())
                .findFirst()
                .orElse(ValidationResult.VALID);
    }
}
