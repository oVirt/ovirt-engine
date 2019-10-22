/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.v3.adapters;

import org.ovirt.engine.api.model.ProductInfo;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.v3.V3Adapter;
import org.ovirt.engine.api.v3.types.V3ProductInfo;
import org.ovirt.engine.api.v3.types.V3Version;

public class V3ProductInfoOutAdapter implements V3Adapter<ProductInfo, V3ProductInfo> {
    @Override
    public V3ProductInfo adapt(ProductInfo from) {
        V3ProductInfo to = new V3ProductInfo();
        if (from.isSetName()) {
            to.setName(from.getName());
        }
        if (from.isSetVendor()) {
            to.setVendor(from.getVendor());
        }
        Version fromVersion = from.getVersion();
        if (fromVersion != null) {
            V3Version toVersion = new V3Version();
            toVersion.setMajor(fromVersion.getMajor());
            toVersion.setMinor(fromVersion.getMinor());
            toVersion.setBuild(fromVersion.getBuild());
            toVersion.setRevision(fromVersion.getRevision());
            toVersion.setFullVersion(fromVersion.getFullVersion());
            to.setVersion(toVersion);
            to.setFullVersion(fromVersion.getFullVersion());
        }
        return to;
    }
}
