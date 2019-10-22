/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HostDevice;

public class V3HostDeviceOutAdapter implements V3Adapter<HostDevice, V3HostDevice> {
    @Override
    public V3HostDevice adapt(HostDevice from) {
        V3HostDevice to = new V3HostDevice();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
            to.setHost(adaptOut(from.getHost()));
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
            to.setParentDevice(adaptOut(from.getParentDevice()));
        }
        if (from.isSetPhysicalFunction()) {
            to.setPhysicalFunction(adaptOut(from.getPhysicalFunction()));
        }
        if (from.isSetPlaceholder()) {
            to.setPlaceholder(from.isPlaceholder());
        }
        if (from.isSetProduct()) {
            to.setProduct(adaptOut(from.getProduct()));
        }
        if (from.isSetVendor()) {
            to.setVendor(adaptOut(from.getVendor()));
        }
        if (from.isSetVirtualFunctions()) {
            to.setVirtualFunctions(from.getVirtualFunctions());
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
