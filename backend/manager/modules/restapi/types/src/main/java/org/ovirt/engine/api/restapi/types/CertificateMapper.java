/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.Certificate;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;

public class CertificateMapper {
    @Mapping(from = CertificateInfo.class, to = Certificate.class)
    public static Certificate map(CertificateInfo entity, Certificate template) {
        Certificate model = template != null ? template : new Certificate();
        model.setContent(entity.getPayload());
        model.setSubject(entity.getSubject());
        return model;
    }
}
