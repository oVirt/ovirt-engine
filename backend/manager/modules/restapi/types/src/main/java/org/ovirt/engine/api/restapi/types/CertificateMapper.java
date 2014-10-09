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

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.ovirt.engine.api.model.Certificate;

public class CertificateMapper {
    @Mapping(from = java.security.cert.Certificate.class, to = Certificate.class)
    public static Certificate map(java.security.cert.Certificate entity, Certificate template) {
        try {
            Certificate model = template != null? template: new Certificate();
            X509Certificate x509 = (X509Certificate) entity;
            try {
                byte[] content = x509.getEncoded();
                byte[] encoded = Base64.encodeBase64(content, false);
                String text = StringUtils.newStringUtf8(encoded);
                model.setContent(text);
            }
            catch (CertificateEncodingException exception) {
                throw new IllegalArgumentException("Can't encode X.509 certificate", exception);
            }
            model.setSubject(x509.getSubjectDN().toString());
            return model;
        }
        catch (ClassCastException exception) {
            throw new IllegalArgumentException("Only X.509 certificates are supported", exception);
        }
    }
}
