/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.invocation;

/**
 * This enumerated type is used to indicate where the API version has been extracted from.
 */
public enum VersionSource {
    /**
     * Indicates that the version hasn't been explicitly specified, so the default value has been used.
     */
    DEFAULT,

    /**
     * Indicates that the version has been extracted from the {@code Version} HTTP header.
     */
    HEADER,

    /**
     * Indicates that the version has been extracted from the prefix of the URL, for example, if the request URI
     * contains a {@code v4} prefix like in {@code http://engine.example.com/ovirt-engine/api/v4/vms}.
     */
    URL,
}
