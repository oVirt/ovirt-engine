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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.model.Versions;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Cluster;

public class V3ClusterInAdapter implements V3Adapter<V3Cluster, Cluster> {
    @Override
    public Cluster adapt(V3Cluster from) {
        Cluster to = new Cluster();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBallooningEnabled()) {
            to.setBallooningEnabled(from.isBallooningEnabled());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCpu()) {
            to.setCpu(adaptIn(from.getCpu()));
        }
        if (from.isSetDataCenter()) {
            to.setDataCenter(adaptIn(from.getDataCenter()));
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptIn(from.getDisplay()));
        }
        if (from.isSetErrorHandling()) {
            to.setErrorHandling(adaptIn(from.getErrorHandling()));
        }
        if (from.isSetFencingPolicy()) {
            to.setFencingPolicy(adaptIn(from.getFencingPolicy()));
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
            to.setKsm(adaptIn(from.getKsm()));
        }
        if (from.isSetMaintenanceReasonRequired()) {
            to.setMaintenanceReasonRequired(from.isMaintenanceReasonRequired());
        }
        if (from.isSetManagementNetwork()) {
            to.setManagementNetwork(adaptIn(from.getManagementNetwork()));
        }
        if (from.isSetMemoryPolicy()) {
            to.setMemoryPolicy(adaptIn(from.getMemoryPolicy()));
        }
        if (from.isSetMigration()) {
            to.setMigration(adaptIn(from.getMigration()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetOptionalReason()) {
            to.setOptionalReason(from.isOptionalReason());
        }
        if (from.isSetRequiredRngSources()) {
            to.setRequiredRngSources(new Cluster.RequiredRngSourcesList());
            to.getRequiredRngSources().getRequiredRngSources().addAll(adaptRngSources(from));
        }
        if (from.isSetSchedulingPolicy()) {
            to.setSchedulingPolicy(adaptIn(from.getSchedulingPolicy()));
        }
        if (from.isSetSerialNumber()) {
            to.setSerialNumber(adaptIn(from.getSerialNumber()));
        }
        if (from.isSetSupportedVersions()) {
            to.setSupportedVersions(new Versions());
            to.getSupportedVersions().getVersions().addAll(adaptIn(from.getSupportedVersions().getVersions()));
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
            to.setVersion(adaptIn(from.getVersion()));
        }
        if (from.isSetVirtService()) {
            to.setVirtService(from.isVirtService());
        }
        return to;
    }

    private List<RngSource> adaptRngSources(V3Cluster from) {
        List<RngSource> results = new LinkedList<>();
        for (String s : from.getRequiredRngSources().getRngSources()) {
            results.add(RngSource.fromValue(s));
        }
        return results;
    }
}
