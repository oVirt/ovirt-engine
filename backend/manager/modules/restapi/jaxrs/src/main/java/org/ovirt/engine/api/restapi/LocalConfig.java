/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi;

import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.utils.EngineLocalConfig;

/**
 * This class stores the configuration of the API, loaded from the file specified by the {@code ENGINE_VARS} environment
 * variable, usually {@code /etc/ovirt-engine/engine.conf.d/*.conf}.
 */
public class LocalConfig {
    // This is a singleton, and this is the instance:
    private static volatile LocalConfig instance;

    // The names of the properties:
    private static final String SUPPORTED_VERSIONS = "ENGINE_API_SUPPORTED_VERSIONS";
    private static final String DEPRECATED_VERSIONS = "ENGINE_API_DEPRECATED_VERSIONS";
    private static final String DEFAULT_VERSION = "ENGINE_API_DEFAULT_VERSION";
    private static final String FILTER_BY_DEFAULT = "ENGINE_API_FILTER_BY_DEFAULT";
    private static final String EXPLORER_DIRECTORY = "ENGINE_API_EXPLORER_DIRECTORY";

    // Reference to the engine local configuration, as that is what is used to actually read the configuration:
    private EngineLocalConfig config;

    /**
     * Returns a reference to the singleton, initializing it if needed.
     */
    public static LocalConfig getInstance() {
        if (instance == null) {
            synchronized(LocalConfig.class) {
                if (instance == null) {
                    LocalConfig tmp = new LocalConfig();
                    tmp.config = EngineLocalConfig.getInstance();
                    instance = tmp;
                }
            }
        }
        return instance;
    }

    /**
     * Returns the set of supported versions of the API.
     */
    public Set<String> getSupportedVersions() {
        return getVersions(SUPPORTED_VERSIONS);
    }

    /**
     * Returns the set of deprecated versions of the API.
     */
    public Set<DeprecatedVersionInfo> getDeprecatedVersions() {
        return getVersions(DEPRECATED_VERSIONS).stream()
            .map(DeprecatedVersionInfo::parse)
            .filter(Objects::nonNull)
            .collect(toSet());
    }

    private Set<String> getVersions(String key) {
        String value = config.getProperty(key);
        if (value == null || value.isEmpty()) {
            return Collections.emptySet();
        }
        String[] chunks = value.split("\\s*,\\s*");
        Set<String> versions = new HashSet<>();
        Collections.addAll(versions, chunks);
        return versions;
    }

    /**
     * Returns the default version of the API, the one that should be used when the caller doesn't explicitly request
     * a specific version.
     */
    public String getDefaultVersion() {
        return config.getProperty(DEFAULT_VERSION);
    }

    /**
     * Returns a boolean that indicates if the <i>filter</i> mechanism should be enabled by default for users that
     * aren't administrators.
     */
    public boolean getFilterByDefault() {
        return config.getBoolean(FILTER_BY_DEFAULT);
    }

    /**
     * Returns the absolute path name of the directory that contains the API explorer application.
     */
    public String getExplorerDirectory() {
        return config.getProperty(EXPLORER_DIRECTORY);
    }
}
