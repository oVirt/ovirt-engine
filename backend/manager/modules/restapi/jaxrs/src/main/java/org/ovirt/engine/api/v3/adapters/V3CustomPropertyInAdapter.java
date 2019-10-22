/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.CustomProperty;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3CustomProperty;

public class V3CustomPropertyInAdapter implements V3Adapter<V3CustomProperty, CustomProperty> {
    @Override
    public CustomProperty adapt(V3CustomProperty from) {
        CustomProperty to = new CustomProperty();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetRegexp()) {
            to.setRegexp(from.getRegexp());
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
