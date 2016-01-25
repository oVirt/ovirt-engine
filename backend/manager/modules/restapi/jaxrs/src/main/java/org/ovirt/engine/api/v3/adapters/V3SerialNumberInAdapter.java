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

import org.ovirt.engine.api.model.SerialNumber;
import org.ovirt.engine.api.model.SerialNumberPolicy;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3SerialNumber;

public class V3SerialNumberInAdapter implements V3Adapter<V3SerialNumber, SerialNumber> {
    @Override
    public SerialNumber adapt(V3SerialNumber from) {
        SerialNumber to = new SerialNumber();
        if (from.isSetPolicy()) {
            to.setPolicy(SerialNumberPolicy.fromValue(from.getPolicy()));
        }
        if (from.isSetValue()) {
            to.setValue(from.getValue());
        }
        return to;
    }
}
