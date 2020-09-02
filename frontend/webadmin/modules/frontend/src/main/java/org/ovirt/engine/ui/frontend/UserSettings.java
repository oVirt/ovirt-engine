package org.ovirt.engine.ui.frontend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Effectively immutable.
 */
public class UserSettings {
    private static final Logger logger = Logger.getLogger(UserSettings.class.getName());

    static final int CURRENT_VERSION = 1;
    static final String LOCAL_STORAGE_PERSISTENCE = "localStoragePersistence"; //$NON-NLS-1$
    // Possible values:
    // 1 - support for hiding/swapping columns in grids
    static final String LOCAL_STORAGE_PERSISTENCE_VERSION = "localStoragePersistenceVersion"; //$NON-NLS-1$
    static final String LOCAL_STORAGE = "localStorage"; //$NON-NLS-1$
    static final String WEB_ADMIN = "webAdmin"; //$NON-NLS-1$
    private boolean localStoragePersistence;
    private Map<String, String> localStorage;
    private Integer localStoragePersistenceVersion;
    private Map<String, String> originalUserOptions;

    private UserSettings(boolean localStoragePersistence,
            Integer localStoragePersistenceVersion,
            Map<String, String> localStorage,
            Map<String, String> allEncodedUserOptions) {
        this.originalUserOptions = Objects.requireNonNull(allEncodedUserOptions);
        this.localStorage = Objects.requireNonNull(localStorage);
        this.localStoragePersistence = localStoragePersistence;
        this.localStoragePersistenceVersion = localStoragePersistenceVersion;
    }

    public static UserSettings from(DbUser loggedInUser) {
        if (loggedInUser == null) {
            return UserSettings.defaultSettings();
        }
        return Builder.create().fromUser(loggedInUser).build();
    }

    public static UserSettings defaultSettings() {
        return Builder.create()
                .withStorage(Collections.emptyMap())
                .withLocalStoragePersistence(true)
                .build();
    }

    /**
     * Copy of the local storage from user settings.
     */
    public Map<String, String> getLocalStoragePersistedOnServer() {
        return new HashMap<>(localStorage);
    }

    public boolean isLocalStoragePersistedOnServer() {
        return localStoragePersistence;
    }

    public Integer getLocalStoragePersistenceVersion() {
        return localStoragePersistenceVersion;
    }

    /**
     * Encode to map of (key, json string).
     */
    public Map<String, String> encode() {
        return NativeParser.encode(this);
    }

    /**
     * Copy of the settings retrieved from {@linkplain DbUser#getUserOptions()}
     */
    public Map<String, String> getOriginalUserOptions() {
        return new HashMap<>(originalUserOptions);
    }

    public static class Builder {

        private final Function<String, Parser> createParser;
        private Map<String, String> storage = Collections.emptyMap();
        private boolean localStoragePersistence;
        private Integer localStoragePersistenceVersion;
        private Map<String, String> originalUserOptions = Collections.emptyMap();

        public Builder(Function<String, Parser> createParser) {
            this.createParser = createParser;
        }

        public static Builder create() {
            return new Builder(NativeParser::from);
        }

        public Builder withStorage(Map<String, String> storage) {
            this.storage = storage;
            return this;
        }

        public Builder withLocalStoragePersistence(boolean flag) {
            this.localStoragePersistence = flag;
            return this;
        }

        public Builder fromUser(DbUser user) {
            Parser parser = createParser.apply(user.getUserOptions().get(WEB_ADMIN));
            storage = parser.parseStorage();
            localStoragePersistence = parseLocalStoragePersistence(parser.getLocalStoragePersistence());
            localStoragePersistenceVersion = parseVersion(parser.getVersion());
            originalUserOptions = user.getUserOptions();
            return this;
        }

        public Builder fromSettings(UserSettings settings) {
            this.storage = settings.getLocalStoragePersistedOnServer();
            this.localStoragePersistenceVersion = settings.getLocalStoragePersistenceVersion();
            this.localStoragePersistence = settings.isLocalStoragePersistedOnServer();
            originalUserOptions = settings.getOriginalUserOptions();
            return this;
        }

        public UserSettings build() {
            return new UserSettings(localStoragePersistence, localStoragePersistenceVersion, storage,
                    originalUserOptions);
        }

        public Builder withLocalStoragePersistenceVersion(Integer version) {
            this.localStoragePersistenceVersion = version;
            return this;
        }

        public Builder withCurrentVersion() {
            this.localStoragePersistenceVersion = CURRENT_VERSION;
            return this;
        }

        private static boolean parseLocalStoragePersistence(String encodedValue) {
            // defaults to true if no value
            // defaults to false if value is invalid
            return encodedValue == null || Boolean.parseBoolean(encodedValue);
        }

        private static Integer parseVersion(String encodedValue) {
            if (encodedValue == null) {
                return null;
            }
            try {
                return Integer.parseInt(encodedValue);
            } catch (NumberFormatException e) {
                logger.severe("Invalid local storage version: " + encodedValue); //$NON-NLS-1$
                // null would trigger the migration
                return CURRENT_VERSION;
            }
        }
    }

    /**
     * Extension point to separate native JSON parser.
     */
    interface Parser {
        Map<String, String> parseStorage();

        String getLocalStoragePersistence();

        String getVersion();
    }

    /**
     * Uses native JavaScript to parse provided JSON.
     */
    private static class NativeParser implements Parser {
        private static final String ANY_KEY = "any_key"; //$NON-NLS-1$
        private JSONObject json;

        private NativeParser(JSONObject json) {
            this.json = json;
        }

        public static Parser from(String jsonString) {
            return new NativeParser(toJSONObject(jsonString));
        }

        @Override
        public Map<String, String> parseStorage() {
            if (!json.containsKey(LOCAL_STORAGE) || json.get(LOCAL_STORAGE).isObject() == null) {
                return Collections.emptyMap();
            }

            JSONObject storage = json.get(LOCAL_STORAGE).isObject();
            Map<String, String> decoded = storage.keySet().stream()
                    .filter(key -> storage.get(key) != null)
                    .filter(key -> storage.get(key).isString() != null)
                    .collect(Collectors.toMap(
                            key -> key,
                            key -> storage.get(key).isString().stringValue()
                            )
                    );

            return decoded;
        }

        @Override
        public String getLocalStoragePersistence() {
            return json.containsKey(LOCAL_STORAGE_PERSISTENCE)
                    // check for JSONNull
                    && json.get(LOCAL_STORAGE_PERSISTENCE).isNull() == null ?
                    json.get(LOCAL_STORAGE_PERSISTENCE).toString() :
                    null;
        }

        @Override
        public String getVersion() {
            return json.containsKey(LOCAL_STORAGE_PERSISTENCE_VERSION)
                    // check for JSONNull
                    && json.get(LOCAL_STORAGE_PERSISTENCE_VERSION).isNull() == null ?
                    json.get(LOCAL_STORAGE_PERSISTENCE_VERSION).toString() :
                    null;
        }

        /**
         * Encode to map of (key, json string).
         */
        public static Map<String, String> encode(UserSettings settings) {
            JSONObject webAdminOptions = toJSONObject(settings.getOriginalUserOptions().get(WEB_ADMIN));

            JSONObject newStorage = new JSONObject();
            for (Map.Entry<String, String> entry : settings.getLocalStoragePersistedOnServer().entrySet()) {
                newStorage.put(entry.getKey(), new JSONString(entry.getValue()));
            }

            webAdminOptions.put(LOCAL_STORAGE, newStorage);
            webAdminOptions.put(LOCAL_STORAGE_PERSISTENCE, JSONBoolean.getInstance(settings.isLocalStoragePersistedOnServer()));
            webAdminOptions.put(LOCAL_STORAGE_PERSISTENCE_VERSION, settings.getLocalStoragePersistenceVersion() != null ?
                    new JSONNumber(settings.getLocalStoragePersistenceVersion()) :
                    JSONNull.getInstance());

            Map<String, String> newOptions = new HashMap<>(settings.getOriginalUserOptions());
            newOptions.put(WEB_ADMIN, JsonUtils.stringify(webAdminOptions.getJavaScriptObject()));
            return newOptions;
        }

        /**
         * Safe conversion.
         */
        private static JSONObject toJSONObject(String jsonString) {
            if (jsonString == null) {
                // actually safeEval seems to accept null but this violates the contract
                return new JSONObject();
            }
            // may contain invalid type i.e. may wrap null
            // which results in crash when get(key) is invoked
            // unfortunately isObject() will not work as expected
            JSONObject perhapsObject = new JSONObject(JsonUtils.safeEval(jsonString));

            // let's put it into the box - this triggers unwrapping to native JS type
            JSONObject box = new JSONObject();
            box.put(ANY_KEY, perhapsObject);

            // just take it from the the box
            // this time native JS type gets wrapped by a correct class
            // now isObject() will work as expected
            JSONValue value = box.get(ANY_KEY);
            return value.isObject() == null ? new JSONObject() : value.isObject();

        }
    }
}
