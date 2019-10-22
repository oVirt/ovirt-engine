/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.OpenStackImages;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3OpenStackImages;

public class V3OpenStackImagesInAdapter implements V3Adapter<V3OpenStackImages, OpenStackImages> {
    @Override
    public OpenStackImages adapt(V3OpenStackImages from) {
        OpenStackImages to = new OpenStackImages();
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetSize()) {
            to.setSize(from.getSize());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        to.getOpenStackImages().addAll(adaptIn(from.getOpenStackImages()));
        return to;
    }
}
