/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SerialNumber;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SerialNumber;

public class V3SerialNumberOutAdapter implements V3Adapter<SerialNumber, V3SerialNumber> {
    @Override
    public V3SerialNumber adapt(SerialNumber from) {
        V3SerialNumber to = new V3SerialNumber();
        if (from.isSetPolicy()) {
            to.setPolicy(from.getPolicy().value());
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
