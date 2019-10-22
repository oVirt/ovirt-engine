/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.SshPublicKeys;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SSHPublicKeys;

public class V3SshPublicKeysOutAdapter implements V3Adapter<SshPublicKeys, V3SSHPublicKeys> {
    @Override
    public V3SSHPublicKeys adapt(SshPublicKeys from) {
        V3SSHPublicKeys to = new V3SSHPublicKeys();
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
        to.getSSHPublicKeys().addAll(adaptOut(from.getSshPublicKeys()));
        return to;
    }
}
