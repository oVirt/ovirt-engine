/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.Bookmarks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bookmarks;

public class V3BookmarksInAdapter implements V3Adapter<V3Bookmarks, Bookmarks> {
    @Override
    public Bookmarks adapt(V3Bookmarks from) {
        Bookmarks to = new Bookmarks();
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
        to.getBookmarks().addAll(adaptIn(from.getBookmarks()));
        return to;
    }
}
