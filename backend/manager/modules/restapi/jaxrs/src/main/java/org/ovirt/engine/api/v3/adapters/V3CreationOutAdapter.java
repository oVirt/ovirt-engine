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

import org.ovirt.engine.api.model.Creation;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Creation;
import org.ovirt.engine.api.v3.types.V3Status;

public class V3CreationOutAdapter implements V3Adapter<Creation, V3Creation> {
    @Override
    public V3Creation adapt(Creation from) {
        V3Creation to = new V3Creation();
        if (from.isSetId()) {
            to.setId(from.getId());
        }
        if (from.isSetHref()) {
            to.setHref(from.getHref());
        }
        if (from.isSetLinks()) {
            to.getLinks().addAll(adaptOut(from.getLinks()));
        }
        if (from.isSetStatus()) {
            V3Status toStatus = new V3Status();
            toStatus.setState(from.getStatus());
            to.setStatus(toStatus);
        }
        if (from.isSetFault()) {
            to.setFault(adaptOut(from.getFault()));
        }
        return to;
    }
}
