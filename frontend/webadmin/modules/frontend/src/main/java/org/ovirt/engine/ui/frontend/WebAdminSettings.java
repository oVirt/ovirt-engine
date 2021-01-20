package org.ovirt.engine.ui.frontend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;

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
public class WebAdminSettings {
    private static final Logger logger = Logger.getLogger(WebAdminSettings.class.getName());

    static final int CURRENT_VERSION = 1;
    static final String LOCAL_STORAGE_PERSISTENCE = "localStoragePersistence"; //$NON-NLS-1$
    // Possible values:
    // 1 - support for hiding/swapping columns in grids
    static final String LOCAL_STORAGE_PERSISTENCE_VERSION = "localStoragePersistenceVersion"; //$NON-NLS-1$
    static final String LOCAL_STORAGE = "localStorage"; //$NON-NLS-1$
    public static final String WEB_ADMIN = "webAdmin"; //$NON-NLS-1$
    private final boolean localStoragePersistence;
    private final Map<String, String> localStorage;
    private final Integer localStoragePersistenceVersion;
    private final UserProfileProperty originalUserOptions;

    private WebAdminSettings(boolean localStoragePersistence,
            Integer localStoragePersistenceVersion,
            Map<String, String> localStorage,
            UserProfileProperty originalUserOptions) {
        this.originalUserOptions = Objects.requireNonNull(originalUserOptions);
        this.localStorage = Objects.requireNonNull(localStorage);
        this.localStoragePersistence = localStoragePersistence;
        this.localStoragePersistenceVersion = localStoragePersistenceVersion;
    }

    public static WebAdminSettings from(UserProfileProperty webAdminUserOption) {
        if (webAdminUserOption == null) {
            return WebAdminSettings.defaultSettings();
        }
        return Builder.create().fromProperty(webAdminUserOption).build();
    }

    public static WebAdminSettings defaultSettings() {
        return Builder.create().withDefaults().build();
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
    public UserProfileProperty encode() {
        return NativeParser.encode(this);
    }

    public UserProfileProperty getOriginalUserOptions() {
        return originalUserOptions;
    }

    public static class Builder {

        private final Function<String, Parser> createParser;
        private Map<String, String> storage = Collections.emptyMap();
        private boolean localStoragePersistence;
        private Integer localStoragePersistenceVersion;
        private UserProfileProperty originalUserOptions = UserProfileProperty.builder()
                .withTypeJson()
                .withName(WEB_ADMIN)
                .withContent("{}") //$NON-NLS-1$
                .build();

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

        public Builder fromProperty(UserProfileProperty webAdminProp) {
            if (webAdminProp == null ||
                    !WEB_ADMIN.equals(webAdminProp.getName()) ||
                    !webAdminProp.isJsonProperty()) {
                return withDefaults();
            }
            Parser parser = createParser.apply(webAdminProp.getContent());
            storage = parser.parseStorage();
            localStoragePersistence = parseLocalStoragePersistence(parser.getLocalStoragePersistence());
            localStoragePersistenceVersion = parseVersion(parser.getVersion());
            originalUserOptions = webAdminProp;
            return this;
        }

        public Builder fromSettings(WebAdminSettings settings) {
            this.storage = settings.getLocalStoragePersistedOnServer();
            this.localStoragePersistenceVersion = settings.getLocalStoragePersistenceVersion();
            this.localStoragePersistence = settings.isLocalStoragePersistedOnServer();
            originalUserOptions = settings.getOriginalUserOptions();
            return this;
        }

        public WebAdminSettings build() {
            return new WebAdminSettings(localStoragePersistence, localStoragePersistenceVersion, storage,
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

        public Builder withDefaults() {
            return withStorage(Collections.emptyMap())
                    .withLocalStoragePersistence(true)
                    .withCurrentVersion();
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
        public static UserProfileProperty encode(WebAdminSettings settings) {
            // use original content as base and overwrite known properties
            // any unknown properties will be preserved
            JSONObject webAdminOptions = toJSONObject(settings.getOriginalUserOptions().getContent());

            JSONObject newStorage = new JSONObject();
            for (Map.Entry<String, String> entry : settings.getLocalStoragePersistedOnServer().entrySet()) {
                newStorage.put(entry.getKey(), new JSONString(entry.getValue()));
            }

            webAdminOptions.put(LOCAL_STORAGE, newStorage);
            webAdminOptions.put(LOCAL_STORAGE_PERSISTENCE,
                    JSONBoolean.getInstance(settings.isLocalStoragePersistedOnServer()));
            webAdminOptions.put(LOCAL_STORAGE_PERSISTENCE_VERSION,
                    settings.getLocalStoragePersistenceVersion() != null ?
                            new JSONNumber(settings.getLocalStoragePersistenceVersion()) :
                            JSONNull.getInstance());

            return UserProfileProperty.builder()
                    .from(settings.getOriginalUserOptions())
                    .withContent(JsonUtils.stringify(webAdminOptions.getJavaScriptObject()))
                    .build();
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
