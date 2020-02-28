package org.ovirt.engine.core.common.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.errors.EngineMessage;

public class ValidationUtils {

    public static final String NO_SPECIAL_CHARACTERS_EXTRA_I18N = "^[\\p{L}0-9._\\+-]*$";
    public static final String CUSTOM_CPU_NAME = "^[\\p{L}0-9._\\+\\-,]*$";
    public static final String NO_SPECIAL_CHARACTERS_I18N = "^[\\p{L}0-9._-]*$";
    public static final String NO_SPECIAL_CHARACTERS = "[0-9a-zA-Z_-]+";
    public static final String ONLY_I18N_ASCII_OR_NONE = "[\\p{ASCII}\\p{L}]*";
    public static final String ONLY_ASCII_OR_NONE = "[\\p{ASCII}]*";
    public static final String NO_SPECIAL_CHARACTERS_WITH_DOT = "[0-9a-zA-Z-_\\.]+";
    public static final String NO_TRIMMING_WHITE_SPACES_PATTERN = "^$|\\S.*\\S|^\\S+$";
    public static final String IPV4_PATTERN_NON_EMPTY =
            "\\b((25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)";
    // IPv4 pattern should not match the empty string because 'no entry' is represented in engine with null
    public static final String IPV4_PATTERN = "^" + IPV4_PATTERN_NON_EMPTY;
    private static final String IPV6_ADDRESS_BLOCK = "[0-9a-fA-F]{1,4}";
    private static final String IPV6_HEX_COMPRESSED_PATTERN =
            "((?:" + IPV6_ADDRESS_BLOCK + "(?::" + IPV6_ADDRESS_BLOCK + ")*)?)::((?:" +
                    IPV6_ADDRESS_BLOCK + "(?::" + IPV6_ADDRESS_BLOCK + ")*)?)";
    private static final String IPV6_STD_PATTERN = "(?:" + IPV6_ADDRESS_BLOCK + ":){7}" + IPV6_ADDRESS_BLOCK;
    public static final String IPV6_PATTERN = "(?:" + IPV6_STD_PATTERN + "|" + IPV6_HEX_COMPRESSED_PATTERN + ")";
    public static final String IPV4_OR_IPV6_PATTERN = IPV4_PATTERN + "|" + IPV6_PATTERN;
    public static final String IPV6_FOR_URI = "\\[" + IPV6_PATTERN + "\\]";
    public static final String ANY_IP_PATTERN = "^(" + IPV4_PATTERN_NON_EMPTY + "|" + IPV6_PATTERN +")$";
    public static final String FQDN_PATTERN =
            "([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*";
    public static final String HOSTNAME_FOR_URI =
            "(?:" + FQDN_PATTERN + "|" + IPV4_PATTERN + "|" + IPV6_FOR_URI + ")";
    public static final String IPV4_SUBNET_PREFIX_PATTERN = "(?:3[0-2]|[12]?[0-9])";
    public static final String IPV6_SUBNET_PREFIX_PATTERN = "(?:1[0-2][0-8]|[0-9]?[0-9])";
    public static final String IPV4_CIDR_FORMAT_PATTERN =
            "^" + IPV4_PATTERN_NON_EMPTY + "/" + IPV4_SUBNET_PREFIX_PATTERN + "$";
    public static final String IPV6_CIDR_FORMAT_PATTERN = "^" + IPV6_PATTERN + "/" + IPV6_SUBNET_PREFIX_PATTERN + "$";
    public static final String ISO_SUFFIX = ".iso";
    public static final String ISO_SUFFIX_PATTERN = "^$|^.+\\.iso$";
    public static final String BASE_64_PATTERN =
            "^([A-Za-z0-9+/]{4})*(()|[A-Za-z0-9+/][AQgw]==|[A-Za-z0-9+/]{2}[AEIMQUYcgkosw048]=)$";
    public static final String KEY_EQUALS_VALUE_SPACE_SEPARATED = "^[^\\s=]+=[^\\s=]+(\\s+[^\\s=]+=[^\\s=]+)*$";
    public static final String EMPTY_STRING = "^$";
    public static final String NO_WHITESPACE = "[^\\s]+";
    public static final String GUID = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";
    public static final String HOST_NIC_NAME_PATTERN =
            "^[0-9a-zA-Z_-]{1," + BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH + "}$";



    /**
     * the mask will be replaced with zero-padded number in the generated names of the VMs in the pool, see
     * NameForVmInPoolGeneratorTest PoolNameValidationTest for valid and invalid expressions of this pattern
     */
    public static final String POOL_NAME_PATTERN = "^[\\p{L}0-9._-]+[" + VmPool.MASK_CHARACTER
            + "]*[\\p{L}0-9._-]*$|^[\\p{L}0-9._-]*[" + VmPool.MASK_CHARACTER + "]*[\\p{L}0-9._-]+$";

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    public static final Pattern ipv6RegexPattern = Pattern.compile(IPV6_PATTERN);
    public static final Pattern ipv4RegexPattern = Pattern.compile(IPV4_PATTERN);

    /***
     * This function validates a hostname according to URI RFC's.
     */
    public static boolean validHostname(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        return isValidIpAddressOrHostname(address) || isValidIpv6Address(address);
    }

    private static boolean isValidIpAddressOrHostname(String address) {
        try {
            URI uri = new URI("http://" + address);
            return address.equals(uri.getHost());
        } catch (URISyntaxException use) {
            return false;
        }
    }

    private static boolean isValidIpv6Address(String address) {
        final String quotedIpv6 = "[" + address + "]";
        return isValidIpAddressOrHostname(quotedIpv6);
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
            messages = new ArrayList<>(violations.size());

            for (ConstraintViolation<T> constraintViolation : violations) {
                messages.add(constraintViolation.getMessage());
                ConstraintDescriptor<?> constraintDescriptor = constraintViolation.getConstraintDescriptor();
                //this will extract all violation attributes and will create messages of the type:
                //${violationkey} violationValue
                //these values can later be used for formatting the returned messages.
                if(constraintDescriptor != null) {
                    Map<String, Object> violationAttributes = constraintDescriptor.getAttributes();
                    for (Map.Entry violationAttribute : violationAttributes.entrySet()) {
                        String propertyName = violationAttribute.getKey().toString();
                        Object value = violationAttribute.getValue();

                        messages.add(createSetVariableString(propertyName, value));
                    }
                }
                if (constraintViolation.getPropertyPath() != null) {
                    messages.add(EngineMessage.ACTION_TYPE_FAILED_ATTRIBUTE_PATH.name());
                    messages.add(createSetVariableString("path", constraintViolation.getPropertyPath()));
                }

                messages.add(createSetVariableString("validatedValue", constraintViolation.getInvalidValue()));
            }
        }
        return messages;
    }

    private static String createSetVariableString(String propertyName, Object value) {
        final String setVariableValueFormat = "$%s %s";
        return String.format(setVariableValueFormat, propertyName, value);
    }

    public static boolean validatePort(int port) {
        return (port >= BusinessEntitiesDefinitions.NETWORK_MIN_LEGAL_PORT) && (port <= BusinessEntitiesDefinitions.NETWORK_MAX_LEGAL_PORT);
    }

    /**
     *
     * @param ipv4Address a ipv4 address with prefix ('/24') is invalid
     * @return true if the address matches the regex
     */
    public static boolean isValidIpv4(String ipv4Address) {
        return ipv4Address == null ? false : ipv4RegexPattern.matcher(ipv4Address).matches();
    }

    /**
     *
     * @param ipv6Address an address with prefix ('/64') or link local zone index ('%eth0')
     *                    is invalid
     * @return true if the address matches the regex
     */
    public static boolean isValidIpv6(String ipv6Address) {
        return ipv6Address == null ? false : ipv6RegexPattern.matcher(ipv6Address).matches();
    }
}
