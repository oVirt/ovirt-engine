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
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.NetworkLabels;
import org.ovirt.engine.api.model.NicStatus;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostNIC;

public class V3HostNICInAdapter implements V3Adapter<V3HostNIC, HostNic> {
    @Override
    public HostNic adapt(V3HostNIC from) {
        HostNic to = new HostNic();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetBaseInterface()) {
            to.setBaseInterface(from.getBaseInterface());
        }
        if (from.isSetBonding()) {
            to.setBonding(adaptIn(from.getBonding()));
        }
        if (from.isSetBootProtocol()) {
            to.setBootProtocol(BootProtocol.fromValue(from.getBootProtocol()));
        }
        if (from.isSetBridged()) {
            to.setBridged(from.isBridged());
        }
        if (from.isSetCheckConnectivity()) {
            to.setCheckConnectivity(from.isCheckConnectivity());
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetCustomConfiguration()) {
            to.setCustomConfiguration(from.isCustomConfiguration());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIp()) {
            to.setIp(adaptIn(from.getIp()));
        }
        if (from.isSetLabels()) {
            to.setNetworkLabels(new NetworkLabels());
            to.getNetworkLabels().getNetworkLabels().addAll(adaptIn(from.getLabels().getLabels()));
        }
        if (from.isSetMac()) {
            to.setMac(adaptIn(from.getMac()));
        }
        if (from.isSetMtu()) {
            to.setMtu(from.getMtu());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetwork()) {
            to.setNetwork(adaptIn(from.getNetwork()));
        }
        if (from.isSetOverrideConfiguration()) {
            to.setOverrideConfiguration(from.isOverrideConfiguration());
        }
        if (from.isSetPhysicalFunction()) {
            to.setPhysicalFunction(adaptIn(from.getPhysicalFunction()));
        }
        if (from.isSetProperties()) {
            to.setProperties(new Properties());
            to.getProperties().getProperties().addAll(adaptIn(from.getProperties().getProperties()));
        }
        if (from.isSetQos()) {
            to.setQos(adaptIn(from.getQos()));
        }
        if (from.isSetSpeed()) {
            to.setSpeed(from.getSpeed());
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new Statistics());
            to.getStatistics().getStatistics().addAll(adaptIn(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus() && from.getStatus().isSetState()) {
            to.setStatus(NicStatus.fromValue(from.getStatus().getState()));
        }
        if (from.isSetVirtualFunctionsConfiguration()) {
            to.setVirtualFunctionsConfiguration(adaptIn(from.getVirtualFunctionsConfiguration()));
        }
        if (from.isSetVlan()) {
            to.setVlan(adaptIn(from.getVlan()));
        }
        return to;
    }
}
