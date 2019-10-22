/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.VmPool;
import org.ovirt.engine.api.model.VmPoolType;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VmPool;

public class V3VmPoolInAdapter implements V3Adapter<V3VmPool, VmPool> {
    @Override
    public VmPool adapt(V3VmPool from) {
        VmPool to = new VmPool();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetCluster()) {
            to.setCluster(adaptIn(from.getCluster()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetDisplay()) {
            to.setDisplay(adaptIn(from.getDisplay()));
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
            to.setRngDevice(adaptIn(from.getRngDevice()));
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetSoundcardEnabled()) {
            to.setSoundcardEnabled(from.isSoundcardEnabled());
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetType()) {
            to.setType(VmPoolType.fromValue(from.getType()));
        }
        if (from.isSetUseLatestTemplateVersion()) {
            to.setUseLatestTemplateVersion(from.isUseLatestTemplateVersion());
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
