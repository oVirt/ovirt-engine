/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Method;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Method;

public class V3MethodOutAdapter implements V3Adapter<Method, V3Method> {
    @Override
    public V3Method adapt(Method from) {
        V3Method to = new V3Method();
        if (from.isSetId()) {
            to.setId(from.getId().value());
        }
        return to;
    }
}
