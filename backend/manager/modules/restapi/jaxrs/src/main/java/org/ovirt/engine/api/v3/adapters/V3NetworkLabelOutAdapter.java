/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
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
