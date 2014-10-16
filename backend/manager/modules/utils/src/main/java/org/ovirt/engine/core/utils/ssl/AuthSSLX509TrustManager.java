/*
 * $HeadURL$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.ovirt.engine.core.utils.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * AuthSSLX509TrustManager can be used to extend the default {@link X509TrustManager} with additional trust decisions.
 * </p>
 *
 *
 * <p>
 * DISCLAIMER: HttpClient developers DO NOT actively support this component. The component is provided as a reference
 * material, which may be inappropriate for use without additional customization.
 * </p>
 */

public class AuthSSLX509TrustManager implements X509TrustManager {
    private X509TrustManager defaultTrustManager = null;

    /** Log object for this class. */
    private static final Logger log = LoggerFactory.getLogger(AuthSSLX509TrustManager.class);

    /**
     * Constructor for AuthSSLX509TrustManager.
     */
    public AuthSSLX509TrustManager(final X509TrustManager defaultTrustManager) {
        super();
        if (defaultTrustManager == null) {
            throw new IllegalArgumentException("Trust manager may not be null");
        }
        this.defaultTrustManager = defaultTrustManager;
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[], String authType)
     */
    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        logCertificate(certificates, "Client");
        defaultTrustManager.checkClientTrusted(certificates, authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[], String authType)
     */
    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
        logCertificate(certificates, "Server");
        defaultTrustManager.checkServerTrusted(certificates, authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.defaultTrustManager.getAcceptedIssuers();
    }

    private void logCertificate(X509Certificate[] certificates, String type) {
        if (log.isDebugEnabled() && certificates != null) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                log.debug(" {} certificate {}:", type, c + 1);
                log.debug("  Subject DN: {}", cert.getSubjectDN());
                log.debug("  Signature Algorithm: {}", cert.getSigAlgName());
                log.debug("  Valid from: {}", cert.getNotBefore());
                log.debug("  Valid until: {}", cert.getNotAfter());
                log.debug("  Issuer: {}", cert.getIssuerDN());
            }
        }
    }
}
