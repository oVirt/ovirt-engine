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

import org.ovirt.engine.api.model.VmSummary;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3VmSummary;

public class V3VmSummaryInAdapter implements V3Adapter<V3VmSummary, VmSummary> {
    @Override
    public VmSummary adapt(V3VmSummary from) {
        VmSummary to = new VmSummary();
        if (from.isSetActive()) {
            to.setActive(from.getActive());
        }
        if (from.isSetMigrating()) {
            to.setMigrating(from.getMigrating());
        }
        if (from.isSetTotal()) {
            to.setTotal(from.getTotal());
        }
        return to;
    }
}
