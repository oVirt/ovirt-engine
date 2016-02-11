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

import org.ovirt.engine.api.model.NetworkAttachment;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IpAddressAssignments;
import org.ovirt.engine.api.v3.types.V3NetworkAttachment;
import org.ovirt.engine.api.v3.types.V3Properties;
import org.ovirt.engine.api.v3.types.V3ReportedConfigurations;

public class V3NetworkAttachmentOutAdapter implements V3Adapter<NetworkAttachment, V3NetworkAttachment> {
    @Override
    public V3NetworkAttachment adapt(NetworkAttachment from) {
        V3NetworkAttachment to = new V3NetworkAttachment();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetHost()) {
            to.setHost(adaptOut(from.getHost()));
        }
        if (from.isSetHostNic()) {
            to.setHostNic(adaptOut(from.getHostNic()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetInSync()) {
            V3ReportedConfigurations toReportedConfigurations = to.getReportedConfigurations();
            if (toReportedConfigurations == null) {
                toReportedConfigurations = new V3ReportedConfigurations();
                to.setReportedConfigurations(toReportedConfigurations);
            }
            toReportedConfigurations.setInSync(from.isInSync());
        }
        if (from.isSetIpAddressAssignments()) {
            to.setIpAddressAssignments(new V3IpAddressAssignments());
            to.getIpAddressAssignments().getIpAddressAssignments().addAll(adaptOut(from.getIpAddressAssignments().getIpAddressAssignments()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetwork()) {
            to.setNetwork(adaptOut(from.getNetwork()));
        }
        if (from.isSetProperties()) {
            to.setProperties(new V3Properties());
            to.getProperties().getProperties().addAll(adaptOut(from.getProperties().getProperties()));
        }
        if (from.isSetQos()) {
            to.setQos(adaptOut(from.getQos()));
        }
        if (from.isSetReportedConfigurations()) {
            V3ReportedConfigurations toReportedConfigurations = to.getReportedConfigurations();
            if (toReportedConfigurations == null) {
                toReportedConfigurations = new V3ReportedConfigurations();
                to.setReportedConfigurations(toReportedConfigurations);
            }
            toReportedConfigurations.getReportedConfigurations().addAll(adaptOut(from.getReportedConfigurations().getReportedConfigurations()));
        }
        return to;
    }
}
