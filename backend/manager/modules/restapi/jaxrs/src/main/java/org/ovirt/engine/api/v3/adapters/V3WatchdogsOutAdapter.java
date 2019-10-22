/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Watchdogs;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3WatchDogs;

public class V3WatchdogsOutAdapter implements V3Adapter<Watchdogs, V3WatchDogs> {
    @Override
    public V3WatchDogs adapt(Watchdogs from) {
        V3WatchDogs to = new V3WatchDogs();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        to.getWatchDogs().addAll(adaptOut(from.getWatchdogs()));
        return to;
    }
}
