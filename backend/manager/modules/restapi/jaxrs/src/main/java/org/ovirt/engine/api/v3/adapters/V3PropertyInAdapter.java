/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Property;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Property;

public class V3PropertyInAdapter implements V3Adapter<V3Property, Property> {
    @Override
    public Property adapt(V3Property from) {
        Property to = new Property();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
