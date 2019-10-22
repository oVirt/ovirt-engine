/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;

public class IpVersionMapper {
    public static IpVersion map(ExternalSubnet.IpVersion entity) {
        switch (entity) {
            case IPV4:
                return IpVersion.V4;
            case IPV6:
                return IpVersion.V6;
            default:
                throw new IllegalArgumentException("Unknown IP version \"" + entity + "\"");
        }
    }

    public static ExternalSubnet.IpVersion map(IpVersion model) {
        switch (model) {
            case V4:
                return ExternalSubnet.IpVersion.IPV4;
            case V6:
                return ExternalSubnet.IpVersion.IPV6;
            default:
                throw new IllegalArgumentException("Unknown IP version \"" + model + "\"");
        }
    }
}
