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

/**
 * This enum holds the types of all internal scheduling policies
 */
public enum SchedulingPolicyType {
    /**
     * Distributes the guest in a way that the CPU usage is approximately the same
     * on all hosts in the cluster
     */
    EVENLY_DISTRIBUTED,
    /**
     * Distributes the guests in a way that is the best for saving power
     */
    POWER_SAVING,
    NONE,
    /**
     * Distributes the guests in a way that every host host has approximately the same
     * number of running guests
     */
    VM_EVENLY_DISTRIBUTED;


    public String value() {
        return name().toLowerCase();
    }

    public static SchedulingPolicyType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
