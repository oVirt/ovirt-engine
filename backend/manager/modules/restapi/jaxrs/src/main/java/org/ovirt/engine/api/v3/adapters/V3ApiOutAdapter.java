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

import static org.ovirt.engine.api.v3.adapters.V3OutAdapters.adaptOut;

import org.ovirt.engine.api.model.Api;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.invocation.CurrentManager;
import org.ovirt.engine.api.restapi.invocation.VersionSource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3API;
import org.ovirt.engine.api.v3.types.V3Link;

public class V3ApiOutAdapter implements V3Adapter<Api, V3API> {
    @Override
    public V3API adapt(Api from) {
        V3API to = new V3API();
        if (from.isSetActions()) {
            to.setActions(adaptOut(from.getActions()));
        }
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
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
