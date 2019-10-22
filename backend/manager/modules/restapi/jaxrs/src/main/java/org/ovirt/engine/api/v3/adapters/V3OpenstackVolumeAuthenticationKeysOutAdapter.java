/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.OpenstackVolumeAuthenticationKeys;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenstackVolumeAuthenticationKeys;

public class V3OpenstackVolumeAuthenticationKeysOutAdapter implements V3Adapter<OpenstackVolumeAuthenticationKeys, V3OpenstackVolumeAuthenticationKeys> {
    @Override
    public V3OpenstackVolumeAuthenticationKeys adapt(OpenstackVolumeAuthenticationKeys from) {
        V3OpenstackVolumeAuthenticationKeys to = new V3OpenstackVolumeAuthenticationKeys();
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
        to.getOpenstackVolumeAuthenticationKeys().addAll(adaptOut(from.getOpenstackVolumeAuthenticationKeys()));
        return to;
    }
}
