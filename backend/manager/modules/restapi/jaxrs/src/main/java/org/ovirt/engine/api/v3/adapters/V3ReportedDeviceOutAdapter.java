/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IPs;
import org.ovirt.engine.api.v3.types.V3ReportedDevice;

public class V3ReportedDeviceOutAdapter implements V3Adapter<ReportedDevice, V3ReportedDevice> {
    @Override
    public V3ReportedDevice adapt(ReportedDevice from) {
        V3ReportedDevice to = new V3ReportedDevice();
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
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIps()) {
            to.setIps(new V3IPs());
            to.getIps().getIPs().addAll(adaptOut(from.getIps().getIps()));
        }
        if (from.isSetMac()) {
            to.setMac(adaptOut(from.getMac()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
