/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3WatchDog;

public class V3WatchDogInAdapter implements V3Adapter<V3WatchDog, Watchdog> {
    @Override
    public Watchdog adapt(V3WatchDog from) {
        Watchdog to = new Watchdog();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetAction()) {
            to.setAction(WatchdogAction.fromValue(from.getAction()));
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
            to.setInstanceType(adaptIn(from.getInstanceType()));
        }
        if (from.isSetModel()) {
            to.setModel(WatchdogModel.fromValue(from.getModel()));
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        if (from.isSetVms()) {
            to.setVms(new Vms());
            to.getVms().getVms().addAll(adaptIn(from.getVms().getVMs()));
        }
        return to;
    }
}
