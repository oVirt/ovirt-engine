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
import static org.ovirt.engine.api.v3.helpers.V3NICHelper.setNetworkAndPortMirroring;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NIC;
import org.ovirt.engine.api.v3.types.V3ReportedDevices;
import org.ovirt.engine.api.v3.types.V3Statistics;
import org.ovirt.engine.api.v3.types.V3VMs;

public class V3NicOutAdapter implements V3Adapter<Nic, V3NIC> {
    @Override
    public V3NIC adapt(Nic from) {
        V3NIC to = new V3NIC();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBootProtocol()) {
            to.setBootProtocol(from.getBootProtocol().value());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptOut(from.getInstanceType()));
        }
        if (from.isSetInterface()) {
            to.setInterface(from.getInterface().value());
        }
        if (from.isSetLinked()) {
            to.setLinked(from.isLinked());
        }
        if (from.isSetMac()) {
            to.setMac(adaptOut(from.getMac()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetwork()) {
            to.setNetwork(adaptOut(from.getNetwork()));
        }
        if (from.isSetOnBoot()) {
            to.setOnBoot(from.isOnBoot());
        }
        if (from.isSetPlugged()) {
            to.setPlugged(from.isPlugged());

            // In V3 the "active" property used to be a synonym of "plugged":
            to.setActive(from.isPlugged());
        }
        if (from.isSetReportedDevices()) {
            to.setReportedDevices(new V3ReportedDevices());
            to.getReportedDevices().getReportedDevices().addAll(adaptOut(from.getReportedDevices().getReportedDevices()));
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new V3Statistics());
            to.getStatistics().getStatistics().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        if (from.isSetVms()) {
            to.setVms(new V3VMs());
            to.getVms().getVMs().addAll(adaptOut(from.getVms().getVms()));
        }
        if (from.isSetVnicProfile()) {
            to.setVnicProfile(adaptOut(from.getVnicProfile()));

            // In V4 the "network" and "port_mirroring" properties of the NIC have been removed, but in V3 they must
            // be populated:
            setNetworkAndPortMirroring(from, to);
        }

        return to;
    }
}
