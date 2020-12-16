package org.ovirt.engine.core.sso.service;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LocalizationService {

    private static final Logger log = LoggerFactory.getLogger(LocalizationService.class);
    private String messageSource;
    private Locale defaultLocale;
    private ResourceBundle defaultResourceBundle;
    private Map<Locale, ResourceBundle> resourceBundlesByLocale;

    public LocalizationService(final String fileName) {
        log.info("Start initializing {}", getClass().getSimpleName());
        messageSource = fileName.replaceAll("\\.properties$", "");
        resourceBundlesByLocale = new ConcurrentHashMap<>();
        defaultLocale = Locale.getDefault();
        // Load default locale.
        defaultResourceBundle = loadResourceBundle(defaultLocale);
        // If default locale not found, load English US as default locale.
        if (defaultResourceBundle == null) {
            defaultLocale = Locale.US;
            defaultResourceBundle = loadResourceBundle(defaultLocale);
        }
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    public String localize(final String errorMsg, final Locale requestLocale) {
        String localizedErrorMsg = errorMsg;
        ResourceBundle resourceBundle = getResourceBundle(requestLocale == null ? defaultLocale : requestLocale);
        if (resourceBundle != null && resourceBundle.containsKey(errorMsg)) {
            localizedErrorMsg = resourceBundle.getString(errorMsg);
        }
        return localizedErrorMsg;
    }

    private synchronized ResourceBundle getResourceBundle(Locale locale) {
        ResourceBundle resourceBundle = resourceBundlesByLocale.get(locale);
        if (resourceBundle == null) {
            resourceBundle = loadResourceBundle(locale);
        }
        return resourceBundle == null ? defaultResourceBundle : resourceBundle;
    }

    private synchronized ResourceBundle loadResourceBundle(Locale locale) {
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle(messageSource, locale);
            resourceBundlesByLocale.put(locale, resourceBundle);
        } catch (RuntimeException e) {
            log.error("File: '{}' could not be loaded: {}", messageSource, e.getMessage());
            log.debug("Exception", e);
        }
        return resourceBundle;
    }

}
