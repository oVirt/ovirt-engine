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

public enum OsType {
    UNASSIGNED,
    WINDOWS_XP,
    WINDOWS_2003,
    WINDOWS_2008,
    OTHER_LINUX,
    OTHER,
    RHEL_5,
    RHEL_4,
    RHEL_3,
    WINDOWS_2003X64,
    WINDOWS_7,
    WINDOWS_7X64,
    RHEL_5X64,
    RHEL_4X64,
    RHEL_3X64,
    WINDOWS_2008X64,
    WINDOWS_2008R2X64,
    RHEL_6,
    RHEL_6X64,
    WINDOWS_8,
    WINDOWS_8X64,
    WINDOWS_2012X64;

    public String value() {
        return name().toLowerCase();
    }

    public static OsType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
