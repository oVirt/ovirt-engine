/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Method;
import org.ovirt.engine.api.model.SsoMethod;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Method;

public class V3MethodInAdapter implements V3Adapter<V3Method, Method> {
    @Override
    public Method adapt(V3Method from) {
        Method to = new Method();
        if (from.isSetId()) {
            to.setId(SsoMethod.fromValue(from.getId()));
        }
        return to;
    }
}
