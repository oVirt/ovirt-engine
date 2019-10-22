/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ErrorHandling;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ErrorHandling;

public class V3ErrorHandlingOutAdapter implements V3Adapter<ErrorHandling, V3ErrorHandling> {
    @Override
    public V3ErrorHandling adapt(ErrorHandling from) {
        V3ErrorHandling to = new V3ErrorHandling();
        if (from.isSetOnError()) {
            to.setOnError(from.getOnError().value());
        }
        return to;
    }
}
