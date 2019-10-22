/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.AuthorizedKey;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3AuthorizedKey;

public class V3AuthorizedKeyOutAdapter implements V3Adapter<AuthorizedKey, V3AuthorizedKey> {
    @Override
    public V3AuthorizedKey adapt(AuthorizedKey from) {
        V3AuthorizedKey to = new V3AuthorizedKey();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetKey()) {
            to.setKey(from.getKey());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetUser()) {
            to.setUser(adaptOut(from.getUser()));
        }
        return to;
    }
}
