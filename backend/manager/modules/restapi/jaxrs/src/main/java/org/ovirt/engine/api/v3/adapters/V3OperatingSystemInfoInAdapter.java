/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.OperatingSystemInfo;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OperatingSystemInfo;

public class V3OperatingSystemInfoInAdapter implements V3Adapter<V3OperatingSystemInfo, OperatingSystemInfo> {
    @Override
    public OperatingSystemInfo adapt(V3OperatingSystemInfo from) {
        OperatingSystemInfo to = new OperatingSystemInfo();
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
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLargeIcon()) {
            to.setLargeIcon(adaptIn(from.getLargeIcon()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetSmallIcon()) {
            to.setSmallIcon(adaptIn(from.getSmallIcon()));
        }
        return to;
    }
}
