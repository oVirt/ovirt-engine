/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.ExternalHostGroups;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ExternalHostGroups;

public class V3ExternalHostGroupsInAdapter implements V3Adapter<V3ExternalHostGroups, ExternalHostGroups> {
    @Override
    public ExternalHostGroups adapt(V3ExternalHostGroups from) {
        ExternalHostGroups to = new ExternalHostGroups();
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
        to.getExternalHostGroups().addAll(adaptIn(from.getExternalHostGroups()));
        return to;
    }
}
