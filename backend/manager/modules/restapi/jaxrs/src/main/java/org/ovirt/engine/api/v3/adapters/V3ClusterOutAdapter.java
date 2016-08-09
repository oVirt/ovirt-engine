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

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;
import static org.ovirt.engine.api.v3.helpers.V3ClusterHelper.getIntegerProperty;

import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.WebApplicationException;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.model.SchedulingPolicy;
import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.resource.SchedulingPolicyResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.restapi.resource.BackendApiResource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Cluster;
import org.ovirt.engine.api.v3.types.V3RngSources;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicy;
import org.ovirt.engine.api.v3.types.V3SchedulingPolicyThresholds;
import org.ovirt.engine.api.v3.types.V3SupportedVersions;

public class V3ClusterOutAdapter implements V3Adapter<Cluster, V3Cluster> {
    @Override
    public V3Cluster adapt(Cluster from) {
        V3Cluster to = new V3Cluster();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBallooningEnabled()) {
            to.setBallooningEnabled(from.isBallooningEnabled());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptOut(from.getCpu()));
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptOut(from.getDataCenter()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptOut(from.getDisplay()));
        }
        if (from.isSetErrorHandling()) {
            to.setErrorHandling(adaptOut(from.getErrorHandling()));
        }
        if (from.isSetFencingPolicy()) {
            to.setFencingPolicy(adaptOut(from.getFencingPolicy()));
        }
        if (from.isSetGlusterService()) {
            to.setGlusterService(from.isGlusterService());
        }
        if (from.isSetHaReservation()) {
            to.setHaReservation(from.isHaReservation());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetKsm()) {
            to.setKsm(adaptOut(from.getKsm()));
        }
        if (from.isSetMaintenanceReasonRequired()) {
            to.setMaintenanceReasonRequired(from.isMaintenanceReasonRequired());
        }
        if (from.isSetManagementNetwork()) {
            to.setManagementNetwork(adaptOut(from.getManagementNetwork()));
        }
        if (from.isSetMemoryPolicy()) {
            to.setMemoryPolicy(adaptOut(from.getMemoryPolicy()));
        }
        if (from.isSetMigration()) {
            to.setMigration(adaptOut(from.getMigration()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOptionalReason()) {
            to.setOptionalReason(from.isOptionalReason());
        }
        if (from.isSetRequiredRngSources()) {
            to.setRequiredRngSources(new V3RngSources());
            to.getRequiredRngSources().getRngSources().addAll(adaptRngSources(from));
        }
        if (from.isSetSerialNumber()) {
            to.setSerialNumber(adaptOut(from.getSerialNumber()));
        }
        if (from.isSetSupportedVersions()) {
            to.setSupportedVersions(new V3SupportedVersions());
            to.getSupportedVersions().getVersions().addAll(adaptOut(from.getSupportedVersions().getVersions()));
        }
        if (from.isSetThreadsAsCores()) {
            to.setThreadsAsCores(from.isThreadsAsCores());
        }
        if (from.isSetTrustedService()) {
            to.setTrustedService(from.isTrustedService());
        }
        if (from.isSetTunnelMigration()) {
            to.setTunnelMigration(from.isTunnelMigration());
        }
        if (from.isSetVersion()) {
            to.setVersion(adaptOut(from.getVersion()));
        }
        if (from.isSetVirtService()) {
            to.setVirtService(from.isVirtService());
        }

        // In V3 the scheduling policy was part of the cluster, so we need to retrieve the details and populate the
        // "policy" and "thresholds" elements:
        SchedulingPolicy fromPolicy = from.getSchedulingPolicy();
        if (fromPolicy != null && fromPolicy.isSetId()) {
            SystemResource systemResource = BackendApiResource.getInstance();
            SchedulingPoliciesResource policiesResource = systemResource.getSchedulingPoliciesResource();
            SchedulingPolicyResource policyResource = policiesResource.getPolicyResource(fromPolicy.getId());
            try {
                fromPolicy = policyResource.get();
            }
            catch (WebApplicationException exception) {
                // If an application exception is generated while retrieving the details of the scheduling policy it
                // is safe to ignore it, as it may be that the user just doesn't have permission to see the policy, but
                // she may still have permissions to see the other details of the cluster.
            }
            V3SchedulingPolicy toPolicy = to.getSchedulingPolicy();
            if (toPolicy == null) {
                toPolicy = new V3SchedulingPolicy();
                to.setSchedulingPolicy(toPolicy);
            }
            if (fromPolicy.isSetId()) {
                toPolicy.setId(fromPolicy.getId());
            }
            if (fromPolicy.isSetHref()) {
                toPolicy.setHref(fromPolicy.getHref());
            }
            if (fromPolicy.isSetName() && !toPolicy.isSetPolicy()) {
                toPolicy.setPolicy(fromPolicy.getName());
            }
            Properties fromProperties = fromPolicy.getProperties();
            if (fromProperties != null) {
                Integer fromDuration = getIntegerProperty(fromProperties, "CpuOverCommitDurationMinutes");
                if (fromDuration != null) {
                    V3SchedulingPolicyThresholds toThresholds = toPolicy.getThresholds();
                    if (toThresholds == null) {
                        toThresholds = new V3SchedulingPolicyThresholds();
                        toPolicy.setThresholds(toThresholds);
                    }
                    toThresholds.setDuration(fromDuration);
                }
                Integer fromHigh = getIntegerProperty(fromProperties, "HighUtilization");
                if (fromHigh != null) {
                    V3SchedulingPolicyThresholds toThresholds = toPolicy.getThresholds();
                    if (toThresholds == null) {
                        toThresholds = new V3SchedulingPolicyThresholds();
                        toPolicy.setThresholds(toThresholds);
                    }
                    toThresholds.setHigh(fromHigh);
                }
                Integer fromLow = getIntegerProperty(fromProperties, "LowUtilization");
                if (fromLow != null) {
                    V3SchedulingPolicyThresholds toThresholds = toPolicy.getThresholds();
                    if (toThresholds == null) {
                        toThresholds = new V3SchedulingPolicyThresholds();
                        toPolicy.setThresholds(toThresholds);
                    }
                    toThresholds.setLow(fromLow);
                }
            }
        }

        return to;
    }

    private List<String> adaptRngSources(Cluster from) {
        List<String> results = new LinkedList<>();
        for (RngSource source : from.getRequiredRngSources().getRequiredRngSources()) {
            results.add(source.value());
        }
        return results;
    }
}
