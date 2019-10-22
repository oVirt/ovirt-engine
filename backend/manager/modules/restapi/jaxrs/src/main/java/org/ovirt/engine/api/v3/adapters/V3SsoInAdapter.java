/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Methods;
import org.ovirt.engine.api.model.Sso;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Sso;

public class V3SsoInAdapter implements V3Adapter<V3Sso, Sso> {
    @Override
    public Sso adapt(V3Sso from) {
        Sso to = new Sso();
        if (from.isSetMethods()) {
            to.setMethods(new Methods());
            to.getMethods().getMethods().addAll(adaptIn(from.getMethods().getMethods()));
        }
        return to;
    }
}
