/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.SerialNumber;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SerialNumber;

public class V3SerialNumberInAdapter implements V3Adapter<V3SerialNumber, SerialNumber> {
    @Override
    public SerialNumber adapt(V3SerialNumber from) {
        SerialNumber to = new SerialNumber();
        if (from.isSetPolicy()) {
            to.setPolicy(SerialNumberPolicy.fromValue(from.getPolicy()));
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
