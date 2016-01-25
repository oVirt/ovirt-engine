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

import static org.ovirt.engine.api.v3.adapters.V3InAdapters.adaptIn;

import org.ovirt.engine.api.model.FencingPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3FencingPolicy;

public class V3FencingPolicyInAdapter implements V3Adapter<V3FencingPolicy, FencingPolicy> {
    @Override
    public FencingPolicy adapt(V3FencingPolicy from) {
        FencingPolicy to = new FencingPolicy();
        if (from.isSetEnabled()) {
            to.setEnabled(from.isEnabled());
        }
        if (from.isSetSkipIfConnectivityBroken()) {
            to.setSkipIfConnectivityBroken(adaptIn(from.getSkipIfConnectivityBroken()));
        }
        if (from.isSetSkipIfSdActive()) {
            to.setSkipIfSdActive(adaptIn(from.getSkipIfSdActive()));
        }
        return to;
    }
}
