/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.AuthorizedKeys;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3AuthorizedKeys;

public class V3AuthorizedKeysOutAdapter implements V3Adapter<AuthorizedKeys, V3AuthorizedKeys> {
    @Override
    public V3AuthorizedKeys adapt(AuthorizedKeys from) {
        V3AuthorizedKeys to = new V3AuthorizedKeys();
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
        to.getAuthorizedKeys().addAll(adaptOut(from.getAuthorizedKeys()));
        return to;
    }
}
