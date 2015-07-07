package org.ovirt.engine.core.uutils.crypto;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Certificate chain related tools.
 * Example:
 * <pre>
 * CertificateChain.completeChain(CertificateChain.getSSLPeerCertificates(new URL("https://www.google.com")), null)
 * </pre>
 */
public class CertificateChain {

    /**
     * Returns trust anchors out of key store.
     * @param keystore KeyStore to use.
     * @return TrustAnchor
     */
    public static Set<TrustAnchor> keyStoreToTrustAnchors(KeyStore keystore) throws KeyStoreException {
        Set<TrustAnchor> ret = new HashSet<>();

        for (String alias : Collections.list(keystore.aliases())) {
            try {
                KeyStore.Entry entry = keystore.getEntry(alias, null);
                if (entry instanceof KeyStore.TrustedCertificateEntry) {
                    Certificate c = ((KeyStore.TrustedCertificateEntry)entry).getTrustedCertificate();
                    if (c instanceof X509Certificate) {
                        c.verify(c.getPublicKey());
                        ret.add(new TrustAnchor((X509Certificate)c, null));
                    }
                }
            } catch(Exception e) {
                // ignore
            }
        }
        return ret;
    }

    /**
     * Returns trust anchors for the default java key store.
     * @return TrustAnchor
     */
    public static Set<TrustAnchor> getDefaultTrustAnchors() throws GeneralSecurityException, IOException {
        try (
            InputStream is = new FileInputStream(
                System.getProperty(
                    "javax.net.ssl.trustStore",
                    FileSystems.getDefault().getPath(
                        System.getProperty("java.home"),
                        "lib",
                        "security",
                        "cacerts"
                    ).toString()
                )
            )
        ) {
            KeyStore trustStore = KeyStore.getInstance(
                System.getProperty(
                    "javax.net.ssl.trustStoreType",
                    KeyStore.getDefaultType()
                )
            );
            trustStore.load(
                is,
                System.getProperty(
                    "javax.net.ssl.trustStorePassword",
                    "changeit"
                ).toCharArray()
            );

            return keyStoreToTrustAnchors(trustStore);
        }
    }

    /**
     * Builds CertsPath object out of chain candidate.
     * Throws CertPathBuilderException exception if fails among other exceptions.
     * @param chain chain candidate, first end certificate last issuer.
     * @param trustAnchors trust anchors to use.
     * @return CertPath
     */
    public static CertPath buildCertPath(
        List<Certificate> chain,
        Set<TrustAnchor> trustAnchors
    ) throws GeneralSecurityException {
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate((X509Certificate)chain.get(0));
        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(
            trustAnchors,
            selector
        );
        pkixParams.setRevocationEnabled(false);
        pkixParams.setMaxPathLength(-1);
        pkixParams.addCertStore(
            CertStore.getInstance(
                "Collection",
                new CollectionCertStoreParameters(chain)
            )
        );
        return CertPathBuilder.getInstance("PKIX").build(pkixParams).getCertPath();
    }

    /**
     * Complete certificate chain candidate up to root if possible.
     * @param chain chain candidate, first end certificate last issuer.
     * @param extraTrustAnchors extra trust anchors to use.
     * @return Built chain
     */
    public static List<Certificate> completeChain(
        List<Certificate> chain,
        Set<TrustAnchor> extraTrustAnchors
    ) throws GeneralSecurityException, IOException {
        List<Certificate> ret = chain;

        if (ret != null) {
            Certificate top = ret.get(ret.size()-1);
            boolean topIsRoot = false;
            try {
                top.verify(top.getPublicKey());
                topIsRoot = true;
            } catch(Exception e) {
                // ignore
            }

            if (!topIsRoot && ret.get(0) instanceof X509Certificate) {
                try {
                    Set<TrustAnchor> trustAnchors = getDefaultTrustAnchors();
                    if (extraTrustAnchors != null) {
                        trustAnchors.addAll(extraTrustAnchors);
                    }
                    ret = new ArrayList<>(buildCertPath(ret, trustAnchors).getCertificates());
                    top = ret.get(ret.size()-1);
                    for (TrustAnchor t : trustAnchors) {
                        try {
                            Certificate c= t.getTrustedCert();
                            top.verify(c.getPublicKey());
                            ret.add(c);
                            break;
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                } catch (CertPathBuilderException e) {
                    // ignore
                }
            }
        }

        return ret;
    }

    /**
     * Retrieve SSL peer certificate.
     * @param url URL to use.
     * @return Chain received from peer.
     */
    public static List<Certificate> getSSLPeerCertificates(URL url) throws GeneralSecurityException, IOException {
        List<Certificate> ret = null;

        if ("https".equals(url.getProtocol())) {
            SSLContext ctx = SSLContext.getInstance("TLS");

            //
            // handshake may fail due to various of reasons
            // we need peer certificate, we do not care about any
            // other information.
            // so we collect the peer certificate out of the server
            // hello message which triggers the trust manager early
            // during handshake.
            //
            final List<Certificate> tmcerts = new ArrayList<>();
            ctx.init(
                null,
                new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[] {};
                        }
                        public void checkClientTrusted(
                            X509Certificate[] certs,
                            String authType
                        ) {
                        }
                        public void checkServerTrusted(
                            X509Certificate[] certs,
                            String authType
                        ) {
                            tmcerts.addAll(Arrays.asList(certs));
                        }
                    }
                },
                null
            );

            try (
                SSLSocket sock = (SSLSocket)ctx.getSocketFactory().createSocket(
                    url.getHost(),
                    url.getPort() != -1 ? url.getPort() : url.getDefaultPort()
                )
            ) {
                sock.setSoTimeout(60*1000);
                try {
                    sock.startHandshake();
                } catch (Exception e) {
                    // ignore get whatever we can from trust manager
                }
                if (!tmcerts.isEmpty()) {
                    ret = tmcerts;
                }
            }
        }

        return ret;
    }

}
