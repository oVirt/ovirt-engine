/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKeys;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenstackVolumeAuthenticationKeys;

public class V3OpenstackVolumeAuthenticationKeysInAdapter implements V3Adapter<V3OpenstackVolumeAuthenticationKeys, OpenstackVolumeAuthenticationKeys> {
    @Override
    public OpenstackVolumeAuthenticationKeys adapt(V3OpenstackVolumeAuthenticationKeys from) {
        OpenstackVolumeAuthenticationKeys to = new OpenstackVolumeAuthenticationKeys();
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
        to.getOpenstackVolumeAuthenticationKeys().addAll(adaptIn(from.getOpenstackVolumeAuthenticationKeys()));
        return to;
    }
}
