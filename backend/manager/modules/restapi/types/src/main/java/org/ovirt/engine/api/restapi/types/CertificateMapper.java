/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
