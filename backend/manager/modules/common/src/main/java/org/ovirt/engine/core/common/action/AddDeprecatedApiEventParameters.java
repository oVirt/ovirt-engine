/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.common.action;

public class AddDeprecatedApiEventParameters extends ActionParametersBase {
    private static final long serialVersionUID = 7023971593753015625L;

    private String apiVersion;
    private String clientAddress;
    private String deprecatingVersion;
    private String removingVersion;

    /**
     * Returns the API version that is deprecated.
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns the address of the client that is sending the request using a deprecated version of the API.
     */
    public String getClientAddress() {
        return clientAddress;
    }

    /**
     * Returns the version of the engine where the version of the API has been declared as deprecated.
     */
    public String getDeprecatingVersion() {
        return deprecatingVersion;
    }

    /**
     * Returns the version of the engine where the version of the API will be removed.
     */
    public String getRemovingVersion() {
        return removingVersion;
    }

    public AddDeprecatedApiEventParameters() {
    }

    public AddDeprecatedApiEventParameters(String apiVersion, String clientAddress, String deprecatingVersion,
            String removingVersion) {
        super();
        this.apiVersion = apiVersion;
        this.clientAddress = clientAddress;
        this.deprecatingVersion = deprecatingVersion;
        this.removingVersion = removingVersion;
    }
}
