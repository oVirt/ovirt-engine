package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageBundler {

    private static final Logger log = LoggerFactory.getLogger(MessageBundler.class);
    private static final String RESOURCE_BUNDLE_NAME = "bundles/AuditLogMessages";
    private static final ResourceBundle resourceBundle = getResourceBundle();

    public static ResourceBundle getResourceBundle() {
        try {
            return ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Could not find ResourceBundle file '" + RESOURCE_BUNDLE_NAME + "'.");
        }
    }

    public static String getMessage(AuditLogType logType) {
        return StringUtils.defaultString(getMessageOrNull(logType));
    }

    public static String getMessageOrNull(AuditLogType logType) {
        final String key = logType.name();
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            log.error("Key '{}' is not translated in '{}'", key, RESOURCE_BUNDLE_NAME);
            return null;
        }
    }
}
