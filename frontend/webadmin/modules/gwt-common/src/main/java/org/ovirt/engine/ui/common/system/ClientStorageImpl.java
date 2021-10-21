package org.ovirt.engine.ui.common.system;

import static org.ovirt.engine.ui.common.system.StorageKeyUtils.GRID_HIDDEN_COLUMN_WIDTH_PREFIX;
import static org.ovirt.engine.ui.common.system.StorageKeyUtils.GRID_SWAPPED_COLUMN_LIST_SUFFIX;
import static org.ovirt.engine.ui.common.system.StorageKeyUtils.GRID_VISIBLE;
import static org.ovirt.engine.ui.frontend.UserProfileManager.BaseConflictResolutionStrategy.OVERWRITE_REMOTE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.WebAdminSettings;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;

/**
 * Default implementation of {@link ClientStorage} interface.
 * <p>
 * Applies an application-specific {@linkplain #CLIENT_STORAGE_KEY_PREFIX prefix} to all key names.
 */
public class ClientStorageImpl implements ClientStorage {

    private static final Logger logger = Logger.getLogger(ClientStorageImpl.class.getName());

    static final String CLIENT_STORAGE_KEY_PREFIX = "ENGINE_WebAdmin_"; //$NON-NLS-1$
    private static final String[] REMOTE_SUPPORTED_PREFIXES = {
            GRID_HIDDEN_COLUMN_WIDTH_PREFIX,
            GRID_VISIBLE
    };

    private static final String[] REMOTE_SUPPORTED_SUFFIXES = { GRID_SWAPPED_COLUMN_LIST_SUFFIX };

    // Fifty years, in milliseconds
    private static final long PERSISTENT_COOKIE_EXPIRATION = 50L * 365L * 24L * 60L * 60L * 1000L;

    private static Storage localStorage;
    private static Storage sessionStorage;

    private final SyncTimer syncTimer = new SyncTimer();
    private long lastUploadStartTime = 0L;

    public ClientStorageImpl() {
        initStorage();
    }

    void initStorage() {
        localStorage = Storage.getLocalStorageIfSupported();
        sessionStorage = Storage.getSessionStorageIfSupported();
    }

    String getPrefixedKey(String key) {
        return CLIENT_STORAGE_KEY_PREFIX + key;
    }

    boolean isPrefixed(String key) {
        return key != null && key.startsWith(CLIENT_STORAGE_KEY_PREFIX);
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

    @Override
    public void removeLocalItem(String key) {
        removeLocalItemImpl(getPrefixedKey(key));
    }

    private void removeLocalItemImpl(String key) {
        if (localStorage != null) {
            localStorage.removeItem(key);
        } else {
            Cookies.removeCookie(key);
        }
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

    private boolean shouldSaveOnServer() {
        WebAdminSettings settings = Frontend.getInstance().getWebAdminSettings();
        return settings != null &&
                settings.isLocalStoragePersistedOnServer() &&
                settings.getLocalStoragePersistenceVersion() != null;
    }

    @Override
    public void setRemoteItem(String key, String value) {
        setLocalItemImpl(getPrefixedKey(key), value);
        if (shouldSaveOnServer()) {
            logger.finest("Remote save item:" + key + " | " + value); //$NON-NLS-1$ //$NON-NLS-2$
            syncTimer.trigger();
        }
    }

    @Override
    public void removeRemoteItem(String key) {
        removeLocalItem(key);
        if (shouldSaveOnServer()) {
            logger.finest("Remote remove item:" + key); //$NON-NLS-1$
            syncTimer.trigger();
        }
    }

    private class SyncTimer extends Timer {
        // do not trigger request immediately but wait for follow-up events
        // this allows to group subsequent updates and send them in one request
        private static final int OPTION_SYNC_DELAY = 2 * 1000;
        // safety valve if the lock was not released due to request failures
        private static final long MAX_LOCK_DURATION = 2L * 60L * 1000L;

        @Override
        public void run() {
            doUpload(true);
            logger.finest("Upload to the server started."); //$NON-NLS-1$
        }

        private void doUpload(boolean uploadChangedOnly) {
            long currentTime = new Date().getTime();
            if (currentTime - lastUploadStartTime < MAX_LOCK_DURATION) {
                logger.finest("Sync in progress. Delay next sync."); //$NON-NLS-1$
                trigger();
                return;
            } else if (lastUploadStartTime != 0L) {
                logger.warning("Sync lock has expired after[ms]: " + (currentTime - lastUploadStartTime)); //$NON-NLS-1$
            }

            WebAdminSettings currentSettings = Frontend.getInstance().getWebAdminSettings();
            if (!currentSettings.isLocalStoragePersistedOnServer()) {
                logger.info("Storage persistence disabled in the meantime.Skip upload."); //$NON-NLS-1$
                return;
            }

            Map<String, String> updated = getAllSupportedMappingsFromLocalStorage();
            if (uploadChangedOnly && currentSettings.getLocalStoragePersistedOnServer().equals(updated)) {
                logger.finest("Options are already up-to-date.Skip upload."); //$NON-NLS-1$
                return;
            }

            // (currently) we cannot propagate the changes in the local storage back to UI(grid layout)
            // The flow is one-way only: UI (user hides a column) -> local storage(side effect)
            // -> upload to the server (side effect). Settings are loaded from local storage only at the start (first
            // display). There is no reverse flow so changing content of local storage has no effect on the UI (if the
            // grid was already displayed).
            WebAdminSettings nextSettings = WebAdminSettings.Builder.create()
                    .fromSettings(currentSettings)
                    .withStorage(updated)
                    .build();

            lastUploadStartTime = new Date().getTime();
            Frontend.getInstance()
                    .getUserProfileManager()
                    .uploadUserProfileProperty(
                            nextSettings.encode(),
                            property -> {
                                lastUploadStartTime = 0L;
                                logger.fine("Uploaded property: " + property); //$NON-NLS-1$
                                },
                            result -> {
                                lastUploadStartTime = 0L;
                                logger.warning("Failed to upload WebAdmin settings due to:" + result);  //$NON-NLS-1$
                            },
                            (remote, local) -> OVERWRITE_REMOTE,
                            null,
                            false);
        }

        public void trigger() {
            this.cancel();
            this.schedule(OPTION_SYNC_DELAY);
        }

    }

    @Override
    public void storeAllUserSettingsInLocalStorage(WebAdminSettings webAdminSettings) {
        if (webAdminSettings == null) {
            logger.warning("Null settings. Nothing to store."); //$NON-NLS-1$
            return;
        }

        if (!webAdminSettings.isLocalStoragePersistedOnServer()) {
            logger.info("Uploading settings from local storage to the server is disabled."); //$NON-NLS-1$
            return;
        }

        if (webAdminSettings.getLocalStoragePersistenceVersion() == null
                || webAdminSettings.getOriginalUserOptions() == null) {
            // version defaults to current version if no option, otherwise should be part of JSON content
            // missing version indicates unexpected JSON content i.e. {}, [], null, etc
            logger.severe("Invalid state. Cancel populating local storage."); //$NON-NLS-1$
            return;
        }

        if (Guid.Empty.equals(webAdminSettings.getOriginalUserOptions().getPropertyId())) {
            logger.warning("No webAdmin property on the server. Trigger standard upload to create the property."); //$NON-NLS-1$
            syncTimer.doUpload(false);
            return;
        }

        for (Map.Entry<String, String> entry : webAdminSettings.getLocalStoragePersistedOnServer().entrySet()) {
            setLocalItem(entry.getKey(), entry.getValue());
        }

        // remove local keys not present on the server
        getAllKeysWithoutPrefix().stream()
                .filter(this::isSupported)
                .filter(key -> !webAdminSettings.getLocalStoragePersistedOnServer().containsKey(key))
                .forEach(this::removeLocalItem);

        logger.finest("Finished initial sync server -> local"); //$NON-NLS-1$
    }

    public Map<String, String> getAllSupportedMappingsFromLocalStorage() {
        return getAllKeysWithoutPrefix().stream()
                .filter(this::isSupported)
                .collect(
                        Collectors.toMap(
                                key -> key,
                                this::getLocalItem));
    }

    private boolean isSupported(String key) {
        for (String prefix : REMOTE_SUPPORTED_PREFIXES) {
            if (key.startsWith(prefix)) {
                return true;
            }
        }

        for (String suffix : REMOTE_SUPPORTED_SUFFIXES) {
            if (key.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private Collection<String> getAllKeysWithoutPrefix() {
        Collection<String> allKeys = localStorage == null ? Cookies.getCookieNames() : getAllKeysFromLocalStorage();

        return allKeys.stream()
                .filter(this::isPrefixed)
                .map(this::removePrefix)
                .collect(Collectors.toList());
    }

    private Collection<String> getAllKeysFromLocalStorage() {
        if (localStorage == null) {
            return Collections.emptyList();
        }

        ArrayList<String> allKeys = new ArrayList<>();
        for (int index = 0; index < localStorage.getLength(); index++) {
            allKeys.add(localStorage.key(index));
        }
        return allKeys;
    }

    private String removePrefix(String prefixedKey) {
        return prefixedKey.substring(CLIENT_STORAGE_KEY_PREFIX.length());
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
