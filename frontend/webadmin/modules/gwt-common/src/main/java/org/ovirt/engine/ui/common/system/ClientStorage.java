package org.ovirt.engine.ui.common.system;

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
public interface ClientStorage {

    /**
     * Returns the value for the given key from local (persistent) storage, or {@code null} if there is no value for
     * such key.
     */
    String getLocalItem(String key);

    /**
     * Sets the value for the given key using local (persistent) storage.
     */
    void setLocalItem(String key, String value);

    /**
     * Removes the value associated with the given key from local (persistent) storage.
     */
    void removeLocalItem(String key);

    /**
     * Returns the value for the given key from session (transient) storage, or {@code null} if there is no value for
     * such key.
     */
    String getSessionItem(String key);

    /**
     * Sets the value for the given key using session (transient) storage.
     */
    void setSessionItem(String key, String value);

    /**
     * Removes the value associated with the given key from session (transient) storage.
     */
    void removeSessionItem(String key);

}
