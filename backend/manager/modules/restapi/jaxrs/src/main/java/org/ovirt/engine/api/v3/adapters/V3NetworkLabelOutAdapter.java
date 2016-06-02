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

import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Label;

public class V3NetworkLabelOutAdapter implements V3Adapter<NetworkLabel, V3Label> {
    @Override
    public V3Label adapt(NetworkLabel from) {
        V3Label to = new V3Label();
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
        if (from.isSetHostNic()) {
            to.setHostNic(adaptOut(from.getHostNic()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }

        // In version 4 of the API the "label" URL segment has been renamed to "networklabels", so we need to replace
        // it to preserve backwards compatibility with version 3:
        String href = from.getHref();
        if (href != null) {
            href = href.replace("/networklabels/", "/labels/");
            to.setHref(href);
        }

        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetNetwork()) {
            to.setNetwork(adaptOut(from.getNetwork()));
        }
        return to;
    }
}
