/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostDevice;

public class V3HostDeviceInAdapter implements V3Adapter<V3HostDevice, HostDevice> {
    @Override
    public HostDevice adapt(V3HostDevice from) {
        HostDevice to = new HostDevice();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetCapability()) {
            to.setCapability(from.getCapability());
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
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetIommuGroup()) {
            to.setIommuGroup(from.getIommuGroup());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetParentDevice()) {
            to.setParentDevice(adaptIn(from.getParentDevice()));
        }
        if (from.isSetPhysicalFunction()) {
            to.setPhysicalFunction(adaptIn(from.getPhysicalFunction()));
        }
        if (from.isSetPlaceholder()) {
            to.setPlaceholder(from.isPlaceholder());
        }
        if (from.isSetProduct()) {
            to.setProduct(adaptIn(from.getProduct()));
        }
        if (from.isSetVendor()) {
            to.setVendor(adaptIn(from.getVendor()));
        }
        if (from.isSetVirtualFunctions()) {
            to.setVirtualFunctions(from.getVirtualFunctions());
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
