package org.ovirt.engine.core.utils;

import java.util.Locale;

import org.ovirt.engine.core.utils.servlet.LocaleFilter;

public class LocaleUtils {
    //Do not allow instantiation of this class.
    private LocaleUtils() {}

    /**
     * Returns the {@code Locale} based on the passed in string.
     * @param localeString The string to find the {@code Locale} in.
     * @return The {@code Locale} or null if the string doesn't represent
     * a proper {@code Locale}
     */
    public static Locale getLocaleFromString(String localeString) {
        return getLocaleFromString(localeString, false);
    }

    /**
     * Returns the {@code Locale} based on the passed in string. Returns the
     * default {@code Locale} if a locale cannot be found and the passed in
     * flag is set to true.
     * @param localeString The string to find the {@code Locale} in.
     * @param returnNull If {@code true} return Locale.US if a locale cannot be
     * determined from the localeString. if false return null if the locale
     * cannot be determined.
     * @return The {@code Locale} determined in the method.
     */
    public static Locale getLocaleFromString(String localeString, boolean returnDefaultLocale) {
        Locale result = returnDefaultLocale ? null : LocaleFilter.DEFAULT_LOCALE;
        try {
            if (localeString != null) {
                result = org.apache.commons.lang.LocaleUtils.toLocale(localeString.replaceAll("\\-", "_"));
            } else {
                result = null;
            }
            if(result == null && returnDefaultLocale) {
                result = LocaleFilter.DEFAULT_LOCALE;
            }
        } catch (IllegalArgumentException e) {
            result = returnDefaultLocale ? LocaleFilter.DEFAULT_LOCALE : null;
        }
        return result;
    }
}
