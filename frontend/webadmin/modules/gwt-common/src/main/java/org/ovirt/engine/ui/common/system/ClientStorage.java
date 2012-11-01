package org.ovirt.engine.ui.common.system;

import java.util.Date;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;

/**
 * Provides client-side key-value storage service.
 * <p>
 * Uses HTML5 {@linkplain Storage Web Storage} when supported by the browser. Falls back to {@linkplain Cookies cookies}
 * when HTML5 Web Storage is not available.
 * <p>
 * Some facts and limitations:
 * <ul>
 * <li>typically, there can be max. 20 cookies per domain, each holding max. 4kB of data
 * <li>HTML5 local storage is persistent and shared by all browser windows/tabs
 * <li>HTML5 session storage is transient and accessible only to one browser window/tab
 * </ul>
 */
public class ClientStorage {

    // Fifty years
    private static final long PERSISTENT_COOKIE_EXPIRATION = 1000 * 60 * 60 * 24 * 365 * 50;

    private static final Storage localStorage = Storage.getLocalStorageIfSupported();
    private static final Storage sessionStorage = Storage.getSessionStorageIfSupported();

    /**
     * Returns the value for the given key from local (persistent) storage, or {@code null} if there is no value for
     * such key.
     */
    public String getLocalItem(String key) {
        if (localStorage != null) {
            return localStorage.getItem(key);
        } else {
            return Cookies.getCookie(key);
        }
    }

    /**
     * Sets the value for the given key using local (persistent) storage.
     */
    public void setLocalItem(String key, String value) {
        if (localStorage != null) {
            localStorage.setItem(key, value);
        } else {
            // Emulate persistent storage using cookies which have predefined expiration date
            Cookies.setCookie(key, value, new Date(new Date().getTime() + PERSISTENT_COOKIE_EXPIRATION));
        }
    }

    /**
     * Removes the value associated with the given key from local (persistent) storage.
     */
    public void removeLocalItem(String key) {
        if (localStorage != null) {
            localStorage.removeItem(key);
        } else {
            Cookies.removeCookie(key);
        }
    }

    /**
     * Returns the value for the given key from session (transient) storage, or {@code null} if there is no value for
     * such key.
     */
    public String getSessionItem(String key) {
        if (sessionStorage != null) {
            return sessionStorage.getItem(key);
        } else {
            return Cookies.getCookie(key);
        }
    }

    /**
     * Sets the value for the given key using session (transient) storage.
     */
    public void setSessionItem(String key, String value) {
        if (sessionStorage != null) {
            sessionStorage.setItem(key, value);
        } else {
            // Emulate transient storage using cookies which expire when the browser session ends
            Cookies.setCookie(key, value);
        }
    }

    /**
     * Removes the value associated with the given key from session (transient) storage.
     */
    public void removeSessionItem(String key) {
        if (sessionStorage != null) {
            sessionStorage.removeItem(key);
        } else {
            Cookies.removeCookie(key);
        }
    }

}
