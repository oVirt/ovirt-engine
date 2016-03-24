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

import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.NicInterface;
import org.ovirt.engine.api.model.ReportedDevices;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NIC;

public class V3NICInAdapter implements V3Adapter<V3NIC, Nic> {
    @Override
    public Nic adapt(V3NIC from) {
        Nic to = new Nic();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBootProtocol()) {
            to.setBootProtocol(BootProtocol.fromValue(from.getBootProtocol()));
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
            to.setInstanceType(adaptIn(from.getInstanceType()));
        }
        if (from.isSetInterface()) {
            to.setInterface(NicInterface.fromValue(from.getInterface()));
        }
        if (from.isSetLinked()) {
            to.setLinked(from.isLinked());
        }
        if (from.isSetMac()) {
            to.setMac(adaptIn(from.getMac()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetwork()) {
            to.setNetwork(adaptIn(from.getNetwork()));
        }
        if (from.isSetOnBoot()) {
            to.setOnBoot(from.isOnBoot());
        }
        if (from.isSetPlugged()) {
            to.setPlugged(from.isPlugged());
        }
        if (from.isSetReportedDevices()) {
            to.setReportedDevices(new ReportedDevices());
            to.getReportedDevices().getReportedDevices().addAll(adaptIn(from.getReportedDevices().getReportedDevices()));
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistics().getStatistics()));
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        if (from.isSetVms()) {
            to.setVms(new Vms());
            to.getVms().getVms().addAll(adaptIn(from.getVms().getVMs()));
        }
        if (from.isSetVnicProfile()) {
            to.setVnicProfile(adaptIn(from.getVnicProfile()));
        }

        // In V3 the "active" property used to be a synonym of "plugged":
        if (from.isSetActive() && to.isSetPlugged()) {
            to.setPlugged(from.isActive());
        }

        return to;
    }
}
