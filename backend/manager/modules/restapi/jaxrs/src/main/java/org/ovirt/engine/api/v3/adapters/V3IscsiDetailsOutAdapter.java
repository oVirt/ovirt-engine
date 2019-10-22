/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.IscsiDetails;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IscsiDetails;

public class V3IscsiDetailsOutAdapter implements V3Adapter<IscsiDetails, V3IscsiDetails> {
    @Override
    public V3IscsiDetails adapt(IscsiDetails from) {
        V3IscsiDetails to = new V3IscsiDetails();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetDiskId()) {
            to.setDiskId(from.getDiskId());
        }
        if (from.isSetInitiator()) {
            to.setInitiator(from.getInitiator());
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
            to.setSize(Long.valueOf(from.getSize()));
        }
        if (from.isSetStatus()) {
            to.setStatus(from.getStatus());
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
