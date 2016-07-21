/*
Copyright (c) 2015-2016 Red Hat, Inc.

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

package org.ovirt.engine.api.restapi.invocation;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.mode.ApplicationMode;

/**
 * This class stores information that its scoped to the request currently being processed.
 */
public class Current {
    /**
     * The requested version of the API.
     */
    private String version;

    /**
     * The place where the version was extracted from.
     */
    private VersionSource versionSource;

    /**
     * The reconstructed root of the request URI, including only the protocol, the host and the port number, for example
     * {@code https://engine.example.com}.
     */
    private String root;

    /**
     * The prefix of the path of the application, for example {@code /ovirt-engine/api}.
     */
    private String prefix;

    /**
     * The relative path of the current request, without the prefix of the application, for example
     * {@code /vms/123/disks}.
     */
    private String path;

    /**
     * The parameters of the current request. This is intended to comunicate parameters from one part to the application
     * to another without using directly the HTTP request.
     */
    private Map<String, String> parameters = new HashMap<>();

    /**
     * The identifier of the backend session.
     */
    private String sessionId;

    /**
     * This indicates the application mode for the current request.
     */
    private ApplicationMode applicationMode;

    /**
     * This is a reference to the user that is performing the request.
     */
    private DbUser user;

    /**
     * Reference to the backend EJB used for this request.
     */
    private BackendLocal backend;

    /**
     * Returns the request version of the API.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the place where the requested version of the API was extracted from.
     */
    public VersionSource getVersionSource() {
        return versionSource;
    }

    public void setVersionSource(VersionSource versionSource) {
        this.versionSource = versionSource;
    }

    /**
     * Returns the reconstructed root of the request URI, including only the protocol, the host and the port number, for
     * example {@code https://engine.example.com}.
     */
    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    /**
     * Returns the prefix of the path of the application, for example {@code /ovirt-engine/api}.
     */
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Return the relative path of the current request, without the prefix of the application, for example
     * {@code /vms/123/disks}.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns a reference to the map used to store parameters of this request. It can be modified, but it isn't
     * thread safe, so if you plan to call it from multiple threads makes sure to explicitly avoid concurrency.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ApplicationMode getApplicationMode() {
        return applicationMode;
    }

    public void setApplicationMode(ApplicationMode applicationMode) {
        this.applicationMode = applicationMode;
    }

    public DbUser getUser() {
        return user;
    }

    public void setUser(DbUser user) {
        this.user = user;
    }

    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    public BackendLocal getBackend() {
        return backend;
    }
}
