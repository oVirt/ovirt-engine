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

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.api.model.HardwareInformation;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HardwareInformation;
import org.ovirt.engine.api.v3.types.V3RngSources;

public class V3HardwareInformationOutAdapter implements V3Adapter<HardwareInformation, V3HardwareInformation> {
    @Override
    public V3HardwareInformation adapt(HardwareInformation from) {
        V3HardwareInformation to = new V3HardwareInformation();
        if (from.isSetFamily()) {
            to.setFamily(from.getFamily());
        }
        if (from.isSetManufacturer()) {
            to.setManufacturer(from.getManufacturer());
        }
        if (from.isSetProductName()) {
            to.setProductName(from.getProductName());
        }
        if (from.isSetSerialNumber()) {
            to.setSerialNumber(from.getSerialNumber());
        }
        if (from.isSetSupportedRngSources()) {
            to.setSupportedRngSources(new V3RngSources());
            to.getSupportedRngSources().getRngSources().addAll(adaptRngSources(from));
        }
        if (from.isSetUuid()) {
            to.setUuid(from.getUuid());
        }
        if (from.isSetVersion()) {
            to.setVersion(from.getVersion());
        }
        return to;
    }

    private List<String> adaptRngSources(HardwareInformation from) {
        List<String> results = new LinkedList<>();
        for (RngSource source : from.getSupportedRngSources().getSupportedRngSources()) {
            results.add(source.value());
        }
        return results;
    }
}
