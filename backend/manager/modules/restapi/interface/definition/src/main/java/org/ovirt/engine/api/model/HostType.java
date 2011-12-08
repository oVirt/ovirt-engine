/*
* Copyright (c) 2010 Red Hat, Inc.
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

public enum HostType {
    RHEL("rhel"), RHEV_H("rhev-h");

    private String value;

    HostType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HostType fromValue(String v) {
        try {
            if (v==null) {
                return null;
            }
            if (v.equals("rhel")) {
                return RHEL;
            } else if (v.equals("rhev-h")) {
                return RHEV_H;
            } else {
                return valueOf(v.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
