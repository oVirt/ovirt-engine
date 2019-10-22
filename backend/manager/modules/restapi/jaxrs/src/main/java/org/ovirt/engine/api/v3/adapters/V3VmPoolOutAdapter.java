/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VmPool;

public class V3VmPoolOutAdapter implements V3Adapter<VmPool, V3VmPool> {
    @Override
    public V3VmPool adapt(VmPool from) {
        V3VmPool to = new V3VmPool();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptOut(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptOut(from.getDisplay()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetMaxUserVms()) {
            to.setMaxUserVms(from.getMaxUserVms());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetPrestartedVms()) {
            to.setPrestartedVms(from.getPrestartedVms());
        }
        if (from.isSetRngDevice()) {
            to.setRngDevice(adaptOut(from.getRngDevice()));
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetSoundcardEnabled()) {
            to.setSoundcardEnabled(from.isSoundcardEnabled());
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptOut(from.getTemplate()));
        }
        if (from.isSetType()) {
            to.setType(from.getType().value());
        }
        if (from.isSetUseLatestTemplateVersion()) {
            to.setUseLatestTemplateVersion(from.isUseLatestTemplateVersion());
        }
        if (from.isSetVm()) {
            to.setVm(adaptOut(from.getVm()));
        }
        return to;
    }
}
