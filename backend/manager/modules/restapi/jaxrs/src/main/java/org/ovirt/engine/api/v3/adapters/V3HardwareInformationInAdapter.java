/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.api.model.HardwareInformation;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3HardwareInformation;

public class V3HardwareInformationInAdapter implements V3Adapter<V3HardwareInformation, HardwareInformation> {
    @Override
    public HardwareInformation adapt(V3HardwareInformation from) {
        HardwareInformation to = new HardwareInformation();
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
            to.setSupportedRngSources(new HardwareInformation.SupportedRngSourcesList());
            to.getSupportedRngSources().getSupportedRngSources().addAll(adaptRngSources(from));
        }
        if (from.isSetUuid()) {
            to.setUuid(from.getUuid());
        }
        if (from.isSetVersion()) {
            to.setVersion(from.getVersion());
        }
        return to;
    }

    private List<RngSource> adaptRngSources(V3HardwareInformation from) {
        List<RngSource> results = new LinkedList<>();
        for (String s : from.getSupportedRngSources().getRngSources()) {
            results.add(RngSource.fromValue(s));
        }
        return results;
    }
}
