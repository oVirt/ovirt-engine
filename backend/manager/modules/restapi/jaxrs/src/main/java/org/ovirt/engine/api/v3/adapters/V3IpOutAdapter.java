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

import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3IP;

public class V3IpOutAdapter implements V3Adapter<Ip, V3IP> {
    @Override
    public V3IP adapt(Ip from) {
        V3IP to = new V3IP();
        if (from.isSetAddress()) {
            to.setAddress(from.getAddress());
        }
        if (from.isSetGateway()) {
            to.setGateway(from.getGateway());
        }
        if (from.isSetNetmask()) {
            to.setNetmask(from.getNetmask());
        }
        if (from.isSetVersion()) {
            to.setVersion(from.getVersion().value());
        }
        return to;
    }
}
