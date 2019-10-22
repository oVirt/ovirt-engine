/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Cdrom;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CdRom;
import org.ovirt.engine.api.v3.types.V3VMs;

public class V3CdromOutAdapter implements V3Adapter<Cdrom, V3CdRom> {
    @Override
    public V3CdRom adapt(Cdrom from) {
        V3CdRom to = new V3CdRom();
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
        if (from.isSetFile()) {
            to.setFile(adaptOut(from.getFile()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptOut(from.getInstanceType()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        if (from.isSetVms()) {
            to.setVms(new V3VMs());
            to.getVms().getVMs().addAll(adaptOut(from.getVms().getVms()));
        }
        return to;
    }
}
