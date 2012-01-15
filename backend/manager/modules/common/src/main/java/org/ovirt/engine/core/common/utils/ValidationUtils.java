package org.ovirt.engine.core.common.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import javax.validation.Validation;
import javax.validation.Validator;

public class ValidationUtils {

    public static final String NO_SPECIAL_CHARACTERS = "[0-9a-zA-Z_-]+";
    public static final String ONLY_ASCII_OR_NONE = "[\\p{ASCII}]*";
    public static final String NO_SPECIAL_CHARACTERS_OR_DASH = "[0-9a-zA-Z_]+";
    protected static final Pattern VDS_NAME_PATTERN = Pattern.compile("[0-9a-zA-Z-_\\.]+");
    public static final String DOMAIN_NAME_PATTERN =
            "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}$";
    public static final String NO_WHITES_SPACE_PATTERN = "\\S+";
    public static final String IP_PATTERN =
            "^\\b((25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]\\d\\d|\\d?\\d)\\b$";
    // NULLABLE_MAC_ADDRESS can be valid mac address: xx:xx:xx:xx:xx:xx or empty string,
    // We need it for VMs that is not sending custom MAC address and we provide MAC address from
    // the MAC pool - this values came as empty string and we don't want the validation fail.
    public static final String NULLABLE_MAC_ADDRESS = "^((\\d|([a-f]|[A-F])){2}:){5}(\\d|([a-f]|[A-F])){2}$|^$";
    // Invalid mac address (for now just checking 00:00:00:00:00:00
    public static final String INVALID_NULLABLE_MAC_ADDRESS = "^(00:){5}00$";

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static boolean isVdsNameLegal(String vdsName) {
        return VDS_NAME_PATTERN.matcher(vdsName).matches();
    }

    public static boolean containsIlegalCharacters(String s) {

        return !Pattern.matches(NO_SPECIAL_CHARACTERS, s);

    }

    public static boolean containsIlegalCharactersOrDash(String s) {
        return !Pattern.matches(NO_SPECIAL_CHARACTERS_OR_DASH, s);

    }

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

    public static Validator getValidator() {
        return validator;

    }
}
