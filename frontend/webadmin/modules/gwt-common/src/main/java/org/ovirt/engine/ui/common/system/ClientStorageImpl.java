package org.ovirt.engine.ui.common.system;

import java.util.Date;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;
import com.google.inject.Inject;

/**
 * Default implementation of {@link ClientStorage} interface.
 * <p>
 * Applies an application-specific {@linkplain ClientStorageKeyPrefix prefix} to all key names.
 */
public class ClientStorageImpl implements ClientStorage {

    // Fifty years, in milliseconds
    private static final long PERSISTENT_COOKIE_EXPIRATION = 50L * 365L * 24L * 60L * 60L * 1000L;

    private static Storage localStorage;
    private static Storage sessionStorage;

    private final String keyPrefix;

    @Inject
    public ClientStorageImpl(@ClientStorageKeyPrefix String keyPrefix) {
        this.keyPrefix = keyPrefix;
        initStorage();
    }

    void initStorage() {
        localStorage = Storage.getLocalStorageIfSupported();
        sessionStorage = Storage.getSessionStorageIfSupported();
    }

    String getPrefixedKey(String key) {
        return keyPrefix + "_" + key; //$NON-NLS-1$
    }

    @Override
    public boolean isWebStorageAvailable() {
        return localStorage != null && sessionStorage != null;
    }

    @Override
    public String getLocalItem(String key) {
        String value = getLocalItemImpl(getPrefixedKey(key));
        // If missing, use un-prefixed key for backwards compatibility
        return (value != null) ? value : getLocalItemImpl(key);
    }

    String getLocalItemImpl(String key) {
        if (localStorage != null) {
            return localStorage.getItem(key);
        } else {
            return Cookies.getCookie(key);
        }
    }

    @Override
    public void setLocalItem(String key, String value) {
        setLocalItemImpl(getPrefixedKey(key), value);
    }

    void setLocalItemImpl(String key, String value) {
        if (localStorage != null) {
            localStorage.setItem(key, value);
        } else {
            // Emulate persistent storage using cookies which have predefined expiration date
            Cookies.setCookie(key, value, new Date(new Date().getTime() + PERSISTENT_COOKIE_EXPIRATION));
        }
    }

    @Override
    public String getSessionItem(String key) {
        String value = getSessionItemImpl(getPrefixedKey(key));
        // If missing, use un-prefixed key for backwards compatibility
        return (value != null) ? value : getSessionItemImpl(key);
    }

    String getSessionItemImpl(String key) {
        if (sessionStorage != null) {
            return sessionStorage.getItem(key);
        } else {
            return Cookies.getCookie(key);
        }
    }

    @Override
    public void setSessionItem(String key, String value) {
        setSessionItemImpl(getPrefixedKey(key), value);
    }

    void setSessionItemImpl(String key, String value) {
        if (sessionStorage != null) {
            sessionStorage.setItem(key, value);
        } else {
            // Emulate transient storage using cookies which expire when the browser session ends
            Cookies.setCookie(key, value);
        }
    }

}
