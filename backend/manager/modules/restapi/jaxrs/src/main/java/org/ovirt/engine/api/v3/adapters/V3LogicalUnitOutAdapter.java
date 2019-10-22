/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.LogicalUnit;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3LogicalUnit;

public class V3LogicalUnitOutAdapter implements V3Adapter<LogicalUnit, V3LogicalUnit> {
    @Override
    public V3LogicalUnit adapt(LogicalUnit from) {
        V3LogicalUnit to = new V3LogicalUnit();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetDiskId()) {
            to.setDiskId(from.getDiskId());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetLunMapping()) {
            to.setLunMapping(from.getLunMapping());
        }
        if (from.isSetPassword()) {
            to.setPassword(from.getPassword());
        }
        if (from.isSetPaths()) {
            to.setPaths(from.getPaths());
        }
        if (from.isSetPort()) {
            to.setPort(from.getPort());
        }
        if (from.isSetPortal()) {
            to.setPortal(from.getPortal());
        }
        if (from.isSetProductId()) {
            to.setProductId(from.getProductId());
        }
        if (from.isSetSerial()) {
            to.setSerial(from.getSerial());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetStatus()) {
            to.setStatus(from.getStatus().value());
        }
        if (from.isSetStorageDomainId()) {
            to.setStorageDomainId(from.getStorageDomainId());
        }
        if (from.isSetTarget()) {
            to.setTarget(from.getTarget());
        }
        if (from.isSetUsername()) {
            to.setUsername(from.getUsername());
        }
        if (from.isSetVendorId()) {
            to.setVendorId(from.getVendorId());
        }
        if (from.isSetVolumeGroupId()) {
            to.setVolumeGroupId(from.getVolumeGroupId());
        }
        return to;
    }
}
