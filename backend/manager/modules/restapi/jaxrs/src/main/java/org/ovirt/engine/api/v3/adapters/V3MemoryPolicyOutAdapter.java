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

import org.ovirt.engine.api.model.MemoryPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MemoryPolicy;

public class V3MemoryPolicyOutAdapter implements V3Adapter<MemoryPolicy, V3MemoryPolicy> {
    @Override
    public V3MemoryPolicy adapt(MemoryPolicy from) {
        V3MemoryPolicy to = new V3MemoryPolicy();
        if (from.isSetBallooning()) {
            to.setBallooning(from.isBallooning());
        }
        if (from.isSetGuaranteed()) {
            to.setGuaranteed(from.getGuaranteed());
        }
        if (from.isSetOverCommit()) {
            to.setOverCommit(adaptOut(from.getOverCommit()));
        }
        if (from.isSetTransparentHugepages()) {
            to.setTransparentHugepages(adaptOut(from.getTransparentHugepages()));
        }
        return to;
    }
}
