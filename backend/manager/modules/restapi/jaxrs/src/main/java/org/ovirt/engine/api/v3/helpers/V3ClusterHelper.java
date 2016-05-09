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

package org.ovirt.engine.api.v3.helpers;

import java.util.Objects;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.api.model.SchedulingPolicies;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.types.V3Cluster;
import org.ovirt.engine.api.v3.types.V3Fault;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicy;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicyThresholds;

/**
 * This class contains a set of methods useful to handle backwards compatibility issues related to clusters.
 */
public class V3ClusterHelper {
    /**
     * Makes sure that the V4 cluster has a reference to a scheduling policy that is compatible with the one specified
     * by the given V3 cluster. If that isn't possible throws an exception that will result in an HTTP error response
     * sent to the caller.
     */
    public static void assignCompatiblePolicy(V3Cluster v3Cluster, Cluster v4Cluster) {
        V3SchedulingPolicy v3Policy = v3Cluster.getSchedulingPolicy();
        SchedulingPolicy v4Policy = v4Cluster.getSchedulingPolicy();
        boolean v3IsSet = v3Policy != null && (v3Policy.isSetName() || v3Policy.isSetPolicy());
        boolean v4IsSet = v4Policy != null && (v4Policy.isSetName() || v4Policy.isSetId());
        if (v3IsSet && !v4IsSet) {
            SchedulingPolicy v4CompatiblePolicy = findCompatiblePolicy(v3Policy);
            if (v4CompatiblePolicy != null) {
                v4Policy = new SchedulingPolicy();
                v4Policy.setId(v4CompatiblePolicy.getId());
                v4Cluster.setSchedulingPolicy(v4Policy);
            }
            else {
                V3Fault fault = new V3Fault();
                fault.setReason("Operation Failed");
                fault.setDetail("Can't find a compatible scheduling policy.");
                Response response = Response.serverError().entity(fault).build();
                throw new WebApplicationException(response);
            }
        }
    }

    /**
     * Tries to find a V4 scheduling policy that is compatible with the given V3 scheduling policy.
     *
     * @return the compatible scheduling policy, or {@code null} if no such policy can be found
     */
    public static SchedulingPolicy findCompatiblePolicy(V3SchedulingPolicy v3Policy) {
        SystemResource systemResource = BackendApiResource.getInstance();
        SchedulingPoliciesResource policiesResource = systemResource.getSchedulingPoliciesResource();
        SchedulingPolicies v4Policies = policiesResource.list();
        return v4Policies.getSchedulingPolicies().stream()
            .filter(v4Policy -> arePoliciesCompatible(v3Policy, v4Policy))
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if the given V3 and V4 scheduling policies are compatible.
     *
     * @return {@code true} iif the two policies are compatible
     */
    private static boolean arePoliciesCompatible(V3SchedulingPolicy v3Policy, SchedulingPolicy v4Policy) {
        // The V3 "policy" attribute must be equal to the V4 "name" attribute:
        String v3Name = v3Policy.getName();
        if (v3Name == null) {
            v3Name = v3Policy.getPolicy();
        }
        if (v3Name != null && !Objects.equals(v3Name, v4Policy.getName())) {
            return false;
        }

        // All the V3 threshold must match the equivalent V4 properties:
        V3SchedulingPolicyThresholds v3Thresholds = v3Policy.getThresholds();
        Properties v4Properties = v4Policy.getProperties();
        if (v3Thresholds != null) {
            Integer v3Duration = v3Thresholds.getDuration();
            Integer v4Duration = getIntegerProperty(v4Properties, "CpuOverCommitDurationMinutes");
            if (!arePropertiesCompatible(v3Duration, v4Duration)) {
                return false;
            }
            Integer v3High = v3Thresholds.getHigh();
            Integer v4High = getIntegerProperty(v4Properties, "HighUtilization");
            if (!arePropertiesCompatible(v3High, v4High)) {
                return false;
            }
            Integer v3Low = v3Thresholds.getLow();
            Integer v4Low = getIntegerProperty(v4Properties, "LowUtilization");
            if (!arePropertiesCompatible(v3Low, v4Low)) {
                return false;
            }
        }

        // All the tests passed, so the policies are compatible:
        return true;
    }

    /**
     * Finds an integer property with the given name inside the given properties object.
     *
     * @return the value of the property, or {@code null} if there is no such property
     */
    public static Integer getIntegerProperty(Properties properties, String name) {
        if (properties == null) {
            return null;
        }
        Optional<String> text = properties.getProperties().stream()
            .filter(property -> Objects.equals(name, property.getName()))
            .map(Property::getValue)
            .findFirst();
        if (text.isPresent()) {
            return Integer.valueOf(text.get());
        }
        return null;
    }

    /**
     * Checks if two integer scheduling policy properties are compatible.
     *
     * @param v3Value the V3 value
     * @param v4Value the V4 value
     * @returns {@code true} the the values are compatible, {@code false} otherwise
     */
    private static boolean arePropertiesCompatible(Integer v3Value, Integer v4Value) {
        return v3Value == null || v4Value == null || Objects.equals(v3Value, v4Value);
    }
}
