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

import org.ovirt.engine.api.model.IpAddressAssignments;
import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.model.Properties;
import org.ovirt.engine.api.model.ReportedConfigurations;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NetworkAttachment;

public class V3NetworkAttachmentInAdapter implements V3Adapter<V3NetworkAttachment, NetworkAttachment> {
    @Override
    public NetworkAttachment adapt(V3NetworkAttachment from) {
        NetworkAttachment to = new NetworkAttachment();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetHostNic()) {
            to.setHostNic(adaptIn(from.getHostNic()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIpAddressAssignments()) {
            to.setIpAddressAssignments(new IpAddressAssignments());
            to.getIpAddressAssignments().getIpAddressAssignments().addAll(adaptIn(from.getIpAddressAssignments().getIpAddressAssignments()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetwork()) {
            to.setNetwork(adaptIn(from.getNetwork()));
        }
        if (from.isSetProperties()) {
            to.setProperties(new Properties());
            to.getProperties().getProperties().addAll(adaptIn(from.getProperties().getProperties()));
        }
        if (from.isSetQos()) {
            to.setQos(adaptIn(from.getQos()));
        }
        if (from.isSetReportedConfigurations()) {
            if (from.getReportedConfigurations().isSetInSync()) {
                to.setInSync(from.getReportedConfigurations().isInSync());
            }
            to.setReportedConfigurations(new ReportedConfigurations());
            to.getReportedConfigurations().getReportedConfigurations().addAll(adaptIn(from.getReportedConfigurations().getReportedConfigurations()));
        }
        return to;
    }
}
