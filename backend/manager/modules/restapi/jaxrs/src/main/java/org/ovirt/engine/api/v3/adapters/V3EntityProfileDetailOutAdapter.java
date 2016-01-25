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

import org.ovirt.engine.api.model.EntityProfileDetail;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3EntityProfileDetail;

public class V3EntityProfileDetailOutAdapter implements V3Adapter<EntityProfileDetail, V3EntityProfileDetail> {
    @Override
    public V3EntityProfileDetail adapt(EntityProfileDetail from) {
        V3EntityProfileDetail to = new V3EntityProfileDetail();
        if (from.isSetProfileDetails()) {
            to.getProfileDetail().addAll(adaptOut(from.getProfileDetails().getProfileDetails()));
        }
        return to;
    }
}
