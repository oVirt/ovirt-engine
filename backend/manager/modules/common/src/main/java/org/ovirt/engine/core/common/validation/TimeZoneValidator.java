package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.validation.annotation.ValidTimeZone;

public class TimeZoneValidator implements ConstraintValidator<ValidTimeZone, VmBase> {
    private final OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    @Override
    public void initialize(ValidTimeZone constraintAnnotation) {
    }

    @Override
    public boolean isValid(VmBase value, ConstraintValidatorContext context) {
        if (value.getTimeZone() == null) {
            return true;
        }
        TimeZoneType timeZoneType = osRepository.isWindows(value.getOsId()) ? TimeZoneType.WINDOWS_TIMEZONE : TimeZoneType.GENERAL_TIMEZONE;
        return timeZoneType.getTimeZoneList().containsKey(value.getTimeZone());
    }
}
