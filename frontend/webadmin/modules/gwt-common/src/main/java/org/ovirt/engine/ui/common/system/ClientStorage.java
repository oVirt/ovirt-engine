package org.ovirt.engine.ui.common.system;

import org.ovirt.engine.ui.frontend.WebAdminSettings;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;

/**
 * Provides client-side key-value storage service.
 * <p>
 * Uses HTML5 {@linkplain com.google.gwt.storage.client.Storage Web Storage} when supported by the browser.
 * Falls back to {@linkplain com.google.gwt.user.client.Cookies cookies} when the Web Storage is not available.
 * <p>
 * Some facts and limitations:
 * <ul>
 * <li>typically, there can be max. 20 cookies per domain, each holding max. 4kB of data
 * <li>HTML5 <b>local storage</b> is persistent and shared by all browser windows/tabs
 * <li>HTML5 <b>session storage</b> is transient and accessible only to one browser window/tab
 * </ul>
 */
public interface ClientStorage extends LocalStorage {

    /**
     * Returns {@code true} if the browser supports HTML5 Web Storage
     * (both local storage and session storage APIs).
     */
    boolean isWebStorageAvailable();

    /**
     * Returns the value for the given key from local (persistent) storage,
     * or {@code null} if there is no value for such key.
     */
    String getLocalItem(String key);

    /**
     * Sets the value for the given key using local (persistent) storage.
     */
    void setLocalItem(String key, String value);

    /**
     * Set the value for the given key using both remote and local (persistent) storage.
     * If remote storage is disabled behaves the same as {@linkplain #setLocalItem(String, String)}
     * Note that remote persistance is best-effort only.
     */
    void setRemoteItem(String key, String value);


    /**
     * Remove the value for the given key from both remote and local (persistent) storage.
     * If remote storage is disabled behaves the same as {@linkplain #removeLocalItem(String)}
     * Note that remote persistance is best-effort only.
     */
    void removeRemoteItem(String key);

    /**
     * Remove the value for the given key from the local (persistent) storage.
     * @param key The key of the value to remove.
     */
    void removeLocalItem(String key);

    /**
     * Applies all supported keys from remote storage to local storage.
     * Existing keys are overridden. Unsupported keys are ignored.
     */
    void storeAllUserSettingsInLocalStorage(WebAdminSettings webAdminSettings);

    /**
     * Returns the value for the given key from session (transient) storage,
     * or {@code null} if there is no value for such key.
     */
    String getSessionItem(String key);

    /**
     * Sets the value for the given key using session (transient) storage.
     */
    void setSessionItem(String key, String value);

}
