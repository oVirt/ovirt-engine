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

import org.ovirt.engine.api.model.MigrationOptions;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3MigrationOptions;

public class V3MigrationOptionsOutAdapter implements V3Adapter<MigrationOptions, V3MigrationOptions> {
    @Override
    public V3MigrationOptions adapt(MigrationOptions from) {
        V3MigrationOptions to = new V3MigrationOptions();
        if (from.isSetAutoConverge()) {
            to.setAutoConverge(from.getAutoConverge().value());
        }
        if (from.isSetCompressed()) {
            to.setCompressed(from.getCompressed().value());
        }
        return to;
    }
}
