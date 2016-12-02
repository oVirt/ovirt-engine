/*
Copyright (c) 2017 Red Hat, Inc.

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

package org.ovirt.engine.core.common.action;

public class AddDeprecatedApiEventParameters extends VdcActionParametersBase {
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
