/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.adapters;

import static java.util.stream.Collectors.toList;
import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.api.model.Api;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3API;
import org.ovirt.engine.api.v3.types.V3Link;

public class V3ApiOutAdapter implements V3Adapter<Api, V3API> {
    // The list of "rels" that should be removed from the set of links created by version 4 of the API, as they are
    // new and shouldn't appear in version 3 of the API:
    private static final Set<String> RELS_TO_REMOVE = new HashSet<>();

    static {
        RELS_TO_REMOVE.add("affinitylabels");
        RELS_TO_REMOVE.add("clusterlevels");
    }

    @Override
    public V3API adapt(Api from) {
        V3API to = new V3API();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }

        // Remove the links for "rels" that are new in version 4 of the API:
        if (from.isSetLinks()) {
            List<Link> links = from.getLinks().stream()
                .filter(link -> !RELS_TO_REMOVE.contains(link.getRel()))
                .collect(toList());
            to.getLinks().addAll(adaptOut(links));
        }

        // In version 4 of the API the "capabilities" resource was removed, but it still exists in version 3, so we
        // need to explicitly add a the link:
        to.getLinks().add(0, makeCapabilitiesLink());

        if (from.isSetSpecialObjects()) {
            to.setSpecialObjects(adaptOut(from.getSpecialObjects()));
        }
        if (from.isSetProductInfo()) {
            to.setProductInfo(adaptOut(from.getProductInfo()));
        }
        if (from.isSetSummary()) {
            to.setSummary(adaptOut(from.getSummary()));
        }
        if (from.isSetTime()) {
            to.setTime(from.getTime());
        }
        return to;
    }

    private V3Link makeCapabilitiesLink() {
        // Calculate the href:
        Current current = CurrentManager.get();
        StringBuilder buffer = new StringBuilder();
        buffer.append(current.getPrefix());
        if (current.getVersionSource() == VersionSource.URL) {
            buffer.append("/v");
            buffer.append(current.getVersion());
        }
        buffer.append("/capabilities");
        String href = buffer.toString();

        // Make the link:
        V3Link link = new V3Link();
        link.setRel("capabilities");
        link.setHref(href);

        return link;
    }
}
