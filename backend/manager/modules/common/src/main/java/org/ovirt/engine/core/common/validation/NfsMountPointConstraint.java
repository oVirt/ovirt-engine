package org.ovirt.engine.core.common.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.validation.annotation.ValidNFSMountPoint;

/**
 * Validates, that the given string is a linux mount point in the following form:
 * <IP or FQDN>:/<linux/path/only/from/ASCII>
 */
public class NfsMountPointConstraint implements ConstraintValidator<ValidNFSMountPoint, String> {

    private static final String IP =
            "((?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)|";

    private static final String FQDN = "(?=^.{1,254}$)(^(((?!-)[a-zA-Z0-9-]{1,63}(?<!-))|((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\\.)+[a-zA-Z0-9]{2,63})))";

    private static final String PATH = "\\:/(.*?/|.*?\\\\)?([^\\./|^\\.\\\\]+)(?:\\.([^\\\\]*)|)";

    private static final String LINUX_MOUNT_POINT = IP + FQDN + PATH;

    private static final String ASCII = "[\\p{ASCII}]*";

    @Override
    public void initialize(ValidNFSMountPoint constraintAnnotation) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return Pattern.matches(LINUX_MOUNT_POINT, name) && Pattern.matches(ASCII, name);
    }

}
