package org.ovirt.engine.core.common.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VmPool;

public class ValidationUtils {

    public static final String NO_SPECIAL_CHARACTERS_I18N = "^[\\p{L}0-9._-]*$";
    public static final String NO_SPECIAL_CHARACTERS = "[0-9a-zA-Z_-]+";
    public static final String ONLY_I18N_ASCII_OR_NONE = "[\\p{ASCII}\\p{L}]*";
    public static final String ONLY_ASCII_OR_NONE = "[\\p{ASCII}]*";
    public static final String NO_SPECIAL_CHARACTERS_WITH_DOT = "[0-9a-zA-Z-_\\.]+";
    public static final String NO_TRIMMING_WHITE_SPACES_PATTERN = "^$|\\S.*\\S";
    public static final String IP_PATTERN =
            "^\\b((25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)\\b$|^$";

    public static final String ISO_SUFFIX = ".iso";
    public static final String ISO_SUFFIX_PATTERN = "^$|^.+\\.iso$";

    /** the mask will be replaced with zero-padded number in the generated names of the VMs in the pool,
     * see NameForVmInPoolGeneratorTest PoolNameValidationTest for valid and invalid expressions of this pattern */
    public static final String POOL_NAME_PATTERN = "^[\\p{L}0-9._-]+[" + VmPool.MASK_CHARACTER + "]*[\\p{L}0-9._-]*$|^[\\p{L}0-9._-]*[" + VmPool.MASK_CHARACTER + "]*[\\p{L}0-9._-]+$";

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    /***
     * This function validates a hostname according to URI RFC's.
     */
    public static boolean validHostname(String s) {
        if (s == null || s.trim().isEmpty()) {
            return false;
        }
        try {
            URI host = new URI("http://" + s);
            return s.equals(host.getHost());
        } catch (URISyntaxException use) {
            return false;
        }
    }

    public static boolean validUri(String addr) {
        try {
            new URI(addr);
            return true;
        } catch (URISyntaxException use) {
            return false;
        }
    }

    public static Validator getValidator() {
        return validator;

    }

    /**
     * @return A list of error message keys representing the violations, or empty list if no violations occurred.
     */
    public static <T> List<String> validateInputs(List<Class<?>> validationGroupList, T parameters) {

        List<String> messages = Collections.emptyList();
        Set<ConstraintViolation<T>> violations = ValidationUtils.getValidator().validate(parameters,
                validationGroupList.toArray(new Class<?>[validationGroupList.size()]));

        if (!violations.isEmpty()) {
            messages = new ArrayList<String>(violations.size());

            for (ConstraintViolation<T> constraintViolation : violations) {
                messages.add(constraintViolation.getMessage());
            }
        }
        return messages;
    }

    public static boolean validatePort(int port) {
        return ((port >= BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT) && (port <= BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT));
    }
}
