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

import org.ovirt.engine.api.model.NumaNodePin;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3NumaNodePin;

public class V3NumaNodePinOutAdapter implements V3Adapter<NumaNodePin, V3NumaNodePin> {
    @Override
    public V3NumaNodePin adapt(NumaNodePin from) {
        V3NumaNodePin to = new V3NumaNodePin();
        if (from.isSetHostNumaNode()) {
            to.setHostNumaNode(adaptOut(from.getHostNumaNode()));
        }
        if (from.isSetIndex()) {
            to.setIndex(from.getIndex());
        }
        if (from.isSetPinned()) {
            to.setPinned(from.isPinned());
        }
        return to;
    }
}
