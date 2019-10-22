/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VMs;
import org.ovirt.engine.api.v3.types.V3WatchDog;

public class V3WatchdogOutAdapter implements V3Adapter<Watchdog, V3WatchDog> {
    @Override
    public V3WatchDog adapt(Watchdog from) {
        V3WatchDog to = new V3WatchDog();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetAction()) {
            to.setAction(from.getAction().value());
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
        if (from.isSetInstanceType()) {
            to.setInstanceType(adaptOut(from.getInstanceType()));
        }
        if (from.isSetModel()) {
            to.setModel(from.getModel().value());
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
