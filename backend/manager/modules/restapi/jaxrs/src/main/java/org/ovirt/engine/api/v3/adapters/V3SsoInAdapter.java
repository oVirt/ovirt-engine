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

import org.ovirt.engine.api.model.Methods;
import org.ovirt.engine.api.model.Sso;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3Sso;

public class V3SsoInAdapter implements V3Adapter<V3Sso, Sso> {
    @Override
    public Sso adapt(V3Sso from) {
        Sso to = new Sso();
        if (from.isSetMethods()) {
            to.setMethods(new Methods());
            to.getMethods().getMethods().addAll(adaptIn(from.getMethods().getMethods()));
        }
        return to;
    }
}
