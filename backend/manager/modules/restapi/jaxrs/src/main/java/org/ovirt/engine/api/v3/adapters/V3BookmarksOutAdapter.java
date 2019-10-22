/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Bookmarks;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Bookmarks;

public class V3BookmarksOutAdapter implements V3Adapter<Bookmarks, V3Bookmarks> {
    @Override
    public V3Bookmarks adapt(Bookmarks from) {
        V3Bookmarks to = new V3Bookmarks();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
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
        to.getBookmarks().addAll(adaptOut(from.getBookmarks()));
        return to;
    }
}
