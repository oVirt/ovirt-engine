package org.ovirt.engine.core.common.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidNFSMountPoint;

/**
 * Validates, that the given string is a linux mount point in the following form:
 * [IP or FQDN]:/[linux/path/only/from/ASCII]
 */
public class NfsMountPointConstraint implements ConstraintValidator<ValidNFSMountPoint, String> {

    private static final String FQDN =
            "(?=^.{1,254}$)(^(((?!-)[a-zA-Z0-9-]{1,63}(?<!-))|((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\\.)+[a-zA-Z0-9]{2,63}))";

    private static final String PATH = "\\:/((.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)/?)?";

    private static final String LINUX_MOUNT_POINT = String.format("(?:%s|%s|%s)%s",
            ValidationUtils.IPV4_PATTERN_NON_EMPTY,
            ValidationUtils.IPV6_FOR_URI,
            FQDN,
            PATH);
    private static final String ASCII = "[\\p{ASCII}]*";

    private static final Pattern LINUX_MOUNT_POINT_PATTERN = Pattern.compile(LINUX_MOUNT_POINT);
    private static final Pattern ASCII_PATTERN = Pattern.compile(ASCII);
    private static final Pattern NO_SPACE_PATTERN = Pattern.compile(ValidationUtils.NO_WHITESPACE);

    @Override
    public void initialize(ValidNFSMountPoint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return LINUX_MOUNT_POINT_PATTERN.matcher(name).matches()
                && ASCII_PATTERN.matcher(name).matches()
                && NO_SPACE_PATTERN.matcher(name).matches();
    }

}
