/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi;

import java.util.Collections;
import java.util.HashSet;
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
    private static final String DEFAULT_VERSION = "ENGINE_API_DEFAULT_VERSION";

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
        String value = config.getProperty(SUPPORTED_VERSIONS);
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
}
