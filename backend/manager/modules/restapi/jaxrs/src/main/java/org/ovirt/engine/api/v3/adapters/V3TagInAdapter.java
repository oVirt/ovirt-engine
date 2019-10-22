/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Tag;
import org.ovirt.engine.api.v3.types.V3TagParent;

public class V3TagInAdapter implements V3Adapter<V3Tag, Tag> {
    @Override
    public Tag adapt(V3Tag from) {
        Tag to = new Tag();
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptIn(from.getLinks()));
        }
        if (from.isSetActions()) {
            to.setActions(adaptIn(from.getActions()));
        }
        if (from.isSetComment()) {
            to.setComment(from.getComment());
        }
        if (from.isSetDescription()) {
            to.setDescription(from.getDescription());
        }
        if (from.isSetGroup()) {
            to.setGroup(adaptIn(from.getGroup()));
        }
        if (from.isSetHost()) {
            to.setHost(adaptIn(from.getHost()));
        }
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetParent()) {
            V3TagParent fromParent = from.getParent();
            if (fromParent.isSetTag()) {
                to.setParent(adaptIn(fromParent.getTag()));
            }
        }
        if (from.isSetTemplate()) {
            to.setTemplate(adaptIn(from.getTemplate()));
        }
        if (from.isSetUser()) {
            to.setUser(adaptIn(from.getUser()));
        }
        if (from.isSetVm()) {
            to.setVm(adaptIn(from.getVm()));
        }
        return to;
    }
}
