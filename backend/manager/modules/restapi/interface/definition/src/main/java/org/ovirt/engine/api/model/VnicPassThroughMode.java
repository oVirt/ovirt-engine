/*
* Copyright (c) 2015 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.model;

/**
 * The enum describes whether vNIC to be implemented as a pass-through device or a virtual one.
 * Currently it supports only 2 option, but there is a plan to add more in the future.
 */
public enum VnicPassThroughMode {
    /**
     * To be implemented as a pass-through device
     */
    ENABLED,

    /**
     * To be implemented as a virtual device
     */
    DISABLED;

    public String value() {
        return name().toLowerCase();
    }

    public static VnicPassThroughMode fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
