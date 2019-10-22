/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
     * Returns the path that corresponds to the given path segments, adding the prefix, the path of the current request
     * and the version (if required). For example, if the segments are {@code disks} and {@code 456}, the path of the
     * current request is {@code vms/123}, and version {@code 4} has been explicitly included in the URL, then it will
     * return {@code /ovirt-engine/api/v4/vms/123/disks/456}.
     *
     * @param segments the path segments to add to the current path
     * @return the complete path, including the path of the current request, the prefix and the version
     */
    public String getRelativePath(String... segments) {
        StringBuilder buffer = new StringBuilder();
        appendPrefix(buffer);
        appendPath(buffer);
        appendSegments(buffer, segments);
        return buffer.toString();
    }

    /**
     * Returns the path that corresponds to the given path segments, adding the prefix and the version (if required).
     * For example, if the path segments are {@code disks} and {@code 456}, and version {@code 4} has been explicitly
     * included in the URL, then it will return {@code /ovirt-engine/api/v4/disks/456}.
     *
     * @param segments the path segments to add to the prefix
     * @return the complete path, including the prefix and the version
     */
    public String getAbsolutePath(String... segments) {
        StringBuilder buffer = new StringBuilder();
        appendPrefix(buffer);
        appendSegments(buffer, segments);
        return buffer.toString();
    }

    private void appendPrefix(StringBuilder buffer) {
        appendSegment(buffer, prefix);
        if (version != null && versionSource == VersionSource.URL) {
            appendSegment(buffer, "v");
            buffer.append(version);
        }
    }

    private void appendPath(StringBuilder buffer) {
        appendSegment(buffer, path);
    }

    private void appendSegments(StringBuilder buffer, String... segments) {
        for (String segment : segments) {
            appendSegment(buffer, segment);
        }
    }

    private void appendSegment(StringBuilder buffer, String segment) {
        if (segment != null && !segment.isEmpty()) {
            if (segment.charAt(0) != '/') {
                int length = buffer.length();
                if (length > 0 && buffer.charAt(length - 1) != '/') {
                    buffer.append('/');
                }
            }
            buffer.append(segment);
        }
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
