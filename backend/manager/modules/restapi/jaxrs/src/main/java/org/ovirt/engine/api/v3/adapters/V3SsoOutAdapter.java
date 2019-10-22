/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Sso;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Methods;
import org.ovirt.engine.api.v3.types.V3Sso;

public class V3SsoOutAdapter implements V3Adapter<Sso, V3Sso> {
    @Override
    public V3Sso adapt(Sso from) {
        V3Sso to = new V3Sso();
        if (from.isSetMethods()) {
            to.setMethods(new V3Methods());
            to.getMethods().getMethods().addAll(adaptOut(from.getMethods().getMethods()));
        }
        return to;
    }
}
