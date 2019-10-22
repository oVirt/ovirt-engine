/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Ips;
import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.model.ReportedDeviceType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ReportedDevice;

public class V3ReportedDeviceInAdapter implements V3Adapter<V3ReportedDevice, ReportedDevice> {
    @Override
    public ReportedDevice adapt(V3ReportedDevice from) {
        ReportedDevice to = new ReportedDevice();
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
        if (from.isSetIps()) {
            to.setIps(new Ips());
            to.getIps().getIps().addAll(adaptIn(from.getIps().getIPs()));
        }
        if (from.isSetMac()) {
            to.setMac(adaptIn(from.getMac()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetType()) {
            to.setType(ReportedDeviceType.fromValue(from.getType()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
