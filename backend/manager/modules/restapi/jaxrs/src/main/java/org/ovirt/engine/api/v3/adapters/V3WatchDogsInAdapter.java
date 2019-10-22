/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Watchdogs;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3WatchDogs;

public class V3WatchDogsInAdapter implements V3Adapter<V3WatchDogs, Watchdogs> {
    @Override
    public Watchdogs adapt(V3WatchDogs from) {
        Watchdogs to = new Watchdogs();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getWatchdogs().addAll(adaptIn(from.getWatchDogs()));
        return to;
    }
}
