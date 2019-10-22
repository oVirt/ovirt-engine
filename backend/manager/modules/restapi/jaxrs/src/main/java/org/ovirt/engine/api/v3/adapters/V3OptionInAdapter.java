/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Option;

public class V3OptionInAdapter implements V3Adapter<V3Option, Option> {
    @Override
    public Option adapt(V3Option from) {
        Option to = new Option();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetType()) {
            to.setType(from.getType());
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
