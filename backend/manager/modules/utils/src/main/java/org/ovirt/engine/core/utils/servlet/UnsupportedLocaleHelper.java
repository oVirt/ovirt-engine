package org.ovirt.engine.core.utils.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnsupportedLocaleHelper {
    private static final Logger log = LoggerFactory.getLogger(UnsupportedLocaleHelper.class);

    public static List<String> getDisplayedLocales(List<String> allLocales) {
        return getDisplayedLocales(allLocales, getLocalesKeys(ConfigValues.UnsupportedLocalesFilterOverrides),
                getLocalesKeys(ConfigValues.UnsupportedLocalesFilter));
    }
    /**
    * Determine the list of locales to display by subtracting unsupported locales from the list of all locales,
    * but then adding back in locales we want to display anyway (locale overrides).
    *
    * Elements in the lists should be of format xx_YY, which is the standard Java Locale format.
    * @param allLocales List of all available locales. Cannot be null.
    * @param unsupportedLocalesFilterOverrides The list of unsupported locale overrides
    * (display them even though they're unsupported). Cannot be null
    * @param unsupportedLocalesFilter The list of unsupported locales. Cannot be null
    * @return The {@code List} of locales to display
    */
    static List<String> getDisplayedLocales(List<String> allLocales,
            List<String> unsupportedLocalesFilterOverrides,
            List<String> unsupportedLocalesFilter) {
        List<String> result = new ArrayList<>(allLocales);
        //Override unsupported locales that we do want to display.
        List<String> unsupportedLocalesTemp = new ArrayList<>(unsupportedLocalesFilter);
        unsupportedLocalesTemp.removeAll(unsupportedLocalesFilterOverrides);
        //Remove remaining unsupported locales from the result.
        result.removeAll(unsupportedLocalesTemp);
        Collections.sort(result);
        return result;
    }

    /**
     * Get the locale keys from the configuration based on the {@code ConfigValues} passed in. Has to be either
     * ConfigValues.UnsupportedLocalesFilter or ConfigValues.UnsupportedLocalesFilterOverrides. Will throw an
     * {@code IllegalArgumentException} otherwise.
     * @param configValues The key to use to look up the values.
     * @return The value as a {@code List} of {@code Strings}.
     */
    public static List<String> getLocalesKeys(ConfigValues configValues) {
        if (!configValues.equals(ConfigValues.UnsupportedLocalesFilter)
                && !configValues.equals(ConfigValues.UnsupportedLocalesFilterOverrides)) {
            throw new IllegalArgumentException("Passed in config value not related to locales"); //$NON-NLS-1$
        }
        List<String> locales = Config.getValue(configValues);
        List<String> result = new ArrayList<>();
        if (locales != null && !locales.isEmpty()) {
            for (String localeKey: locales) {
                if (!StringUtils.isBlank(localeKey)) {
                    try {
                        //Check for valid locale.
                        String underScoredLocaleKey = localeKey.replaceAll("-", "_");
                        LocaleUtils.toLocale(underScoredLocaleKey);
                        result.add(underScoredLocaleKey);
                    } catch (IllegalArgumentException iae) {
                        //The locale passed in is not valid, don't add it to the list.
                        log.info("Invalid locale found in configuration '{}'", localeKey); //$NON-NLS-1$
                    }
                }
            }
        }
        return result;
    }
}
