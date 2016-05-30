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

import java.util.Objects;

import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostNIC;
import org.ovirt.engine.api.v3.types.V3Labels;
import org.ovirt.engine.api.v3.types.V3Link;
import org.ovirt.engine.api.v3.types.V3Properties;
import org.ovirt.engine.api.v3.types.V3Statistics;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3HostNicOutAdapter implements V3Adapter<HostNic, V3HostNIC> {
    @Override
    public V3HostNIC adapt(HostNic from) {
        V3HostNIC to = new V3HostNIC();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));

            // In version 3 of the API the name of the network labels sub-collection was just "labels", but in version
            // 4 of the API it has been renamed to "networklabels", so we need to adjust the links accordingly:
            to.getLinks().stream()
                .filter(this::isNetworksLabelsLink)
                .forEach(this::fixNetworkLabelsLink);
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetBaseInterface()) {
            to.setBaseInterface(from.getBaseInterface());
        }
        if (from.isSetBonding()) {
            to.setBonding(adaptOut(from.getBonding()));
        }
        if (from.isSetBootProtocol()) {
            to.setBootProtocol(from.getBootProtocol().value());
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
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIp()) {
            to.setIp(adaptOut(from.getIp()));
        }
        if (from.isSetNetworkLabels()) {
            to.setLabels(new V3Labels());
            to.getLabels().getLabels().addAll(adaptOut(from.getNetworkLabels().getNetworkLabels()));
        }
        if (from.isSetMac()) {
            to.setMac(adaptOut(from.getMac()));
        }
        if (from.isSetMtu()) {
            to.setMtu(from.getMtu());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetwork()) {
            to.setNetwork(adaptOut(from.getNetwork()));
        }
        if (from.isSetOverrideConfiguration()) {
            to.setOverrideConfiguration(from.isOverrideConfiguration());
        }
        if (from.isSetPhysicalFunction()) {
            to.setPhysicalFunction(adaptOut(from.getPhysicalFunction()));
        }
        if (from.isSetProperties()) {
            to.setProperties(new V3Properties());
            to.getProperties().getProperties().addAll(adaptOut(from.getProperties().getProperties()));
        }
        if (from.isSetQos()) {
            to.setQos(adaptOut(from.getQos()));
        }
        if (from.isSetSpeed()) {
            to.setSpeed(from.getSpeed());
        }
        if (from.isSetStatistics()) {
            to.setStatistics(new V3Statistics());
            to.getStatistics().getStatistics().addAll(adaptOut(from.getStatistics().getStatistics()));
        }
        if (from.isSetStatus()) {
            V3Status status = new V3Status();
            status.setState(from.getStatus().value());
            to.setStatus(status);
        }
        if (from.isSetVirtualFunctionsConfiguration()) {
            to.setVirtualFunctionsConfiguration(adaptOut(from.getVirtualFunctionsConfiguration()));
        }
        if (from.isSetVlan()) {
            to.setVlan(adaptOut(from.getVlan()));
        }
        return to;
    }

    /**
     * Checks if the given link corresponds to the network labels sub-collection.
     */
    private boolean isNetworksLabelsLink(V3Link link) {
        return Objects.equals(link.getRel(), "networklabels");
    }

    /**
     * Fixes a network labels link, replacing {@code networklabels} with {@code labels}.
     */
    private void fixNetworkLabelsLink(V3Link link) {
        // Fix the rel:
        link.setRel("labels");

        // Fix the href:
        String href = link.getHref();
        int index = href.lastIndexOf("/");
        if (index > 0) {
            href = href.substring(0, index + 1) + "labels";
            link.setHref(href);
        }
    }
}
