package org.ovirt.engine.ui.common.system;

import java.util.Date;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;

/**
 * Default implementation of {@link ClientStorage} interface.
 */
public class ClientStorageImpl implements ClientStorage {

    // Fifty years, in milliseconds
    private static final long PERSISTENT_COOKIE_EXPIRATION = 50L * 365L * 24L * 60L * 60L * 1000L;

    private static final Storage localStorage = Storage.getLocalStorageIfSupported();
    private static final Storage sessionStorage = Storage.getSessionStorageIfSupported();

    @Override
    public boolean isWebStorageAvailable() {
        return localStorage != null && sessionStorage != null;
    }

    public String getLocalItem(String key) {
        if (localStorage != null) {
            return localStorage.getItem(key);
        } else {
            return Cookies.getCookie(key);
        }
    }

    public void setLocalItem(String key, String value) {
        if (localStorage != null) {
            localStorage.setItem(key, value);
        } else {
            // Emulate persistent storage using cookies which have predefined expiration date
            Cookies.setCookie(key, value, new Date(new Date().getTime() + PERSISTENT_COOKIE_EXPIRATION));
        }
    }

    public void removeLocalItem(String key) {
        if (localStorage != null) {
            localStorage.removeItem(key);
        } else {
            Cookies.removeCookie(key);
        }
    }

    public String getSessionItem(String key) {
        if (sessionStorage != null) {
            return sessionStorage.getItem(key);
        } else {
            return Cookies.getCookie(key);
        }
    }

    public void setSessionItem(String key, String value) {
        if (sessionStorage != null) {
            sessionStorage.setItem(key, value);
        } else {
            // Emulate transient storage using cookies which expire when the browser session ends
            Cookies.setCookie(key, value);
        }
    }

    public void removeSessionItem(String key) {
        if (sessionStorage != null) {
            sessionStorage.removeItem(key);
        } else {
            Cookies.removeCookie(key);
        }
    }

}
