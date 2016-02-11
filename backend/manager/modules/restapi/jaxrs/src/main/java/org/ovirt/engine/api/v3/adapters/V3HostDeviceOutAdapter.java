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
